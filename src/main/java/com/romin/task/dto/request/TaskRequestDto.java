package com.romin.task.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskRequestDto(

    @NotBlank(message = "Tittle should not be blank")
    String title,

    @NotBlank(message = "Description should not be blank")
    String description,

    @NotNull(message = "You have to specify your identity")
    Long assignedBy,

    @NotNull(message = "You have to specify sender's identity")
    Long assignedTo,

    @NotNull(message = "Due date should have some value")
    @FutureOrPresent(message = "Due date should be in present or future")
    LocalDate dueDate
){}
