package org.makson.tasktrackersummarization.dto;

import java.time.LocalDate;
import java.util.List;

public record UserTasksDto(
        String userEmail,
        LocalDate period,
        List<TaskDto> tasks
) {
    public record TaskDto(
            String title,
            String text,
            TaskStatus status,
            LocalDate createdAt,
            LocalDate performedAt
    ) {
    }
}
