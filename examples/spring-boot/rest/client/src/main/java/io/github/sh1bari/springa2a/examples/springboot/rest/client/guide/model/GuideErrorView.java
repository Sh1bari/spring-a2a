package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

public record GuideErrorView(String title, String message, Integer httpStatus, String responseBody, String suggestion) {
}
