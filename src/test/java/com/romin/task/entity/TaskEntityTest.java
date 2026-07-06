package com.romin.task.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
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

    private void assertStateUnchanged(){
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

            assertStateUnchanged();
        }

        @Test
        public void update_WhenTaskIsCancelled_ShouldThrowException(){
            task.cancel();

            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> task.update("New Title", "New Description", LocalDate.now().plusDays(5))
            );

            assertEquals("Cannot modify a closed task (Completed/Cancelled).", exception.getMessage());
            assertStateUnchanged();
        }

        @Test
        public void update_WhenTaskIsCompleted_ShouldThrowException(){
            task.complete();

            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> task.update("New Title", "New Description", LocalDate.now().plusDays(5))
            );

            assertEquals("Cannot modify a closed task (Completed/Cancelled).", exception.getMessage());
            assertStateUnchanged();
        }

        @Test
        public void updateTitle_WhenPassedInvalidTitle_ShouldThrowException(){
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> task.update("  ab  ", null, null)
            );

            assertEquals("Title must be at least 3 characters.", exception.getMessage());
            assertStateUnchanged();
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
        public void updateTitle_WhenPassedBlankTitle_ShouldThrowException(){
            String currentTitle = task.getTitle();
            var ex = assertThrows(
                IllegalArgumentException.class,
                () -> task.update("     ", null, null)
            );

            assertEquals("Title must be at least 3 characters.", ex.getMessage());
            assertEquals(currentTitle, task.getTitle());
        }

        @Test
        public void updateDescription_WhenPassedInvalidDescription_ShouldThrowException(){
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> task.update(null, " ab ", null)
            );

            assertEquals("Description must be at least 5 characters.", exception.getMessage());
            assertStateUnchanged();
        }

        @Test
        public void updateDescription_WhenTaskIsInProgress_ShouldThrowException(){
            task.start();

            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> task.update(null, "Valid Description", null)
            );

            assertEquals("Description can only be changed if the task has not started.", exception.getMessage());
            assertStateUnchanged();
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
        public void updateTitle_WhenPassedBlankDescription_ShouldThrowException(){
            String currentDescription = task.getDescription();
            var ex = assertThrows(
                IllegalArgumentException.class,
                () -> task.update(null, "    ", null)
            );

            assertEquals("Description must be at least 5 characters.", ex.getMessage());
            assertEquals(currentDescription, task.getDescription());
        }

        @Test
        public void update_WhenPassedInvalidDueDate_ShouldThrowException(){
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> task.update(null, null, LocalDate.now().minusDays(3)
                )
            );

            assertEquals("New due date cannot be earlier than the current date.", exception.getMessage());
            assertStateUnchanged();
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

    @Nested
    class StartTest{

        @Test
        public void start_WhenTaskIsNotStarted_ShouldChangeStateToInProgress(){
            task.start();

            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        }

        @Test
        public void start_WhenTaskIsAlreadyInProgress_ShouldDoNothing(){
            task.start();
            task.start();

            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        }

        @Test
        public void start_WhenTaskIsCancelled_ShouldThrowException(){
            task.cancel();

            var ex = assertThrows(
                IllegalStateException.class,
                () -> task.start()
            );

            assertEquals("Cannot start a completed/cancelled task", ex.getMessage());
            assertEquals(TaskStatus.CANCELLED, task.getStatus());
        }

        @Test
        public void start_WhenTaskIsCompleted_ShouldThrowException(){
            task.complete();

            var ex = assertThrows(
                IllegalStateException.class,
                () -> task.start()
            );

            assertEquals("Cannot start a completed/cancelled task",ex.getMessage());
            assertEquals(TaskStatus.IS_COMPLETED, task.getStatus());
        }
    }

    @Nested
    class CompleteTest {

        @Test
        public void complete_WhenTaskIsNotStarted_ShouldChangeStateToCompletedAndSetCompletionDate() {
            task.complete();

            assertEquals(TaskStatus.IS_COMPLETED, task.getStatus());
            assertNotNull(task.getCompletionDate());
        }

        @Test
        public void complete_WhenTaskIsInProgress_ShouldChangeStateToCompletedAndSetCompletionDate() {
            task.start();

            task.complete();

            assertEquals(TaskStatus.IS_COMPLETED, task.getStatus());
            assertNotNull(task.getCompletionDate());
        }

        @Test
        public void complete_WhenTaskIsAlreadyCompleted_ShouldDoNothing() {
            task.complete();
            Instant firstCompletionDate = task.getCompletionDate();

            task.complete();

            assertEquals(TaskStatus.IS_COMPLETED, task.getStatus());
            assertEquals(firstCompletionDate, task.getCompletionDate());
        }

        @Test
        public void complete_WhenTaskIsCancelled_ShouldThrowException() {
            task.cancel();

            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> task.complete()
            );

            assertEquals("Cannot complete a cancelled task.", exception.getMessage());
            assertEquals(TaskStatus.CANCELLED, task.getStatus());
            assertNull(task.getCompletionDate());
        }
    }

    @Nested
    class CancelTest {

        @Test
        public void cancel_WhenTaskIsNotStarted_ShouldChangeStateToCancelled() {
            task.cancel();

            assertEquals(TaskStatus.CANCELLED, task.getStatus());
        }

        @Test
        public void cancel_WhenTaskIsInProgress_ShouldChangeStateToCancelled() {
            task.start();

            task.cancel();

            assertEquals(TaskStatus.CANCELLED, task.getStatus());
        }

        @Test
        public void cancel_WhenTaskIsAlreadyCancelled_ShouldDoNothing() {
            task.cancel();
            task.cancel();

            assertEquals(TaskStatus.CANCELLED, task.getStatus());
        }

        @Test
        public void cancel_WhenTaskIsCompleted_ShouldThrowException() {
            task.complete();

            Exception exception = assertThrows(
                IllegalStateException.class,
                () -> task.cancel()
            );

            assertEquals("Cannot cancel a completed task.", exception.getMessage());
            assertEquals(TaskStatus.IS_COMPLETED, task.getStatus());
            assertNotNull(task.getCompletionDate());
        }
    }

    @Nested
    class ConstructorTests {

        @Test
        public void constructor_WhenPassedValidFields_ShouldCreateTaskSuccessfully() {
            Task task = new Task(
                "Bug fix",
                "Fix DB indexing issue",
                user1,
                user2,
                dueDate
            );

            assertEquals("Bug fix", task.getTitle());
            assertEquals("Fix DB indexing issue", task.getDescription());
            assertSame(user1, task.getAssignedBy());
            assertSame(user2, task.getAssignedTo());
            assertEquals(dueDate, task.getDueDate());
            assertEquals(TaskStatus.NOT_STARTED, task.getStatus());
        }

        @Test
        public void constructor_WhenTitleAndDescriptionContainExtraSpaces_ShouldTrimAndCreateTask() {
            Task task = new Task(
                "  Bug fix  ",
                "  Fix DB indexing issue  ",
                user1,
                user2,
                dueDate
            );

            assertEquals("Bug fix", task.getTitle());
            assertEquals("Fix DB indexing issue", task.getDescription());
            assertSame(user1, task.getAssignedBy());
            assertSame(user2, task.getAssignedTo());
            assertEquals(dueDate, task.getDueDate());
            assertEquals(TaskStatus.NOT_STARTED, task.getStatus());
        }

        @Test
        public void constructor_WhenTitleIsNull_ShouldThrowException() {
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Task(null, "Valid description", user1, user2, dueDate)
            );

            assertEquals("Invalid title.", exception.getMessage());
        }

        @Test
        public void constructor_WhenTitleIsLessThanThreeCharactersAfterTrim_ShouldThrowException() {
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Task(" ab ", "Valid description", user1, user2, dueDate)
            );

            assertEquals("Invalid title.", exception.getMessage());
        }

        @Test
        public void constructor_WhenDescriptionIsNull_ShouldThrowException() {
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Task("Valid title", null, user1, user2, dueDate)
            );

            assertEquals("Invalid description.", exception.getMessage());
        }

        @Test
        public void constructor_WhenDescriptionIsLessThanFiveCharactersAfterTrim_ShouldThrowException() {
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Task("Valid title", " abcd ", user1, user2, dueDate)
            );

            assertEquals("Invalid description.", exception.getMessage());
        }

        @Test
        public void constructor_WhenAssignedByIsNull_ShouldThrowException() {
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Task("Valid title", "Valid description", null, user2, dueDate)
            );

            assertEquals("Users cannot be null.", exception.getMessage());
        }

        @Test
        public void constructor_WhenAssignedToIsNull_ShouldThrowException() {
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Task("Valid title", "Valid description", user1, null, dueDate)
            );

            assertEquals("Users cannot be null.", exception.getMessage());
        }

        @Test
        public void constructor_WhenDueDateIsNull_ShouldThrowException() {
            Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Task("Valid title", "Valid description", user1, user2, null)
            );

            assertEquals("Due date cannot be null.", exception.getMessage());
        }
    }
}