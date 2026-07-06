package com.romin.task.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.romin.user.entity.User;
import com.romin.user.enums.Position;
import com.romin.user.enums.Role;
import com.romin.user.enums.Status;

public class TaskEntityTest{

    private User user1;
    private User user2;
    private Task task;
    private String title;
    private String description;
    private LocalDate dueDate;

    @BeforeEach
    public void setUp() {

        title = "Bug fix";
        description = "Fix DB Indexing";
        dueDate = LocalDate.now();

        user1 = new User(
            1L,"USER-1","Romin",
            "7710912",Role.ADMIN,Position.CTO,
            Status.ACTIVE,"Romin@example.com","Mumbai",
            null,LocalDate.now(),null,
            null,null,null
        );

        user2 = new User(
            2L,"USER-2","Romin",
            "7710100",Role.ADMIN,Position.ENGINEERING_MANAGER,
            Status.ACTIVE,"Romin_123@example.com","Mumbai",
            null,LocalDate.now(),null,
            null,null,null
        );

        task = new Task(
            title,
            description,
            user1,
            user2,
            dueDate
        );
    }

    private void assertStateChanged(){
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(dueDate, task.getDueDate());
    }

    @Nested
    class UpdateTest{
        @Test
        public void update_WhenPassedAllValidFields_ShouldUpdateSuccessfully(){

            LocalDate updatedDueDate = LocalDate.now().plusDays(5);
            task.update(
                " Take interview ",
                " Take interview of new candidates for devOps ",
                updatedDueDate
            );
            assertEquals("Take interview", task.getTitle());
            assertEquals("Take interview of new candidates for devOps", task.getDescription());
            assertEquals(updatedDueDate, task.getDueDate());
        }
        @Test
        public void update_WhenPassedAllNullFields_ShouldNotChangeAnyStateOfTask(){

            task.update(null, null, null);
            assertStateChanged();
        }
        @Test
        public void update_WhenTaskIsCancelled_ShouldThrowException(){

            task.cancel();
            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> task.update("New Title", "New Description", LocalDate.now().plusDays(5))
            );
            assertEquals("Cannot modify a closed task (Completed/Cancelled).", exception.getMessage());
            assertStateChanged();
        }
        @Test
        public void update_WhenTaskIsCompleted_ShouldThrowException(){

            task.complete();
            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> task.update("New Title", "New Description", LocalDate.now().plusDays(5))
            );
            assertEquals("Cannot modify a closed task (Completed/Cancelled).", exception.getMessage());
            assertStateChanged();
        }
        @Test
        public void updateTitle_WhenPassedInvalidTitle_ShouldThrowException(){

            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> task.update("  ab  ", null, null)
            );
            assertEquals("Title must be at least 3 characters.", exception.getMessage());
            assertStateChanged();
        }
        @Test
        public void updateTitle_WhenPassedValidTitle_ShouldUpdateSuccessfully(){

            String expectedDescription = task.getDescription();
            LocalDate expectedDueDate = task.getDueDate();
            task.update(" New Title   ", null, null);
            assertEquals("New Title", task.getTitle());
            assertEquals(expectedDescription, task.getDescription());
            assertEquals(expectedDueDate, task.getDueDate());
        }
        @Test
        public void updateDescription_WhenPassedInvalidDescription_ShouldThrowException(){

            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> task.update(null, " ab ", null)
            );
            assertEquals("Description must be at least 5 characters.", exception.getMessage());
            assertStateChanged();
        }
        @Test
        public void updateDescription_WhenTaskIsInProgress_ShouldThrowException(){

            task.start();
            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> task.update(null, "Valid Description", null)
            );
            assertEquals("Description can only be changed if the task has not started.", exception.getMessage());
            assertStateChanged();
        }
        @Test
        public void updateDescription_WhenPassedValidDescription_ShouldUpdateSuccessfully(){

            String expectedTitle = task.getTitle();
            LocalDate expectedDueDate = task.getDueDate();
            task.update(null, "  New Description ", null);
            assertEquals(expectedTitle, task.getTitle());
            assertEquals("New Description", task.getDescription());
            assertEquals(expectedDueDate, task.getDueDate());
        }
        @Test
        public void update_WhenPassedInvalidDueDate_ShouldThrowException(){

            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> task.update(null, null, LocalDate.now().minusDays(3)
                )
            );
            assertEquals("New due date cannot be earlier than the current date.", exception.getMessage());
            assertStateChanged();
        }
        @Test
        public void updateDueDate_WhenPassedValidDueDate_ShouldUpdateSuccessfully(){

            String expectedTitle = task.getTitle();
            String expectedDescription = task.getDescription();
            LocalDate expectedDueDate = LocalDate.now().plusDays(5);
            task.update(null, null, expectedDueDate);
            assertEquals(expectedTitle, task.getTitle());
            assertEquals(expectedDescription, task.getDescription());
            assertEquals(expectedDueDate, task.getDueDate());
        }
    }
}