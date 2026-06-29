package com.romin.task.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.romin.task.entity.TaskStatus;

public record TaskResponseDto (
    UUID publicId,
    String taskId,
    String title,
    String description,
    TaskStatus status,
    Long assignedBy,
    Long assignedTo,
    Instant createdAt,
    Instant updatedAt,
    LocalDate dueDate,
    Instant completionDate
){}
