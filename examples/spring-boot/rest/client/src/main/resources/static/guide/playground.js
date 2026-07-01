(() => {
  const $ = (id) => document.getElementById(id);
  const templateNodes = [...document.querySelectorAll('.playground-template-card[data-template-id]')];

  const state = {
    selectedTemplateId: null,
    activeSource: null,
    activeEvents: [],
    emitterStatus: 'Idle',
    callbackPoller: null
  };

  function escapeHtml(value) {
    return String(value)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function parseJson(text) {
    const value = String(text || '').trim();
    return value ? JSON.parse(value) : {};
  }

  function prettyJson(value) {
    if (typeof value === 'string') {
      try {
        return JSON.stringify(JSON.parse(value), null, 2);
      }
      catch (error) {
        return value;
      }
    }
    return JSON.stringify(value, null, 2);
  }

  function templateNodesOrDefault() {
    return templateNodes.length > 0 ? templateNodes : [];
  }

  function templateData(template, key, fallback = '') {
    if (!template) {
      return fallback;
    }
    return template.dataset[key] || fallback;
  }

  function selectedTemplate() {
    const nodes = templateNodesOrDefault();
    if (!nodes.length) {
      return null;
    }
    return nodes.find((template) => template.dataset.templateId === state.selectedTemplateId) || nodes[0];
  }

  function formatTime(value) {
    if (!value) {
      return '';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return String(value);
    }
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  function currentTaskId() {
    const value = $('playground-current-task')?.textContent?.trim();
    return value && value !== 'none yet' ? value : null;
  }

  function currentContextId() {
    const value = $('playground-current-context')?.textContent?.trim();
    return value && value !== 'none yet' ? value : null;
  }

  function updateSessionCards(taskId, contextId) {
    const taskEl = $('playground-current-task');
    const contextEl = $('playground-current-context');
    if (taskEl) {
      taskEl.textContent = taskId || 'none yet';
    }
    if (contextEl) {
      contextEl.textContent = contextId || 'none yet';
    }
  }

  function setEmitterStatus(status) {
    state.emitterStatus = status;
    const statusEl = $('playground-emitter-status');
    if (statusEl) {
      statusEl.textContent = status;
    }
  }

  function resetResultPanel(message) {
    $('playground-status').textContent = 'Ready to run';
    $('playground-human').textContent = message || 'Choose a template and run it.';
    $('playground-http').textContent = 'No request yet.';
    $('playground-state').textContent = 'State: -';
    $('playground-task').textContent = 'Task: -';
    $('playground-context').textContent = 'Context: -';
    $('playground-server-response').textContent = 'No response yet.';
  }

  function renderEmitterEmpty(message) {
    $('playground-emitter-log').innerHTML = `<div class="result-empty">${escapeHtml(message)}</div>`;
  }

  function summarizeEmitterPayload(rawJson) {
    const payload = tryParseJson(rawJson);
    if (!payload) {
      return {
        kind: 'event',
        taskId: '',
        state: '',
        artifact: '',
        rawJson: rawJson
      };
    }

    const updateEvent = payload.updateEvent || {};
    const statusUpdate = updateEvent.statusUpdate || {};
    const artifactUpdate = updateEvent.artifactUpdate || {};
    const task = payload.task?.task || payload.task || {};
    const status = statusUpdate.status || task.status || {};
    const artifact = artifactUpdate.artifact || null;
    const artifactText = artifact?.parts?.[0]?.text || artifact?.parts?.[0]?.inlineData?.data || '';
    const kind = updateEvent.statusUpdate ? 'status' : updateEvent.artifactUpdate ? 'artifact' : 'event';

    return {
      kind: kind,
      taskId: statusUpdate.taskId || artifactUpdate.taskId || task.id || payload.taskId || '',
      state: status.state || '',
      artifact: kind === 'artifact' ? artifactText : '',
      rawJson: payload
    };
  }

  function renderEmitterLog() {
    if (!state.activeEvents.length) {
      renderEmitterEmpty(state.emitterStatus === 'Idle' ? 'Streaming events will appear here.' : 'Waiting for the first event...');
      return;
    }

    $('playground-emitter-log').innerHTML = state.activeEvents.map((event, index) => `
      <article class="emitter-card">
        <div class="emitter-card-head">
          <div>
            <strong>#${index + 1} ${escapeHtml(event.stage || 'event')}</strong>
            <span>${escapeHtml(event.description || '')}</span>
          </div>
          <time>${escapeHtml(formatTime(event.timestamp))}</time>
        </div>
        <div class="emitter-meta">
          <span class="emitter-chip">${escapeHtml(event.kind || 'event')}</span>
          ${event.taskId ? `<span class="emitter-chip">Task ${escapeHtml(event.taskId)}</span>` : ''}
          ${event.state ? `<span class="emitter-chip">${escapeHtml(event.state)}</span>` : ''}
        </div>
        ${event.artifact ? `<div class="emitter-artifact">${escapeHtml(event.artifact)}</div>` : ''}
        ${event.kind === 'result' || !event.rawJson ? '' : `
          <details class="emitter-details">
            <summary>Raw JSON</summary>
            <div class="emitter-raw">
              <pre><code>${escapeHtml(prettyJson(event.rawJson))}</code></pre>
            </div>
          </details>
        `}
      </article>
    `).join('');
  }

  function renderCallbackLog(callbacks) {
    const list = Array.isArray(callbacks) ? callbacks : [];
    const root = $('playground-callback-log');
    if (!root) {
      return;
    }
    if (!list.length) {
      root.innerHTML = '<div class="result-empty">No callbacks yet.</div>';
      return;
    }
    root.innerHTML = list.map((callback, index) => `
      <article class="callback-card">
        <div class="callback-card-head">
          <div>
            <strong>Callback #${list.length - index}</strong>
            <span>${escapeHtml(formatTime(callback.timestamp))}</span>
          </div>
        </div>
        <pre><code>${escapeHtml(prettyJson(callback.body))}</code></pre>
      </article>
    `).join('');
  }

  function appendEmitterEvent(event) {
    state.activeEvents.push(event);
    renderEmitterLog();
    if (event.taskId || event.contextId) {
      updateSessionCards(event.taskId || currentTaskId(), event.contextId || currentContextId());
    }
  }

  function renderTraceEvents(traceEvents) {
    const events = Array.isArray(traceEvents) ? traceEvents : [];
    state.activeEvents = events.map((traceEvent) => {
      const payload = summarizeEmitterPayload(traceEvent.rawJson);
      return {
        timestamp: traceEvent.timestamp,
        stage: traceEvent.stage || 'trace',
        description: traceEvent.description || 'Trace event received',
        ...payload
      };
    });
    renderEmitterLog();
  }

  function closeStream(message) {
    if (state.activeSource) {
      state.activeSource.close();
      state.activeSource = null;
    }
    setEmitterStatus(message || 'Idle');
  }

  async function refreshCallbacks() {
    const root = $('playground-callback-log');
    if (!root) {
      return;
    }
    try {
      const response = await fetch('/guide/push-notifications/callback', {
        headers: { Accept: 'application/json' }
      });
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      const callbacks = await response.json();
      renderCallbackLog(callbacks);
    }
    catch (error) {
      root.innerHTML = `<div class="result-empty">Callback inbox unavailable: ${escapeHtml(error.message || 'request failed')}</div>`;
    }
  }

  function startCallbackPolling() {
    if (state.callbackPoller) {
      return;
    }
    refreshCallbacks();
    state.callbackPoller = window.setInterval(refreshCallbacks, 2000);
  }

  function setTemplate(template) {
    if (!template) {
      state.selectedTemplateId = null;
      $('playground-template-title').textContent = 'No templates available';
      $('playground-template-endpoint').textContent = 'POST /guide/scenarios/{scenarioId}/run';
      $('playground-template-description').textContent = 'No templates were rendered.';
      $('playground-template-mode').textContent = 'Editable JSON';
      $('playground-json').value = '{}';
      resetResultPanel('No scenarios were rendered by the server.');
      state.activeEvents = [];
      renderEmitterLog();
      return;
    }
    state.selectedTemplateId = template.dataset.templateId;
    templateNodes.forEach((card) => card.classList.toggle('active', card === template));
    $('playground-template-title').textContent = templateData(template, 'title', 'Template');
    $('playground-template-endpoint').textContent = `${templateData(template, 'method', 'POST')} ${templateData(template, 'endpoint')}`;
    $('playground-template-description').textContent = templateData(template, 'description');
    $('playground-template-mode').textContent = templateData(template, 'stream') === 'true'
      ? 'Streaming template'
      : (templateData(template, 'requiresTaskId') === 'true' ? 'Task-aware JSON' : 'Editable JSON');
    $('playground-json').value = prettyTemplateJson(template);
    resetResultPanel('Choose a template and run it.');
    state.activeEvents = [];
    renderEmitterLog();
  }

  function prettyTemplateJson(template) {
    const templateId = templateData(template, 'templateId');
    const payload = tryParseJson(templateData(template, 'defaultJson', '{}')) || {};
    const taskId = currentTaskId();
    const contextId = currentContextId();

    if (templateId === 'list-tasks' && !payload.contextId && contextId) {
      payload.contextId = contextId;
    }
    if (['inspect-task', 'subscribe-task', 'cancel-task', 'create-push-config', 'get-push-config', 'list-push-configs',
      'delete-push-config'].includes(templateId) && !payload.taskId && taskId) {
      payload.taskId = taskId;
    }

    return prettyJson(payload);
  }

  function summarizeServerResponse(result) {
    const step = result.steps && result.steps[0] ? result.steps[0] : null;
    const payload = step?.responseJson ? tryParseJson(step.responseJson) : null;
    const responseType = step?.responseType || 'Response';

    if (!payload) {
      return { summary: responseType, serverPayload: 'No response body returned.' };
    }

    if (responseType === 'AgentCard' || payload.name) {
      return {
        summary: `Agent Card: ${payload.name || 'unknown'}${payload.version ? ` v${payload.version}` : ''}`,
        serverPayload: prettyJson({
          name: payload.name,
          description: payload.description,
          version: payload.version,
          capabilities: payload.capabilities,
          supportedInterfaces: payload.supportedInterfaces,
          defaultInputModes: payload.defaultInputModes,
          defaultOutputModes: payload.defaultOutputModes,
          skills: payload.skills
        })
      };
    }

    if (Array.isArray(payload.tasks)) {
      return {
        summary: `Listed ${payload.tasks.length} tasks`,
        serverPayload: prettyJson(payload)
      };
    }

    if (Array.isArray(payload.configs)) {
      return {
        summary: `Listed ${payload.configs.length} push configs`,
        serverPayload: prettyJson(payload)
      };
    }

    if (payload.url && payload.taskId) {
      return {
        summary: `Push config ${payload.id || 'saved'} for task ${payload.taskId}`,
        serverPayload: prettyJson(payload)
      };
    }

    if (payload.task || payload.id || payload.status) {
      const task = payload.task || payload;
      return {
        summary: `Task ${task.id || '-'} is ${task.status?.state || 'unknown'}`,
        serverPayload: prettyJson({
          id: task.id,
          contextId: task.contextId,
          state: task.status?.state,
          timestamp: task.status?.timestamp,
          artifacts: task.artifacts,
          history: task.history
        })
      };
    }

    if (payload.message || payload.role || payload.parts) {
      const message = payload.message || payload;
      return {
        summary: `Message from ${message.role || 'server'}`,
        serverPayload: prettyJson({
          role: message.role,
          parts: message.parts,
          messageId: message.messageId,
          taskId: message.taskId,
          contextId: message.contextId
        })
      };
    }

    return { summary: responseType, serverPayload: prettyJson(payload) };
  }

  function tryParseJson(value) {
    try {
      return typeof value === 'string' ? JSON.parse(value) : value;
    }
    catch (error) {
      return null;
    }
  }

  function renderResult(result, template, payload) {
    const responseView = summarizeServerResponse(result);
    const firstStep = result.steps && result.steps[0] ? result.steps[0] : null;
    const taskId = result.currentTaskId || firstStep?.taskId || '-';
    const contextId = result.currentContextId || firstStep?.contextId || '-';

    $('playground-status').textContent = result.success ? 'Completed' : 'Failed';
    $('playground-human').textContent = responseView.summary || result.summary || 'Execution completed.';
    $('playground-http').textContent = `${templateData(template, 'method', 'POST')} ${templateData(template, 'endpoint')}`;
    $('playground-state').textContent = firstStep
      ? `State: ${firstStep.stateBefore || 'n/a'} -> ${firstStep.stateAfter || 'n/a'}`
      : 'State: -';
    $('playground-task').textContent = `Task: ${taskId}`;
    $('playground-context').textContent = `Context: ${contextId}`;
    $('playground-server-response').textContent = responseView.serverPayload;
    updateSessionCards(result.currentTaskId || firstStep?.taskId || currentTaskId(), result.currentContextId || firstStep?.contextId || currentContextId());
  }

  async function runRequest(template) {
    if (!template) {
      $('playground-status').textContent = 'No template';
      $('playground-human').textContent = 'No scenario template is available on this page.';
      return;
    }
    const payload = parseJson($('playground-json').value);
    const templateId = templateData(template, 'templateId');
    const effectivePayload = { ...payload };
    if (templateId === 'list-tasks' && !effectivePayload.contextId && currentContextId()) {
      effectivePayload.contextId = currentContextId();
    }
    if (['inspect-task', 'subscribe-task', 'cancel-task', 'create-push-config', 'get-push-config', 'list-push-configs',
      'delete-push-config'].includes(templateId) && !effectivePayload.taskId && currentTaskId()) {
      effectivePayload.taskId = currentTaskId();
    }
    if (templateData(template, 'requiresTaskId') === 'true' && !payload.taskId && !currentTaskId()) {
      $('playground-status').textContent = 'Task ID required';
      $('playground-human').textContent = 'This template needs a task id. Run a task scenario first or paste one into the JSON form.';
      return;
    }

    if (templateData(template, 'stream') === 'true') {
      closeStream('Idle');
      state.activeEvents = [];
      renderEmitterLog();
      $('playground-status').textContent = 'Streaming...';
      $('playground-human').textContent = 'Connecting to the live emitter.';
      renderEmitterEmpty('Connecting to the live emitter...');
      setEmitterStatus('Connecting');

      const params = new URLSearchParams();
      if (effectivePayload.prompt) {
        params.set('prompt', effectivePayload.prompt);
      }
      if (effectivePayload.streamingTimeoutSeconds) {
        params.set('streamingTimeoutSeconds', String(effectivePayload.streamingTimeoutSeconds));
      }

      const source = new EventSource(`/guide/scenarios/${templateData(template, 'templateId')}/stream?${params.toString()}`);
      state.activeSource = source;
      source.onopen = () => setEmitterStatus('Live');
      source.addEventListener('trace', (event) => {
        const parsed = JSON.parse(event.data);
        appendEmitterEvent({
          timestamp: new Date().toISOString(),
          stage: parsed.stage || 'trace',
          description: parsed.description || 'Trace event received',
          ...summarizeEmitterPayload(parsed.rawJson),
          rawJson: parsed.rawJson ?? parsed
        });
      });
      source.addEventListener('result', (event) => {
        const result = JSON.parse(event.data);
        const lastStep = result.steps && result.steps.length > 0 ? result.steps[result.steps.length - 1] : null;
        const responsePayload = lastStep?.responseJson ? tryParseJson(lastStep.responseJson) : result;
        appendEmitterEvent({
          timestamp: new Date().toISOString(),
          stage: 'result',
          description: `${result.success ? 'Success' : 'Failure'} result received`,
          kind: 'result',
          taskId: result.currentTaskId || result.steps?.[0]?.taskId || '',
          state: result.steps?.[0]?.stateAfter || result.steps?.[0]?.stateBefore || '',
          artifact: result.summary || result.steps?.[0]?.responseType || '',
          rawJson: null,
          responsePayload: responsePayload
        });
        renderResult(result, template, payload);
        closeStream('Completed');
      });
      source.onerror = () => {
        if (state.activeSource) {
          closeStream('Stopped');
          $('playground-status').textContent = 'Stream stopped';
          $('playground-human').textContent = 'The stream stopped or the server became unavailable.';
        }
      };
      return;
    }

    $('playground-status').textContent = 'Running...';
    $('playground-human').textContent = 'Sending request to the A2A server.';
    $('playground-http').textContent = `${templateData(template, 'method', 'POST')} ${templateData(template, 'endpoint')}`;
    setEmitterStatus(state.activeSource ? 'Live' : 'Idle');

    const response = await fetch(`/guide/scenarios/${templateData(template, 'templateId')}/run`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(effectivePayload)
    });
    const text = await response.text();
    if (!response.ok) {
      throw new Error(text || `Request failed with HTTP ${response.status}`);
    }
    const result = JSON.parse(text);
    renderResult(result, template, effectivePayload);
    if (Array.isArray(result.traceEvents) && result.traceEvents.length > 0) {
      setEmitterStatus(templateData(template, 'templateId') === 'subscribe-task' ? 'Subscribed' : 'Completed');
      renderTraceEvents(result.traceEvents);
    }
    else if (templateData(template, 'templateId') === 'subscribe-task') {
      setEmitterStatus(state.activeSource ? 'Subscribed' : 'Subscribed');
      if (!state.activeSource) {
        renderEmitterEmpty('No future events arrived. Subscribe only shows updates while the task is still running.');
      }
    }
    else {
      if (!state.activeSource) {
        renderEmitterEmpty('This operation does not stream events.');
      }
    }
  }

  templateNodes.forEach((template) => {
    template.addEventListener('click', () => setTemplate(template));
  });

  $('playground-refresh-callbacks').addEventListener('click', refreshCallbacks);

  $('playground-run').addEventListener('click', () => {
    runRequest(selectedTemplate()).catch((error) => {
      $('playground-status').textContent = 'Failed';
      $('playground-human').textContent = error.message || 'Request failed';
      setEmitterStatus('Idle');
    });
  });

  $('playground-stop-stream').addEventListener('click', () => {
    closeStream('Stopped');
    $('playground-status').textContent = 'Stream stopped';
    $('playground-human').textContent = 'The live emitter was stopped from the client.';
  });

  $('playground-clear-emitter').addEventListener('click', () => {
    closeStream('Idle');
    state.activeEvents = [];
    renderEmitterLog();
  });

  $('playground-reset-json').addEventListener('click', () => {
    setTemplate(selectedTemplate());
  });

  $('playground-use-task-id').addEventListener('click', () => {
    const template = selectedTemplate();
    try {
      const payload = parseJson($('playground-json').value);
      const taskId = currentTaskId();
      if (taskId) {
        payload.taskId = taskId;
      }
      $('playground-json').value = prettyJson(payload);
      if (template && templateData(template, 'requiresTaskId') === 'true') {
        $('playground-status').textContent = 'Task ID loaded';
      }
    }
    catch (error) {
      $('playground-status').textContent = 'Invalid JSON';
      $('playground-human').textContent = 'Fix the JSON before reusing the current task id.';
    }
  });

  $('playground-json').addEventListener('input', () => {
    try {
      parseJson($('playground-json').value);
    }
    catch (error) {
      return;
    }
  });

  if (templateNodes.length > 0) {
    setTemplate(templateNodes[0]);
  }
  else {
    setTemplate(null);
  }

  startCallbackPolling();
})();
