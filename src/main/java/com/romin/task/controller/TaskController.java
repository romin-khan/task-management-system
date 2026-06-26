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
import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.request.UpdateRequest;
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

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String publicId){
        taskService.deleteTaskByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{publicId}")
    public ResponseEntity<TaskResponseDto> update(@PathVariable String publicId,
                                                  @Valid @RequestBody UpdateRequest request){
        TaskResponseDto response = taskService.update(request, publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/cancel")
    public ResponseEntity<TaskResponseDto> cancelTask(@PathVariable String publicId){
        TaskResponseDto response = taskService.cancelTask(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<TaskResponseDto> getTask(@PathVariable String publicId){
         TaskResponseDto response = taskService.getTaskById(publicId);
         return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<TaskResponseDto>> getAllTasks(
                                            @PageableDefault(size = 5, sort = "dueDate") Pageable pageable){
        validateSortProperties(pageable.getSort());
        return ResponseEntity.ok(
            taskService.getAllTask(pageable)
        );
    }

    @PatchMapping("/{publicId}/start")
    public ResponseEntity<TaskResponseDto> startTask(@PathVariable String publicId){
        TaskResponseDto response = taskService.startTask(publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/complete")
    public ResponseEntity<TaskResponseDto> completeTask(@PathVariable String publicId){
        TaskResponseDto response = taskService.completeTask(publicId);
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
