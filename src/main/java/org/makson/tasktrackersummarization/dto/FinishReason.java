package org.makson.tasktrackersummarization.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FinishReason {
    STOP("stop"),
    LENGTH("length"),
    FUNCTION_CALL("function_call"),
    BLACKLIST("blacklist"),
    ERROR("error");

    private final String value;

    FinishReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}
