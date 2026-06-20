package com.romin.task.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequestDto {

    @NotBlank(message = "Tittle should not be blank")
    private String title;

    @NotBlank(message = "Description should not be blank")
    private String description;

    @NotNull(message = "You have to specify your identity")
    private Long assignedBy;

    @NotNull(message = "You have to specify sender's identity")
    private Long assignedTo;

    @NotNull(message = "Due date should have some value")
    @FutureOrPresent(message = "Due date should be in present or future")
    private LocalDate dueDate;
}
