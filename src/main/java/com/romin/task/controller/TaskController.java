package com.romin.task.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.romin.task.dto.request.DescriptionRequest;
import com.romin.task.dto.request.DueDateRequest;
import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.response.ApiResponse;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.service.ServiceResult;
import com.romin.task.service.TaskService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTask(
                                                            @Valid @RequestBody TaskRequestDto request
                                                        ){
        ServiceResult<TaskResponseDto> result = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                 .body(
                     buildResponse(
                           result,
                           HttpStatus.CREATED.value()
                       )
                  );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteTask(@PathVariable Long id){
        ServiceResult<Object> result = taskService.deleteTask(id);
        return ResponseEntity.ok(
                   buildResponse(
                        result, 
                        HttpStatus.OK.value()
                    )
               );
    }

    @PatchMapping("/{id}/updateDescription")
    public ResponseEntity<ApiResponse<TaskResponseDto>> updateDescription(
                                                @PathVariable Long id,
                                                @Valid @RequestBody DescriptionRequest request
                                            ){
        ServiceResult<TaskResponseDto> result = taskService.updateDescription(
                                                                request,
                                                                id
                                                            );
        return ResponseEntity.ok(
                   buildResponse(
                        result, 
                        HttpStatus.OK.value()
                   )
               );
    }

    @PatchMapping("/{id}/extendDue")
    public ResponseEntity<ApiResponse<TaskResponseDto>> extendDueDate(
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody DueDateRequest request
                                                        ){
        ServiceResult<TaskResponseDto> result = taskService.extendDueDate(
                                                                request,
                                                                id
                                                            );
        return ResponseEntity.ok(
                   buildResponse(
                        result,
                        HttpStatus.OK.value()
                   )
               );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<TaskResponseDto>> cancelTask(@PathVariable Long id){
        ServiceResult<TaskResponseDto> result = taskService.cancelTask(id);
        return ResponseEntity.ok(
                   buildResponse(
                        result, 
                        HttpStatus.OK.value()
                   )
               );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> getTask(@PathVariable Long id){
         ServiceResult<TaskResponseDto> result = taskService.getTaskById(id);
         return ResponseEntity.ok(
                    buildResponse(
                        result, 
                        HttpStatus.OK.value()
                    )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponseDto>>> getAllTasks(){
        ServiceResult<List<TaskResponseDto>> result = taskService.getAllTask();
        return ResponseEntity.ok(
                   buildResponse(
                        result, 
                        HttpStatus.OK.value()
                    )
               );
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TaskResponseDto>> startTask(@PathVariable Long id){
        ServiceResult<TaskResponseDto> result = taskService.startTask(id);
        return ResponseEntity.ok(
                   buildResponse(
                        result, 
                        HttpStatus.OK.value()
                    )
               );
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskResponseDto>> completeTask(@PathVariable Long id){
        ServiceResult<TaskResponseDto> result = taskService.completeTask(id);
        return ResponseEntity.ok(
                   buildResponse(
                         result, 
                         HttpStatus.OK.value()
                   )
               );
    }

    private <T> ApiResponse<T> buildResponse(ServiceResult<T> result, int status){
        return ApiResponse.<T>builder()
                              .message(result.getMessage())
                              .timeStamp(LocalDateTime.now())
                              .status(status)
                              .data(result.getData())
                              .build();
    }
}
