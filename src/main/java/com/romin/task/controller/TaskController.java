package com.romin.task.controller;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.romin.infra.dto.PaginatedResponse;
import com.romin.task.dto.request.DescriptionRequest;
import com.romin.task.dto.request.DueDateRequest;
import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
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
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto request){
        TaskResponseDto response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                 .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<TaskResponseDto> updateDescription(
                                                @PathVariable Long id,
                                                @Valid @RequestBody DescriptionRequest request){
        TaskResponseDto response = taskService.updateDescription(request, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/due-date")
    public ResponseEntity<TaskResponseDto> extendDueDate(
                                                @PathVariable Long id,
                                                @Valid @RequestBody DueDateRequest request ){
        TaskResponseDto response = taskService.extendDueDate(request, id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TaskResponseDto> cancelTask(@PathVariable Long id){
        TaskResponseDto response = taskService.cancelTask(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTask(@PathVariable Long id){
         TaskResponseDto response = taskService.getTaskById(id);
         return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<TaskResponseDto>> getAllTasks(
                                            @PageableDefault(size = 5, sort = "dueDate") Pageable pageable
                                        ){
        validateSortProperties(pageable.getSort());
        return ResponseEntity.ok(
            taskService.getAllTask(pageable)
        );
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<TaskResponseDto> startTask(@PathVariable Long id){
        TaskResponseDto response = taskService.startTask(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponseDto> completeTask(@PathVariable Long id){
        TaskResponseDto response = taskService.completeTask(id);
        return ResponseEntity.ok(response);
    }

    private void validateSortProperties(Sort sort) {
        Set<String> validFields = Set.of("title", "status", "assignedBy", "assignedTo", "createdAt", "completionDate", "dueDate");
        
        for (Sort.Order order : sort) {
            if (!validFields.contains(order.getProperty())) {
                throw new IllegalArgumentException("Invalid sorting properties: " + order.getProperty() + ", the sorting properties are: " + validFields);
            }
        }
    }
}
