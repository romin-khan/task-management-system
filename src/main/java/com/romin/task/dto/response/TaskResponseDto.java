package com.romin.task.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.romin.task.entity.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class TaskResponseDto {
    private final Long id;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final Long assignedBy;
    private final Long assignedTo;
    private final LocalDateTime createdAt;
    private final LocalDate dueDate;
    private final LocalDateTime completionDate;
}
