package com.romin.task.service;

import java.util.List;

import com.romin.task.dto.request.DescriptioRequest;
import com.romin.task.dto.request.DueDateRequest;
import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.entity.Task;
import com.romin.task.entity.TaskStatus;

import org.springframework.stereotype.Service;
import com.romin.task.exception.TaskNotFoundException;
import com.romin.task.repository.TaskRepo;

@Service
public class TaskService {

    private final TaskRepo repo;

    public TaskService(TaskRepo repo){
        this.repo = repo;
    }

    public ServiceResult<TaskResponseDto> createTask(TaskRequestDto request){

        String assignedBy = "SYSTEM_USER";

        Task task = new Task(request.getTitle(),
                             request.getDescription(),
                             assignedBy,
                             request.getassignedTo(),
                             request.getDueDate());
        repo.save(task);

        return new ServiceResult<>(
            "Task is created successsfully",
            mapToDto(task)
        );
    }

    
    public ServiceResult<Object> deleteTask(Long id){
        getTaskOrThrow(id);
        repo.deleteById(id);
        return new ServiceResult<>(
                        "Task deleted Successfully",
                        null
                    );
    }

    public ServiceResult<TaskResponseDto> updateDescription(DescriptioRequest request, 
                                             Long id){
        Task task = getTaskOrThrow(id);
        task.updateDescription(request.getDescription());

        repo.save(task);

        return new ServiceResult<>(
                        "Description updated succesfully",
                        mapToDto(task)
                    );
    }

    public ServiceResult<TaskResponseDto> extendDueDate(DueDateRequest request,
                                         Long id){
        Task task = getTaskOrThrow(id);
        task.extendDueDate(request.getDueDate(),task.getStatus());

        return new ServiceResult<>(
                        "Due date is extended successfully",
                        mapToDto(task)
                    );
    }
    
    public ServiceResult<TaskResponseDto> cancelTask(Long id){
        Task task = getTaskOrThrow(id);
        task.cancelTask();

        repo.save(task);

        return new ServiceResult<>(
                        "Task is cancelled",
                        mapToDto(task)
                    );
    }

    public ServiceResult<TaskResponseDto> startTask(Long id){
        Task task = getTaskOrThrow(id);
        task.startTask();

        return new ServiceResult<>(
                        "Task is started",
                        mapToDto(task)
                   );
    }

    public ServiceResult<TaskResponseDto> completeTask(Long id){
        Task task = getTaskOrThrow(id);
        TaskStatus priviousStatus = task.completeTask();

        String message;

        if(priviousStatus == TaskStatus.CANCELLED){
            message = "The task is cancelled, but now it is marked as completed";
        }else if(priviousStatus == TaskStatus.NOT_STARTED){
            message = "the task  is not started yet, but now marked as completed";
        }else{
            message = "the task is marked completed";
        }
        repo.save(task);
        return new ServiceResult<>(
                       message,
                       mapToDto(task)
                   );
    }

    public ServiceResult<TaskResponseDto> getTaskById(Long id){
        Task task = getTaskOrThrow(id);
        return new ServiceResult<>(
                        "Task fetched successfully",
                        mapToDto(task)
                    );
    }

    public ServiceResult<List<TaskResponseDto>> getAllTask(){
        return new ServiceResult<>(
                        "Tasks fetched successfully",
                        repo.findAll()
                            .stream()
                            .map(this::mapToDto)
                            .toList()
                    );
    }

    private Task getTaskOrThrow(Long id){
        return repo.findById(id)
                   .orElseThrow(
                       () -> new TaskNotFoundException("Task having id = "+id+" not found")
                   );
    }

    private TaskResponseDto mapToDto(Task task){
        return TaskResponseDto.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .description(task.getDescription())
                                .status(task.getStatus())
                                .assignedBy(task.getAssignedBy())
                                .assignedTo(task.getAssignedTo())
                                .createdAt(task.getCreatedAt())
                                .dueDate(task.getDueDate())
                                .completionDate(task.getCompletionDate())
                              .build();
    }
}
