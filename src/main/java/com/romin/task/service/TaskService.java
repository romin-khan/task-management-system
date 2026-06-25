package com.romin.task.service;

import java.security.InvalidParameterException;

import com.romin.infra.dto.PaginatedResponse;
import com.romin.task.dto.request.DescriptionRequest;
import com.romin.task.dto.request.DueDateRequest;
import com.romin.task.dto.request.TaskRequestDto;
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

        Long authorId = request.getAssignedBy();
        Long targetId = request.getAssignedTo();

        User assignedBy = userRepo.findById(authorId)
                                        .orElseThrow(
                                            () -> new InvalidParameterException("User not found of id "+authorId)
                                        );
        User assignedTo = userRepo.findById(targetId)
                                        .orElseThrow(
                                            () -> new InvalidParameterException("User not found of id "+targetId) 
                                        );

        Task task = new Task(request.getTitle(),
                             request.getDescription(),
                             assignedBy,
                             assignedTo,
                             request.getDueDate());

        taskRepo.save(task);                     

        return mapToDto(task);
    }

    @SuppressWarnings("null")
    @Transactional
    public void deleteTask(Long id){
        Task task = getTaskOrThrow(id);
        taskRepo.delete(task);
    }

    @Transactional
    public TaskResponseDto updateDescription(DescriptionRequest request, 
                                             Long id){
        Task task = getTaskOrThrow(id);
        task.updateDescription(request.getDescription());

        return mapToDto(task);
    }

    @Transactional
    public TaskResponseDto extendDueDate(DueDateRequest request,
                                         Long id){
        Task task = getTaskOrThrow(id);
        task.extendDueDate(request.getDueDate(),task.getStatus());

        return mapToDto(task);
    }
    
    @Transactional
    public TaskResponseDto cancelTask(Long id){
        Task task = getTaskOrThrow(id);
        task.cancelTask();

        return mapToDto(task);
    }

    @Transactional
    public TaskResponseDto startTask(Long id){
        Task task = getTaskOrThrow(id);
        task.startTask();

        return mapToDto(task);
    }

    @Transactional
    public TaskResponseDto completeTask(Long id){
        Task task = getTaskOrThrow(id);
        task.completeTask();

        return mapToDto(task);
    }
    
    public TaskResponseDto getTaskById(Long id){
        Task task = getTaskOrThrow(id);
        
        return mapToDto(task);
    }
    
    public PaginatedResponse<TaskResponseDto> getAllTask(@NonNull Pageable pageable){
        Page<TaskResponseDto> response = taskRepo.findAll(pageable).map(this::mapToDto);
        return PaginatedResponse.from(response);
    }

    private Task getTaskOrThrow(Long id){
        if (id == null) {
            throw new TaskNotFoundException("Task cannot be found because the provided ID is null");
        }
        return taskRepo.findById(id)
                   .orElseThrow(
                       () -> new TaskNotFoundException("Task having id = "+id+" not found")
                   );
    }

    private TaskResponseDto mapToDto(Task task){
        return TaskResponseDto.builder()
                              .assignedBy(task.getAssignedBy().getId())
                              .assignedTo(task.getAssignedTo().getId())
                              .completionDate(task.getCompletionDate())
                              .createdAt(task.getCreatedAt())
                              .description(task.getDescription())
                              .dueDate(task.getDueDate())
                              .taskId(task.getTaskId())
                              .status(task.getStatus())
                              .title(task.getTitle())
                              .build();
    }
}
