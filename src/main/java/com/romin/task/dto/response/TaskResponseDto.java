package com.romin.task.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.romin.task.entity.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class TaskResponseDto {
    private final UUID publicId;
    private final String taskId;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final Long assignedBy;
    private final Long assignedTo;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final LocalDate dueDate;
    private final Instant completionDate;
}
