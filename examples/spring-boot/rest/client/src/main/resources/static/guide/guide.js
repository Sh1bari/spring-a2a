(() => {
  const $ = (id) => document.getElementById(id);

  const state = {
    selectedScenarioId: null,
    selectedScenarioStream: false,
    selectedScenarioPrompt: '',
    selectedScenarioTimeout: 20,
    activeSource: null
  };

  const scenarioCards = [...document.querySelectorAll('.scenario-card[data-scenario-id]')];

  function escapeHtml(value) {
    return String(value)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function splitList(value) {
    if (!value) {
      return [];
    }
    return String(value)
      .replace(/^\[|\]$/g, '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }

  function setScenarioDetails(card) {
    state.selectedScenarioId = card.dataset.scenarioId;
    state.selectedScenarioStream = card.dataset.stream === 'true';
    state.selectedScenarioPrompt = card.dataset.prompt || '';
    state.selectedScenarioTimeout = Number(card.dataset.timeout || 20);

    $('active-scenario-title').textContent = card.dataset.title || 'Scenario';
    $('active-scenario-goal').textContent = card.dataset.goal || '';
    $('active-scenario-purpose').textContent = card.dataset.purpose || '';
    $('active-scenario-endpoint').textContent = `${card.dataset.method || 'POST'} ${card.dataset.endpoint || ''}`;
    $('prompt-input').value = state.selectedScenarioPrompt;
    $('timeout-input').value = String(state.selectedScenarioTimeout);

    const learning = splitList(card.dataset.learning);
    const concepts = splitList(card.dataset.concepts);
    $('active-scenario-learning').innerHTML = learning.map((item) => `<li>${escapeHtml(item)}</li>`).join('') || '<li>No learning points configured.</li>';
    $('active-scenario-concepts').innerHTML = concepts.map((item) => `<span class="chip">${escapeHtml(item)}</span>`).join('') || '<span class="chip">A2A</span>';

    scenarioCards.forEach((scenarioCard) => {
      scenarioCard.classList.toggle('active', scenarioCard.dataset.scenarioId === state.selectedScenarioId);
    });
  }

  function requestPayload() {
    return {
      prompt: $('prompt-input').value || null,
      streamingTimeoutSeconds: Number($('timeout-input').value || 20)
    };
  }

  function setRequestPanel(result, scenarioCard) {
    const requestJson = result.steps && result.steps[0] ? result.steps[0].requestJson || '' : '';
    const requestCurl = result.steps && result.steps[0] ? result.steps[0].curl || '' : '';
    const step = result.steps && result.steps[0] ? result.steps[0] : null;

    $('request-method').textContent = step ? step.method : (scenarioCard?.dataset.method || 'POST');
    $('request-endpoint').textContent = step ? step.endpoint : (scenarioCard?.dataset.endpoint || '');
    $('request-json').textContent = requestJson || 'No request body.';
    $('request-curl').textContent = requestCurl || 'curl will appear here.';
    $('response-human').textContent = step ? step.humanReadableResponse || 'No response body.' : 'No response yet.';
    $('response-json').textContent = step ? step.responseJson || 'No response body.' : 'No response yet.';
    $('http-status').textContent = step && step.httpStatus ? String(step.httpStatus) : '--';
    $('response-type').textContent = step ? step.responseType || '--' : '--';
    $('state-before').textContent = `Before: ${step?.stateBefore || 'n/a'}`;
    $('state-after').textContent = `After: ${step?.stateAfter || 'n/a'}`;
  }

  function renderResult(result) {
    $('result-summary').innerHTML = `
      <div class="result-card">
        <div class="scenario-card-head">
          <span class="step-badge">${escapeHtml(result.title || 'Scenario')}</span>
          <span class="method-badge">${result.success ? 'completed' : 'failed'}</span>
        </div>
        <h3>${escapeHtml(result.summary || 'Execution result')}</h3>
        <p>${escapeHtml(result.serverUrl || '')}</p>
      </div>
    `;

    const steps = result.steps || [];
    $('trace-list').innerHTML = steps.flatMap((step) => (step.traceEvents || []).map((trace) => `
      <div class="trace-entry">
        <strong>${escapeHtml(trace.stage || 'trace')}</strong>
        <span>${escapeHtml(trace.description || '')}</span>
        <time>${escapeHtml(trace.timestamp || '')}</time>
      </div>
    `)).join('') || '<div class="result-empty">No trace events were emitted.</div>';

    $('what-happened').innerHTML = (result.whatHappened || []).map((item) => `<li>${escapeHtml(item)}</li>`).join('') || '<li>No explanation available.</li>';
    $('learning-outcome').innerHTML = (result.learningOutcome || []).map((item) => `<li>${escapeHtml(item)}</li>`).join('') || '<li>No learning outcome available.</li>';

    $('history-list').insertAdjacentHTML('afterbegin', `
      <article class="history-card">
        <div class="history-card-head">
          <strong>${escapeHtml(result.title || 'Scenario')}</strong>
          <span>${result.success ? 'completed' : 'failed'}</span>
        </div>
        <p>${escapeHtml(result.summary || '')}</p>
      </article>
    `);

    if (steps.length > 0) {
      const stepsHtml = steps.map((step, index) => `
        <article class="call-card">
          <div class="scenario-card-head">
            <span class="step-badge">Step ${index + 1}</span>
            <span class="method-badge">${escapeHtml(step.method || '')}</span>
          </div>
          <h3>${escapeHtml(step.title || result.title || 'Protocol step')}</h3>
          <div class="call-grid">
            <div>
              <span class="info-label">Request</span>
              <div class="code-block small"><pre><code>${escapeHtml(step.requestJson || 'No request body.')}</code></pre></div>
            </div>
            <div>
              <span class="info-label">Response</span>
              <div class="code-block small"><pre><code>${escapeHtml(step.responseJson || 'No response body.')}</code></pre></div>
            </div>
          </div>
        </article>
      `).join('<div class="step-divider"></div>');
      $('result-summary').insertAdjacentHTML('afterend', `<div id="step-cards" class="panel" style="margin-top: 16px;">${stepsHtml}</div>`);
    }

    if (steps[0]) {
      $('request-method').textContent = steps[0].method || '--';
      $('request-endpoint').textContent = steps[0].endpoint || '--';
      $('request-json').textContent = steps[0].requestJson || 'No request body.';
      $('request-curl').textContent = steps[0].curl || 'curl will appear here.';
      $('response-human').textContent = steps[0].humanReadableResponse || 'No response body.';
      $('response-json').textContent = steps[0].responseJson || 'No response body.';
      $('http-status').textContent = steps[0].httpStatus ? String(steps[0].httpStatus) : '--';
      $('response-type').textContent = steps[0].responseType || '--';
      $('state-before').textContent = `Before: ${steps[0].stateBefore || 'n/a'}`;
      $('state-after').textContent = `After: ${steps[steps.length - 1].stateAfter || 'n/a'}`;
    }
  }

  async function runScenario(scenarioCard, stream = false) {
    setScenarioDetails(scenarioCard);
    const body = JSON.stringify(requestPayload());
    const endpoint = stream ? `/guide/scenarios/${state.selectedScenarioId}/stream` : `/guide/scenarios/${state.selectedScenarioId}/run`;

    $('result-summary').innerHTML = '<div class="result-empty">Running scenario...</div>';
    $('trace-list').innerHTML = '<div class="result-empty">Waiting for trace events...</div>';
    $('what-happened').innerHTML = '<li>Running...</li>';
    $('learning-outcome').innerHTML = '<li>Running...</li>';
    if ($('step-cards')) {
      $('step-cards').remove();
    }

    if (stream) {
      if (state.activeSource) {
        state.activeSource.close();
      }
      const params = new URLSearchParams();
      if ($('prompt-input').value) {
        params.set('prompt', $('prompt-input').value);
      }
      params.set('streamingTimeoutSeconds', String(Number($('timeout-input').value || 20)));
      const source = new EventSource(`${endpoint}?${params.toString()}`);
      state.activeSource = source;
      const liveTrace = [];
      source.addEventListener('trace', (event) => {
        const trace = JSON.parse(event.data);
        liveTrace.push(trace);
        const current = $('trace-list').querySelector('.result-empty');
        if (current) {
          current.remove();
        }
        $('trace-list').insertAdjacentHTML('beforeend', `
          <div class="trace-entry">
            <strong>${escapeHtml(trace.stage || 'trace')}</strong>
            <span>${escapeHtml(trace.description || '')}</span>
            <time>${escapeHtml(trace.timestamp || '')}</time>
          </div>
        `);
      });
      source.addEventListener('result', (event) => {
        const result = JSON.parse(event.data);
        renderResult(result);
        source.close();
        state.activeSource = null;
      });
      source.onerror = () => {
        if (state.activeSource) {
          state.activeSource.close();
          state.activeSource = null;
        }
        $('result-summary').innerHTML = '<div class="result-empty">Streaming stopped or the server is unavailable.</div>';
      };
      return;
    }

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body
    });
    const result = await response.json();
    renderResult(result);
  }

  async function resetDemo() {
    if (state.activeSource) {
      state.activeSource.close();
      state.activeSource = null;
    }
    const response = await fetch('/guide/reset', { method: 'POST' });
    if (response.ok) {
      window.location.reload();
    }
  }

  async function copyText(value) {
    if (!value) {
      return;
    }
    await navigator.clipboard.writeText(value);
  }

  scenarioCards.forEach((card) => {
    card.addEventListener('click', () => setScenarioDetails(card));
    const button = card.querySelector('.scenario-run');
    if (button) {
      button.addEventListener('click', (event) => {
        event.preventDefault();
        runScenario(card, card.dataset.stream === 'true').catch((error) => {
          $('result-summary').innerHTML = `<div class="result-empty">${escapeHtml(error.message || 'Scenario failed')}</div>`;
        });
      });
    }
  });

  $('run-selected-scenario').addEventListener('click', () => {
    const active = scenarioCards.find((card) => card.dataset.scenarioId === state.selectedScenarioId) || scenarioCards[0];
    runScenario(active, active.dataset.stream === 'true').catch((error) => {
      $('result-summary').innerHTML = `<div class="result-empty">${escapeHtml(error.message || 'Scenario failed')}</div>`;
    });
  });

  $('run-current-scenario').addEventListener('click', () => {
    const active = scenarioCards.find((card) => card.dataset.scenarioId === state.selectedScenarioId) || scenarioCards[0];
    runScenario(active, active.dataset.stream === 'true').catch((error) => {
      $('result-summary').innerHTML = `<div class="result-empty">${escapeHtml(error.message || 'Scenario failed')}</div>`;
    });
  });

  $('reset-demo').addEventListener('click', () => resetDemo().catch((error) => {
    $('result-summary').innerHTML = `<div class="result-empty">${escapeHtml(error.message || 'Reset failed')}</div>`;
  }));

  $('copy-request-json').addEventListener('click', () => copyText($('request-json').textContent));
  $('copy-request-curl').addEventListener('click', () => copyText($('request-curl').textContent));
  $('copy-response-json').addEventListener('click', () => copyText($('response-json').textContent));

  if (scenarioCards.length > 0) {
    setScenarioDetails(scenarioCards.find((card) => card.classList.contains('active')) || scenarioCards[0]);
  }
})();
