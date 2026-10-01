package com.example.task.dto;

import com.example.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Статус задачи обязателен")
        TaskStatus status
) {
}
