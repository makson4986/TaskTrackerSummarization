package org.makson.tasktrackersummarization.dto;

import java.util.List;

public record LLMResponseDto(List<Choices> choices) {
    public record Choices(MessageDto message, int index, FinishReason finishReason) {
    }
}
