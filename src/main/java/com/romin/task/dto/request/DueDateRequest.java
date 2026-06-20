package com.romin.task.dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public class DueDateRequest{

    @NotNull(message = "Due date should have some value")
    @FutureOrPresent(message = "Due date should be in present or in future")
    private LocalDate dueDate;

    public LocalDate getDueDate(){
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate){
        this.dueDate=dueDate;
    }

}