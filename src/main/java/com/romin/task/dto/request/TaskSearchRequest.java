package com.romin.task.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.romin.task.entity.TaskStatus;

public record TaskSearchRequest(
        TaskStatus status,
        UUID UserID,
        UUID assignedToUserID,
        UUID assignedByUserID,

        String title,
        String description,

        Instant dueAt,
        Instant dueBefore,
        Instant dueAfter,
        Instant dueFrom,
        Instant dueTo,

        Instant completedAt,
        Instant completedBefore,
        Instant completedAfter,
        Instant completedFrom,
        Instant completedTo
) {}
