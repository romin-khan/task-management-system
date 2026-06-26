package com.romin.task.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

public record UpdateRequest(

    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters if provided.")
    String title,

    @Size(min = 5, max = 2000, message = "Description must be between 5 and 2000 characters.")
    String description,

    @FutureOrPresent(message = "Extended due date must be in the present or future.")
    LocalDate dueDate
){}
