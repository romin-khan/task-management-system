package com.romin.task.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.entity.Task;
import com.romin.task.entity.TaskStatus;
import com.romin.user.entity.User;
import com.romin.user.enums.Position;
import com.romin.user.enums.Role;
import com.romin.user.enums.Status;

class TaskMapperTest{

    private User user1;
    private User user2;
    private TaskMapperImpl mapper;

    @BeforeEach
    void setUp(){
        mapper = new TaskMapperImpl();

        user1 = new User(
            1L,
            "USER-1",
            "Romin",
            "7710912",
            Role.ADMIN,
            Position.CTO,
            Status.ACTIVE,
            "Romin@example.com",
            "Mumbai",
            null,
            LocalDate.now(),
            null,
            null,
            null,
            null
        );

        user2 = new User(
            2L,
            "USER-2",
            "Romin",
            "7710100",
            Role.ADMIN,
            Position.ENGINEERING_MANAGER,
            Status.ACTIVE,
            "Romin_123@example.com",
            "Mumbai",
            null,
            LocalDate.now(),
            null,
            null,
            null,
            null
        );
    }
    
    @Test
    void toEntity_WhenPassedValidRequestAndUsers_ShouldReturnMappedTask(){
        LocalDate dueDate = LocalDate.now();
        TaskRequestDto request = new TaskRequestDto(
            "Title",
            "Description",
            1L,
            2L,
            dueDate
        );

        Task task = mapper.toEntity(request, user1, user2);

        assertEquals("Title", task.getTitle());
        assertEquals("Description", task.getDescription());
        assertEquals(user1, task.getAssignedBy());
        assertEquals(user2, task.getAssignedTo());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(TaskStatus.NOT_STARTED, task.getStatus());
    }

    @Test
    void toResponse_WhenPassedValidEntity_ShouldReturnResponseDto(){
        LocalDate dueDate = LocalDate.now();
        Task task = new Task(
            "Title",
            "Description",
            user1,
            user2,
            dueDate
        );

        TaskResponseDto response = mapper.toResponseDto(task);

        assertEquals(task.getPublicId(), response.publicId());
        assertEquals(task.getTaskId(), response.taskId());
        assertEquals("Title", response.title());
        assertEquals("Description", response.description());
        assertEquals(1L, response.assignedBy());
        assertEquals(2L, response.assignedTo());
        assertEquals(dueDate, response.dueDate());
        assertEquals(TaskStatus.NOT_STARTED, response.status());
        assertEquals(task.getCreatedAt(), response.createdAt());
        assertEquals(task.getUpdatedAt(), response.updatedAt());
        assertEquals(task.getCompletionDate(), response.completionDate());
    }
}