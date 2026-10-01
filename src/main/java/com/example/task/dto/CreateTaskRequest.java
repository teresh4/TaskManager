package com.example.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Название задачи обязательно")
        @Size(min = 3, max = 100, message = "Название должно содержать от 3 до 100 символов")
        String title,
        @Size(max = 500, message = "Описание не должно превышать 500 символов")
        String description
) {
}
