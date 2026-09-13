package org.makson.tasktrackersummarization.dto;

import java.util.List;

public record LLMRequestDto(String model, List<MessageDto> messages) {
}
