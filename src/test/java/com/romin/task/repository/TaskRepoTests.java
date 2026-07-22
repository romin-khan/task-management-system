package com.romin.task.repository;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.romin.task.entity.Task;
import com.romin.task.entity.TaskStatus;
import com.romin.user.entity.User;
import com.romin.user.enums.Position;
import com.romin.user.enums.Role;
import com.romin.user.enums.Status;
import com.romin.user.repository.UserRepo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepoTests {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("taskdb")
                    .withUsername("taskuser")
                    .withPassword("taskpass");
        
                    

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry){
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TaskRepo taskRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private EntityManager entityManager;

    private User user1;
    private User user2;
    private Task globalTask;
    private static final LocalDate DUE_DATE = LocalDate.of(2026, Month.JULY, 17);

    @BeforeAll
    static void beforeAll() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @BeforeEach
    void setUp(){
        user1 = new User(
            null,
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
            null,
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

        globalTask = new Task(
            "title",
            "description",
            user1,
            user2,
            DUE_DATE
        );
    }

    private Task persistTask() {

            userRepo.saveAndFlush(user1);
            userRepo.saveAndFlush(user2);

            Task saved = taskRepo.saveAndFlush(globalTask);

            entityManager.clear();

            return saved;
        }

        private Task reload(Task task) {
            return taskRepo.findById(task.getId())
                    .orElseThrow();
        }

    @Nested
    class Crud {

        @Test
        void save_WhenTaskIsValid_ShouldPersistTask() {

            Task saved = persistTask();

            Task loaded = reload(saved);

            assertNotNull(loaded.getId());

            assertNotNull(loaded.getTaskId());
            assertTrue(loaded.getTaskId().startsWith("TSK-"));

            assertNotNull(loaded.getPublicId());

            assertEquals("title", loaded.getTitle());
            assertEquals("description", loaded.getDescription());

            assertEquals(TaskStatus.NOT_STARTED, loaded.getStatus());

            assertEquals(DUE_DATE, loaded.getDueDate());

            assertEquals(0L, loaded.getVersion());

            assertNotNull(loaded.getCreatedAt());
            assertNotNull(loaded.getUpdatedAt());

            assertEquals(user1.getId(), loaded.getAssignedBy().getId());
            assertEquals(user2.getId(), loaded.getAssignedTo().getId());
        }

        @Test
        void findById_WhenTaskDoesNotExist_ShouldReturnEmpty() {

            assertTrue(taskRepo.findById(999999L).isEmpty());
        }

        @Test
        void update_WhenTaskModified_ShouldPersistChanges() {

            Task saved = persistTask();

            Task managedTask = reload(saved);

            Instant updatedDate = managedTask.getUpdatedAt();
            managedTask.update(
                    "Updated Title",
                    "Updated Description",
                    LocalDate.now().plusDays(5)
            );

            taskRepo.saveAndFlush(managedTask);

            entityManager.clear();

            Task updated = reload(saved);

            assertEquals("Updated Title", updated.getTitle());
            assertEquals("Updated Description", updated.getDescription());
            assertEquals(LocalDate.now().plusDays(5), updated.getDueDate());
            assertEquals(1L, updated.getVersion());
            assertNotEquals(updatedDate, updated.getUpdatedAt());
        }

        @Test
        void delete_WhenTaskExists_ShouldRemoveTask() {

            Task saved = persistTask();

            taskRepo.deleteById(saved.getId());

            taskRepo.flush();

            entityManager.clear();

            assertTrue(taskRepo.findById(saved.getId()).isEmpty());
        }
    }

    @Nested
    class CustomQuery {

        private void persistTasks(int count) {

            for (int i = 1; i <= count; i++) {

                User assigner = new User(
                        null,
                        "ASSIGNER-" + i,
                        "Assigner " + i,
                        "9000000" + i,
                        Role.ADMIN,
                        Position.CTO,
                        Status.ACTIVE,
                        "assigner" + i + "@mail.com",
                        "Mumbai",
                        null,
                        LocalDate.now(),
                        null,
                        null,
                        null,
                        null
                );

                User assignee = new User(
                        null,
                        "ASSIGNEE-" + i,
                        "Assignee " + i,
                        "8000000" + i,
                        Role.ADMIN,
                        Position.ENGINEERING_MANAGER,
                        Status.ACTIVE,
                        "assignee" + i + "@mail.com",
                        "Mumbai",
                        null,
                        LocalDate.now(),
                        null,
                        null,
                        null,
                        null
                );

                userRepo.save(assigner);
                userRepo.save(assignee);

                taskRepo.save(new Task(
                        "Task " + i,
                        "Description " + i,
                        assigner,
                        assignee,
                        DUE_DATE.plusDays(i)
                ));
            }

            taskRepo.flush();
            entityManager.clear();
        }

        @Test
        void findByPublicId_WhenTaskExists_ShouldReturnTaskWithFetchedUsers() {

            Task saved = persistTask();

            Task found = taskRepo.findByPublicId(saved.getPublicId())
                    .orElseThrow();

            assertEquals(saved.getId(), found.getId());
            assertEquals(saved.getPublicId(), found.getPublicId());
            assertEquals("title", found.getTitle());
            assertEquals("description", found.getDescription());

            PersistenceUnitUtil util =
                    entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

            assertTrue(util.isLoaded(found.getAssignedBy()));
            assertTrue(util.isLoaded(found.getAssignedTo()));

            assertNotNull(found.getAssignedBy());
            assertNotNull(found.getAssignedTo());
        }

        @Test
        void findByPublicId_WhenTaskDoesNotExist_ShouldReturnEmpty() {

            assertTrue(taskRepo.findByPublicId(UUID.randomUUID()).isEmpty());
        }

        @Test
        void findAllTasksWithUsers_WhenDatabaseIsEmpty_ShouldReturnEmptyPage() {

            Page<Task> page = taskRepo.findAllTasksWithUsers(PageRequest.of(0, 5));

            assertTrue(page.isEmpty());
            assertEquals(0, page.getTotalElements());
        }

        @Test
        void findAllTasksWithUsers_ShouldReturnTasksWithUsersFetchedAndCorrectCount() {

            persistTasks(12);

            Page<Task> page = taskRepo.findAllTasksWithUsers(PageRequest.of(0, 5));

            assertFalse(page.isEmpty());

            assertEquals(12, page.getTotalElements());

            assertEquals(5, page.getContent().size());

            PersistenceUnitUtil util =
                    entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

            page.getContent().forEach(tasks -> {

                assertTrue(util.isLoaded(tasks.getAssignedBy()));
                assertTrue(util.isLoaded(tasks.getAssignedTo()));

                assertNotNull(tasks.getAssignedBy());
                assertNotNull(tasks.getAssignedTo());
            });
        }
    }

    @Nested
    class Constraints {

        @Test
        void save_ShouldGenerateUniqueDatabaseIdentifiers() {

            userRepo.saveAndFlush(user1);
            userRepo.saveAndFlush(user2);

            Task first = taskRepo.saveAndFlush(
                    new Task(
                            "Task 1",
                            "Description 1",
                            user1,
                            user2,
                            DUE_DATE
                    )
            );

            Task second = taskRepo.saveAndFlush(
                    new Task(
                            "Task 2",
                            "Description 2",
                            user1,
                            user2,
                            DUE_DATE.plusDays(1)
                    )
            );

            assertNotEquals(first.getId(), second.getId());
            assertNotEquals(first.getTaskId(), second.getTaskId());
            assertNotEquals(first.getPublicId(), second.getPublicId());
        }

        @Test
        void save_WhenInserted_ShouldInitializeAuditFields() {

            Task saved = persistTask();

            Task loaded = reload(saved);

            assertNotNull(loaded.getCreatedAt());
            assertNotNull(loaded.getUpdatedAt());

            assertEquals(
                    loaded.getCreatedAt(),
                    loaded.getUpdatedAt()
            );
        }

        @Test
        void deleteAssignedBy_WhenUserReferencedByTask_ShouldThrowException() {

            persistTask();

            userRepo.delete(user1);
            
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> {
                        userRepo.flush();
                    }
            );
        }

        @Test
        void deleteAssignedTo_WhenUserReferencedByTask_ShouldThrowException() {

            persistTask();

            userRepo.delete(user2);

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> {
                        userRepo.flush();
                    }
            );
        }
    }
}

