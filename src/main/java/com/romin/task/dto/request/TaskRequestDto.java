package com.romin.task.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskRequestDto {

    @NotBlank(message = "Tittle should not be blank")
    private String title;

    @NotBlank(message = "Description should not be blank")
    private String description;

    @NotBlank(message = "Ypu have to specify sender's identity")
    private String assignedTo;

    @NotNull(message = "Due date should have some value")
    @FutureOrPresent(message = "Due date should be in present or future")
    private LocalDate dueDate;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getassignedTo() {
        return assignedTo;
    }
    public void setassignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
