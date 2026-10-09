package org.makson.tasktrackersummarization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LLMResponseDto(List<Choices> choices) {
    public record Choices(MessageDto message, int index, @JsonProperty("finish_reason") FinishReason finishReason) {
    }
}
