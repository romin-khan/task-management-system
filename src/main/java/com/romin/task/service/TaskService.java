package com.romin.task.service;

import java.security.InvalidParameterException;

import com.romin.infra.dto.PaginatedResponse;
import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.request.UpdateRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.entity.Task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.romin.task.exception.TaskNotFoundException;
import com.romin.task.repository.TaskRepo;
import com.romin.user.entity.User;
import com.romin.user.repository.UserRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepo taskRepo;
    private final UserRepo userRepo;

    public TaskService(TaskRepo taskRepo, UserRepo userRepo){
        this.taskRepo = taskRepo;
        this.userRepo = userRepo;
    }

    @SuppressWarnings("null")
    @Transactional
    public TaskResponseDto createTask(TaskRequestDto request){
        log.info("[SERVICE] Initiating task creation. AssignedBy: {}, AssignedTo: {}", request.getAssignedBy(), request.getAssignedTo());

        Long authorId = request.getAssignedBy();
        Long targetId = request.getAssignedTo();

        User assignedBy = userRepo.findById(authorId)
                                        .orElseThrow(() -> {
                                            log.warn("[VALIDATION FAILED] Task creation aborted. Author user not found. ID: {}", authorId);
                                            return new InvalidParameterException("User not found of id " + authorId);
                                        });
        User assignedTo = userRepo.findById(targetId)
                                        .orElseThrow(() -> {
                                            log.warn("[VALIDATION FAILED] Task creation aborted. Target user not found. ID: {}", targetId);
                                            return new InvalidParameterException("User not found of id " + targetId); 
                                        });

        Task task = new Task(request.getTitle(),
                             request.getDescription(),
                             assignedBy,
                             assignedTo,
                             request.getDueDate());

        taskRepo.save(task);                     
        log.info("[SERVICE] Task successfully saved to database.");

        return mapToDto(task);
    }

    @SuppressWarnings("null")
    @Transactional
    public void deleteTaskByPublicId(String publicId){
        log.info("[SERVICE] Processing delete task command. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        
        taskRepo.delete(task);
        log.info("[SERVICE] Task successfully purged from persistence layer. Public ID: {}", publicId);
    }

    @Transactional
    public TaskResponseDto update(UpdateRequestDto request, String publicId){
        log.info("[SERVICE] Executing partial update transaction. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        
        task.update(request.description(), request.dueDate(), request.title());
        log.info("[SERVICE] Task mutations applied successfully. Public ID: {}", publicId);
        return mapToDto(task);
    }
    
    @Transactional
    public TaskResponseDto cancelTask(String publicId){
        log.info("[SERVICE] Executing cancel state transition. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        
        task.cancelTask();
        log.info("[SERVICE] Task state switched to CANCELLED. Public ID: {}", publicId);
        return mapToDto(task);
    }

    @Transactional
    public TaskResponseDto startTask(String publicId){
        log.info("[SERVICE] Executing start state transition. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        
        task.startTask();
        log.info("[SERVICE] Task state switched to IN_PROGRESS. Public ID: {}", publicId);
        return mapToDto(task);
    }

    @Transactional
    public TaskResponseDto completeTask(String publicId){
        log.info("[SERVICE] Executing completion state transition. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        
        task.completeTask();
        log.info("[SERVICE] Task state switched to IS_COMPLETED. Public ID: {}", publicId);
        return mapToDto(task);
    }
    
    public TaskResponseDto getTaskById(String publicId){
        log.info("[SERVICE] Fetching individual task record by path token. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        
        return mapToDto(task);
    }
    
    public PaginatedResponse<TaskResponseDto> getAllTask(@NonNull Pageable pageable){
        log.info("[SERVICE] Retrieving paginated task collection. Page: {}, Size: {}, Sort: {}", 
                 pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
                 
        Page<TaskResponseDto> response = taskRepo.findAll(pageable).map(this::mapToDto);
        
        log.info("[SERVICE] Paginated task query execution complete. Total records found: {}", response.getTotalElements());
        return PaginatedResponse.from(response);
    }

    private Task getTaskOrThrow(String publicId){
        if (publicId == null) {
            log.warn("[VALIDATION FAILED] Query lookup rejected because the public identifier string is null.");
            throw new TaskNotFoundException("Task cannot be found because the provided ID is null");
        }
        
        log.debug("[REPOSITORY REQ] Executing database indexed lookup query for publicId: {}", publicId);
        return taskRepo.findByPublicId(publicId)
                   .orElseThrow(() -> {
                       log.warn("[NOT FOUND] No corresponding active record matches parameter publicId: {}", publicId);
                       return new TaskNotFoundException("Task having id = " + publicId + " not found");
                   });
    }

    private TaskResponseDto mapToDto(Task task){
        log.trace("[MAPPER] Translating core domain infrastructure entity objects into public response objects.");
        return TaskResponseDto.builder()
                              .publicId(task.getPublicId())
                              .taskId(task.getTaskId())
                              .assignedBy(task.getAssignedBy().getId())
                              .assignedTo(task.getAssignedTo().getId())
                              .completionDate(task.getCompletionDate())
                              .createdAt(task.getCreatedAt())
                              .description(task.getDescription())
                              .dueDate(task.getDueDate())
                              .status(task.getStatus())
                              .title(task.getTitle())
                              .updatedAt(task.getUpdatedAt())
                              .build();
    }
}
