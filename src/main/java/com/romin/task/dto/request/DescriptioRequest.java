package com.romin.task.dto.request;

import jakarta.validation.constraints.NotBlank;

public class DescriptioRequest{

    @NotBlank(message = "Description should not be blank")
    private String description;

    public String getDescription(){
        return description;
    }

    public void getDescription(String description){
        this.description=description;
    }

}