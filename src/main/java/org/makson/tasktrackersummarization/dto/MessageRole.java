package org.makson.tasktrackersummarization.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageRole {
    SYSTEM("system"),
    ASSISTANT("assistant"),
    USER("user"),
    FUNCTION("function");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}
