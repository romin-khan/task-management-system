package com.romin.task.controller;

import java.util.Set;
import java.util.UUID;

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
import com.romin.task.dto.request.UpdateRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.service.TaskService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }
    
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto request){
        log.info("[HTTP POST] Incoming task creation request received. Payload: {}", request);
        TaskResponseDto response = taskService.createTask(request);

        log.info("[HTTP 201] Task successfully created. Public ID: {}", response.getPublicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID publicId){
        log.info("[HTTP DELETE] Incoming task delete request received. Public ID: {}", publicId);
        taskService.deleteTaskByPublicId(publicId);

        log.info("[HTTP 204] Task deleted successfully. Public ID: {}", publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{publicId}")
    public ResponseEntity<TaskResponseDto> update(@PathVariable UUID publicId,
                                                  @Valid @RequestBody UpdateRequestDto request){
        log.info("[HTTP PATCH] Incoming task update request received. Public ID: {}, Payload: {}", publicId, request);
        TaskResponseDto response = taskService.update(request, publicId);

        log.info("[HTTP 200] Task updated successfully. Public ID: {}", publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/cancel")
    public ResponseEntity<TaskResponseDto> cancelTask(@PathVariable UUID publicId){
        log.info("[HTTP PATCH] Incoming cancel task request received. Public ID: {}", publicId);
        TaskResponseDto response = taskService.cancelTask(publicId);

        log.info("[HTTP 200] Task canceled successfully. Public ID: {}", publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<TaskResponseDto> getTask(@PathVariable UUID publicId){
         log.info("[HTTP GET] Incoming get task request received. Public ID: {}", publicId);
         TaskResponseDto response = taskService.getTaskById(publicId);

         log.info("[HTTP 200] Task fetched successfully. Public ID: {}", publicId);
         return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<TaskResponseDto>> getAllTasks(
                                            @PageableDefault(size = 5, sort = "dueDate") Pageable pageable){
        log.info("[HTTP GET] Incoming get all task request received. Parameters: page={}, size={}, sort={}", 
                 pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        validateSortProperties(pageable.getSort());

        PaginatedResponse<TaskResponseDto> response = taskService.getAllTask(pageable);
        log.info("[HTTP 200] All tasks fetched successfully. Total Count: {}", response.totalElements());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/start")
    public ResponseEntity<TaskResponseDto> startTask(@PathVariable UUID publicId){
        log.info("[HTTP PATCH] Incoming start task request received. Public ID: {}", publicId);
        TaskResponseDto response = taskService.startTask(publicId);

        log.info("[HTTP 200] Task started successfully. Public ID: {}", publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/complete")
    public ResponseEntity<TaskResponseDto> completeTask(@PathVariable UUID publicId){
        log.info("[HTTP PATCH] Incoming complete task request received. Public ID: {}", publicId);
        TaskResponseDto response = taskService.completeTask(publicId);

        log.info("[HTTP 200] Task completed successfully. Public ID: {}", publicId);
        return ResponseEntity.ok(response);
    }

    private void validateSortProperties(Sort sort) {
        Set<String> validFields = Set.of("title", "status", "assignedBy", "assignedTo", "createdAt", "completionDate", "dueDate");
        
        for (Sort.Order order : sort) {
            if (!validFields.contains(order.getProperty())) {
                log.warn("[VALIDATION FAILED] Invalid sort property attempted: {}", order.getProperty());
                throw new IllegalArgumentException("Invalid sorting properties: " + order.getProperty() + ", the sorting properties are: " + validFields);
            }
        }
    }
}
