package com.romin.task.service;

import java.security.InvalidParameterException;
import java.util.UUID;

import com.romin.infra.dto.PaginatedResponse;
import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.request.UpdateRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.entity.Task;
import com.romin.task.exception.TaskNotFoundException;
import com.romin.task.mapper.TaskMapper;
import com.romin.task.repository.TaskRepo;
import com.romin.user.entity.User;
import com.romin.user.repository.UserRepo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponseDto createTask(TaskRequestDto request) {
        log.info("[SERVICE] Initiating task creation. AssignedBy: {}, AssignedTo: {}", request.assignedBy(), request.assignedTo());

        User assignedBy = userRepo.findById(request.assignedBy())
            .orElseThrow(() -> new InvalidParameterException("User not found with id: " + request.assignedBy()));
            
        User assignedTo = userRepo.findById(request.assignedTo())
            .orElseThrow(() -> new InvalidParameterException("User not found with id: " + request.assignedTo()));

        Task task = taskMapper.toEntity(request, assignedBy , assignedTo);
        Task savedTask = taskRepo.save(task);                     
        
        return taskMapper.toResponseDto(savedTask);
    }

    @Transactional
    public void deleteTaskByPublicId(UUID publicId) {
        log.info("[SERVICE] Processing delete task command. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        taskRepo.delete(task);
    }

    @Transactional
    public TaskResponseDto update(UpdateRequestDto request, UUID publicId) {
        log.info("[SERVICE] Executing partial update transaction. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        
        task.update(
            request.title(), 
            request.description(), 
            request.dueDate()
        );
        
        return taskMapper.toResponseDto(task);
    }
    
    @Transactional
    public TaskResponseDto cancelTask(UUID publicId) {
        log.info("[SERVICE] Executing cancel state transition. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        task.cancel();
        return taskMapper.toResponseDto(task);
    }

    @Transactional
    public TaskResponseDto startTask(UUID publicId) {
        log.info("[SERVICE] Executing start state transition. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        task.start();
        return taskMapper.toResponseDto(task);
    }

    @Transactional
    public TaskResponseDto completeTask(UUID publicId) {
        log.info("[SERVICE] Executing completion state transition. Public ID: {}", publicId);
        Task task = getTaskOrThrow(publicId);
        task.complete();
        return taskMapper.toResponseDto(task);
    }
    
    public TaskResponseDto getTaskById(UUID publicId) {
        return taskMapper.toResponseDto(getTaskOrThrow(publicId));
    }
    
    public PaginatedResponse<TaskResponseDto> getAllTask(@NonNull Pageable pageable) {
        Page<TaskResponseDto> response = taskRepo.findAll(pageable).map(taskMapper::toResponseDto);
        return PaginatedResponse.from(response);
    }

    private Task getTaskOrThrow(UUID publicId) {
        if (publicId == null) {
            throw new TaskNotFoundException("Task cannot be found because the provided ID is null");
        }
        return taskRepo.findByPublicId(publicId)
            .orElseThrow(() -> new TaskNotFoundException("Task having id = " + publicId + " not found"));
    }
}