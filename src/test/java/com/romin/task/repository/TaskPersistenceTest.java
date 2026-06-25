package com.romin.task.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import com.romin.task.entity.Task;
import com.romin.task.entity.TaskStatus;
import com.romin.user.entity.User;
import com.romin.user.enums.Position;
import com.romin.user.enums.Role;
import com.romin.user.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) 
class TaskPersistenceTest {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_ShouldTriggerPrePersistAndGenerateBusinessId_Natively() {
        User author = createTestUser("EMP-101", "Romin Khan", "romin@company.com", "1234567890");
        User target = createTestUser("EMP-102", "SDE Candidate", "candidate@company.com", "0987654321");
        
        entityManager.persist(author); 
        entityManager.persist(target);
        entityManager.flush(); 

        Task transientTask = new Task(
                "Fix Dirty Checking Bug",
                "Migrate infrastructure layers away from tutorial hell",
                author,
                target,
                LocalDate.now().plusDays(3)
        );

        Task persistedTask = taskRepo.save(transientTask);
        entityManager.flush(); 

        assertNotNull(persistedTask.getId()); 
        assertNotNull(persistedTask.getTaskId()); 
        assertEquals(TaskStatus.NOT_STARTED, persistedTask.getStatus()); 
        assertTrue(persistedTask.getTaskId().startsWith("TSK-2026-TECH-BUG-"));
    }

    private User createTestUser(String companyId, String name, String email, String phone) {
        return new User(
            null,
            companyId,
            name,
            phone, 
            Role.ADMIN,
            Position.CTO,
            Status.ACTIVE, 
            email,
            "Mumbai,India",
            null,
            LocalDate.now(),
            null,
            null,
            null,
            null
        );
    }
}
