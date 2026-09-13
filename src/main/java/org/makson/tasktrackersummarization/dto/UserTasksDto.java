package org.makson.tasktrackersummarization.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserTasksDto(
        String userEmail,
        String period,
        List<TaskDto> tasks
) {
    public record TaskDto(
            String title,
            String text,
            TaskStatus status,
            LocalDateTime performedAt
    ) {
    }
}
