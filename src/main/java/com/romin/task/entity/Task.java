package com.romin.task.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.generator.EventType;

import com.romin.infra.entity.BaseAuditEntity;
import com.romin.user.entity.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
    name = "tasks",
    indexes = {
        @Index(name = "idx_tasks_public_id", columnList = "public_id"),
        @Index(name = "idx_tasks_assigned_to", columnList = "assigned_to"),
        @Index(name = "idx_tasks_assigned_by", columnList = "assigned_by")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tasks_public_id", columnNames = {"public_id"}),
        @UniqueConstraint(name = "uk_tasks_task_id", columnNames = {"task_id"})
    }
)
public class Task extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_id_generator")
    @SequenceGenerator(name = "task_id_generator", sequenceName = "task_generator", allocationSize = 1)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Generated(event = EventType.INSERT)
    @Column(name = "task_id", nullable = false, updatable = false, insertable = false, length = 50)
    private String taskId;

    @Generated(event = EventType.INSERT)
    @Column(name = "public_id", nullable = false, updatable = false, insertable = false, length = 36)
    private UUID publicId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false, foreignKey = @ForeignKey(name = "fk_tasks_assigned_by_user"))
    private User assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = false, foreignKey = @ForeignKey(name = "fk_tasks_assigned_to_user"))
    private User assignedTo;

    @Column(name = "completion_date")
    private Instant completionDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    public Task(String title, String description, User assignedBy, User assignedTo, LocalDate dueDate) {
        if (title == null || title.trim().length() < 3) throw new IllegalArgumentException("Invalid title.");
        if (description == null || description.trim().length() < 5) throw new IllegalArgumentException("Invalid description.");
        if (assignedBy == null || assignedTo == null) throw new IllegalArgumentException("Users cannot be null.");
        if (dueDate == null) throw new IllegalArgumentException("Due date cannot be null.");

        this.title = title.trim();
        this.description = description.trim();
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.dueDate = dueDate;
         this.status = TaskStatus.NOT_STARTED;
    }

    @PrePersist
    protected void onPersist() {
        log.debug("[DB-LIFECYCLE] Persisting Task Aggregate: {}", this.taskId);
    }

    public void update(String newTitle, String newDescription, LocalDate newDueDate) {
        ensureTaskIsModifiable();
        
        if (newTitle != null) {
            String trimmedTitle = newTitle.trim();
            if (trimmedTitle.length() < 3) throw new IllegalArgumentException("Title must be at least 3 characters.");
            this.title = trimmedTitle;
        }
        
        if (newDescription != null) {
            if (this.status != TaskStatus.NOT_STARTED) {
                throw new IllegalStateException("Description can only be changed if the task has not started.");
            }
            String trimmedDescription = newDescription.trim();
            if (trimmedDescription.length() < 5) {
                throw new IllegalArgumentException("Description must be at least 5 characters.");
            }
            this.description = trimmedDescription;
        }
        
        if (newDueDate != null) {
            if (newDueDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("New due date cannot be earlier than the current date.");
            }
            this.dueDate = newDueDate;
        }
    }

    public void start() {
        if (this.status == TaskStatus.IN_PROGRESS) return; // Idempotent
        if (this.status != TaskStatus.NOT_STARTED) {
            throw new IllegalStateException("Cannot start a task that is " + this.status);
        }
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.status == TaskStatus.IS_COMPLETED) return; // Idempotent
        if (this.status == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete a cancelled task.");
        }
        
        this.status = TaskStatus.IS_COMPLETED;
        this.completionDate = Instant.now();
    }

    public void cancel() {
        if (this.status == TaskStatus.CANCELLED) return; // Idempotent
        if (this.status == TaskStatus.IS_COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed task.");
        }
        
        this.status = TaskStatus.CANCELLED;
    }

    private void ensureTaskIsModifiable() {
        if (this.status == TaskStatus.IS_COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot modify a closed task (Completed/Cancelled).");
        }
    }
}