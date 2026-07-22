package com.romin.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.romin.infra.dto.PaginatedResponse;
import com.romin.task.dto.request.TaskRequestDto;
import com.romin.task.dto.request.UpdateRequestDto;
import com.romin.task.dto.response.TaskResponseDto;
import com.romin.task.entity.Task;
import com.romin.task.exception.TaskNotFoundException;
import com.romin.task.mapper.TaskMapper;
import com.romin.task.repository.TaskRepo;
import com.romin.user.entity.User;
import com.romin.user.enums.Position;
import com.romin.user.enums.Role;
import com.romin.user.enums.Status;
import com.romin.user.repository.UserRepo;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepo taskRepo;

    @Mock
    TaskMapper taskMapper;

    @Mock
    UserRepo userRepo;

    @InjectMocks
    TaskService service;

    private User user1;
    private User user2;
    private TaskResponseDto expectedResponse;
    private Task task;
    private UUID id;

    @BeforeEach
    void setUp() {
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

        expectedResponse = mock(TaskResponseDto.class);
        task = mock(Task.class);
        id = UUID.randomUUID();
    }

    @Nested
    class CreateTaskTests {

        final TaskRequestDto request = new TaskRequestDto(
            "Test case",
            "Make test on service",
            1L,
            2L,
            LocalDate.now().plusDays(2)
        );

        @Test
        void createTask_WhenAssignerAndAssigneeIsPresent_ReturnResponseDto() {

            when(userRepo.findById(1L)).thenReturn(Optional.of(user1));
            when(userRepo.findById(2L)).thenReturn(Optional.of(user2));
            when(taskMapper.toEntity(request, user1, user2)).thenReturn(task);
            when(taskRepo.save(any(Task.class))).thenReturn(task);
            when(taskMapper.toResponseDto(task)).thenReturn(expectedResponse);

            TaskResponseDto actualResponse = service.createTask(request);

            assertEquals(expectedResponse, actualResponse);

            verify(userRepo).findById(1L);
            verify(userRepo).findById(2L);
            verify(taskMapper).toEntity(request, user1, user2);
            verify(taskRepo).save(task);
            verify(taskMapper).toResponseDto(task);
        }

        @Test
        void throw_WhenAssignerIsNotPresent() {

            when(userRepo.findById(1L)).thenReturn(Optional.empty());

            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createTask(request)
            );

            assertEquals("User not found with id: 1", exception.getMessage());

            verifyNoInteractions(taskRepo);
            verifyNoInteractions(taskMapper);
        }

        @Test
        void throw_WhenAssigneeIsNotPresent() {

            when(userRepo.findById(1L)).thenReturn(Optional.of(user1));
            when(userRepo.findById(2L)).thenReturn(Optional.empty());

            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createTask(request)
            );

            assertEquals("User not found with id: 2", exception.getMessage());

            verifyNoInteractions(taskRepo);
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    class DeleteTaskByPublicIdTests {

        @Test
        void deleteTask_WhenPassedAValidId() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.of(task));

            service.deleteTaskByPublicId(id);

            verify(taskRepo).findByPublicId(id);
            verify(taskRepo).delete(task);
        }

        @Test
        void throw_WhenPassedANullId() {

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.deleteTaskByPublicId(null)
            );

            assertEquals(
                "Task cannot be found because the provided ID is null",
                exception.getMessage()
            );

            verifyNoInteractions(taskRepo);
        }

        @Test
        void throw_WhenPassedAnInvalidId() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.empty());

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.deleteTaskByPublicId(id)
            );

            assertEquals(
                String.format("Task having id = %s not found", id),
                exception.getMessage()
            );

            verify(taskRepo).findByPublicId(id);
            verify(taskRepo, never()).delete(any());
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    class UpdateTaskTests {

        final UpdateRequestDto request = new UpdateRequestDto(
            "fix bug",
            "there is bug in redis instead of kafka",
            LocalDate.now()
        );

        @Test
        void updateTask_WhenTaskExists_UpdatesTaskAndReturnsResponse() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.of(task));
            when(taskMapper.toResponseDto(task)).thenReturn(expectedResponse);

            TaskResponseDto actualResponse = service.updateTask(request, id);

            assertEquals(expectedResponse, actualResponse);

            verify(taskRepo).findByPublicId(id);
            verify(task).update(
                request.title(),
                request.description(),
                request.dueDate()
            );
            verify(taskMapper).toResponseDto(task);
        }

        @Test
        void updateTask_WhenPublicIdIsNull_ThrowsTaskNotFoundException() {

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.updateTask(request, null)
            );

            assertEquals(
                "Task cannot be found because the provided ID is null",
                exception.getMessage()
            );

            verifyNoInteractions(taskRepo);
            verifyNoInteractions(taskMapper);
        }

        @Test
        void updateTask_WhenTaskNotFound_ThrowsTaskNotFoundException() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.empty());

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.updateTask(request, id)
            );

            assertEquals(
                String.format("Task having id = %s not found", id),
                exception.getMessage()
            );

            verify(taskRepo).findByPublicId(id);
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    class CancelTaskTests {

        @Test
        void cancelTask_WhenTaskExists_CancelsTaskAndReturnsResponse() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.of(task));
            when(taskMapper.toResponseDto(task)).thenReturn(expectedResponse);

            TaskResponseDto actualResponse = service.cancelTask(id);

            assertEquals(expectedResponse, actualResponse);

            verify(taskRepo).findByPublicId(id);
            verify(task).cancel();
            verify(taskMapper).toResponseDto(task);
        }

        @Test
        void cancelTask_WhenPublicIdIsNull_ThrowsTaskNotFoundException() {

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.cancelTask(null)
            );

            assertEquals(
                "Task cannot be found because the provided ID is null",
                exception.getMessage()
            );

            verifyNoInteractions(taskRepo);
            verifyNoInteractions(taskMapper);
        }

        @Test
        void cancelTask_WhenTaskNotFound_ThrowsTaskNotFoundException() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.empty());

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.cancelTask(id)
            );

            assertEquals(
                String.format("Task having id = %s not found", id),
                exception.getMessage()
            );

            verify(taskRepo).findByPublicId(id);
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    class StartTaskTests {

        @Test
        void startTask_WhenTaskExists_StartsTaskAndReturnsResponse() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.of(task));
            when(taskMapper.toResponseDto(task)).thenReturn(expectedResponse);

            TaskResponseDto actualResponse = service.startTask(id);

            assertEquals(expectedResponse, actualResponse);

            verify(taskRepo).findByPublicId(id);
            verify(task).start();
            verify(taskMapper).toResponseDto(task);
        }

        @Test
        void startTask_WhenPublicIdIsNull_ThrowsTaskNotFoundException() {

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.startTask(null)
            );

            assertEquals(
                "Task cannot be found because the provided ID is null",
                exception.getMessage()
            );

            verifyNoInteractions(taskRepo);
            verifyNoInteractions(taskMapper);
        }

        @Test
        void startTask_WhenTaskNotFound_ThrowsTaskNotFoundException() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.empty());

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.startTask(id)
            );

            assertEquals(
                String.format("Task having id = %s not found", id),
                exception.getMessage()
            );

            verify(taskRepo).findByPublicId(id);
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    class CompleteTaskTests {

        @Test
        void completeTask_WhenTaskExists_CompletesTaskAndReturnsResponse() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.of(task));
            when(taskMapper.toResponseDto(task)).thenReturn(expectedResponse);

            TaskResponseDto actualResponse = service.completeTask(id);

            assertEquals(expectedResponse, actualResponse);

            verify(taskRepo).findByPublicId(id);
            verify(task).complete();
            verify(taskMapper).toResponseDto(task);
        }

        @Test
        void completeTask_WhenPublicIdIsNull_ThrowsTaskNotFoundException() {

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.completeTask(null)
            );

            assertEquals(
                "Task cannot be found because the provided ID is null",
                exception.getMessage()
            );

            verifyNoInteractions(taskRepo);
            verifyNoInteractions(taskMapper);
        }

        @Test
        void completeTask_WhenTaskNotFound_ThrowsTaskNotFoundException() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.empty());

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.completeTask(id)
            );

            assertEquals(
                String.format("Task having id = %s not found", id),
                exception.getMessage()
            );

            verify(taskRepo).findByPublicId(id);
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    class GetTaskByIdTests {

        @Test
        void getTaskById_WhenTaskExists_ReturnsResponseDto() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.of(task));
            when(taskMapper.toResponseDto(task)).thenReturn(expectedResponse);

            TaskResponseDto actualResponse = service.getTaskById(id);

            assertEquals(expectedResponse, actualResponse);

            verify(taskRepo).findByPublicId(id);
            verify(taskMapper).toResponseDto(task);
        }

        @Test
        void getTaskById_WhenPublicIdIsNull_ThrowsTaskNotFoundException() {

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.getTaskById(null)
            );

            assertEquals(
                "Task cannot be found because the provided ID is null",
                exception.getMessage()
            );

            verifyNoInteractions(taskRepo);
            verifyNoInteractions(taskMapper);
        }

        @Test
        void getTaskById_WhenTaskNotFound_ThrowsTaskNotFoundException() {

            when(taskRepo.findByPublicId(id)).thenReturn(Optional.empty());

            Exception exception = assertThrows(
                TaskNotFoundException.class,
                () -> service.getTaskById(id)
            );

            assertEquals(
                String.format("Task having id = %s not found", id),
                exception.getMessage()
            );

            verify(taskRepo).findByPublicId(id);
            verifyNoInteractions(taskMapper);
        }
    }

    @Nested
    class GetAllTaskTests {

        @Test
        void getAllTask_WhenTasksExist_ReturnPaginatedResponse() {

            Pageable pageable = PageRequest.of(0, 5);

            Task task1 = mock(Task.class);
            Task task2 = mock(Task.class);

            TaskResponseDto response1 = mock(TaskResponseDto.class);
            TaskResponseDto response2 = mock(TaskResponseDto.class);

            Page<Task> taskPage = new PageImpl<>(List.of(task1, task2), pageable, 2);

            when(taskRepo.findAllTasksWithUsers(pageable)).thenReturn(taskPage);
            when(taskMapper.toResponseDto(task1)).thenReturn(response1);
            when(taskMapper.toResponseDto(task2)).thenReturn(response2);

            PaginatedResponse<TaskResponseDto> actualResponse = service.getAllTask(pageable);

            assertEquals(2, actualResponse.data().size());
            assertEquals(response1, actualResponse.data().get(0));
            assertEquals(response2, actualResponse.data().get(1));
            assertEquals(5, actualResponse.pageSize());
            assertEquals(2, actualResponse.totalElements());
            assertEquals(1, actualResponse.totalPages());

            verify(taskRepo).findAllTasksWithUsers(pageable);
            verify(taskMapper).toResponseDto(task1);
            verify(taskMapper).toResponseDto(task2);
        }

        @Test
        void getAllTask_WhenNoTasksExist_ReturnEmptyPaginatedResponse() {

            Pageable pageable = PageRequest.of(0, 5);
            Page<Task> emptyPage = Page.empty(pageable);

            when(taskRepo.findAllTasksWithUsers(pageable)).thenReturn(emptyPage);

            PaginatedResponse<TaskResponseDto> actualResponse = service.getAllTask(pageable);

            assertEquals(0, actualResponse.data().size());
            assertEquals(5, actualResponse.pageSize());
            assertEquals(0, actualResponse.totalElements());
            assertEquals(0, actualResponse.totalPages());

            verify(taskRepo).findAllTasksWithUsers(pageable);
            verifyNoInteractions(taskMapper);
        }
    }
}