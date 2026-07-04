package com.romin.task.mapper;

import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.entity.Task;
import com.romin.task.entity.TaskStatus;
import com.romin.user.entity.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-04T03:06:06+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public TaskResponseDto toResponseDto(Task task) {
        if ( task == null ) {
            return null;
        }

        Long assignedBy = null;
        Long assignedTo = null;
        UUID publicId = null;
        String taskId = null;
        String title = null;
        String description = null;
        TaskStatus status = null;
        Instant createdAt = null;
        Instant updatedAt = null;
        LocalDate dueDate = null;
        Instant completionDate = null;

        assignedBy = taskAssignedById( task );
        assignedTo = taskAssignedToId( task );
        publicId = task.getPublicId();
        taskId = task.getTaskId();
        title = task.getTitle();
        description = task.getDescription();
        status = task.getStatus();
        createdAt = task.getCreatedAt();
        updatedAt = task.getUpdatedAt();
        dueDate = task.getDueDate();
        completionDate = task.getCompletionDate();

        TaskResponseDto taskResponseDto = new TaskResponseDto( publicId, taskId, title, description, status, assignedBy, assignedTo, createdAt, updatedAt, dueDate, completionDate );

        return taskResponseDto;
    }

    @Override
    public Task toEntity(TaskRequestDto request, User assignedBy, User assignedTo) {
        if ( request == null && assignedBy == null && assignedTo == null ) {
            return null;
        }

        String title = null;
        String description = null;
        LocalDate dueDate = null;
        if ( request != null ) {
            title = request.title();
            description = request.description();
            dueDate = request.dueDate();
        }

        User assignedBy1 = null;
        User assignedTo1 = null;

        Task task = new Task( title, description, assignedBy1, assignedTo1, dueDate );

        return task;
    }

    private Long taskAssignedById(Task task) {
        if ( task == null ) {
            return null;
        }
        User assignedBy = task.getAssignedBy();
        if ( assignedBy == null ) {
            return null;
        }
        Long id = assignedBy.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long taskAssignedToId(Task task) {
        if ( task == null ) {
            return null;
        }
        User assignedTo = task.getAssignedTo();
        if ( assignedTo == null ) {
            return null;
        }
        Long id = assignedTo.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
