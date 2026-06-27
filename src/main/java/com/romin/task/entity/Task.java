package com.romin.task.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.romin.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Getter
@Entity
@Table(
    name = "tasks",
    indexes = {
        @Index(name = "idx_tasks_public_id", columnList = "publicId")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tasks_public_id", columnNames = {"public_id"}),
        @UniqueConstraint(name = "uk_tasks_task_id", columnNames = {"task_id"})
    }

)
public class Task {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "task_id_generator"
    )
    @SequenceGenerator(
        name = "task_id_generator",
        sequenceName = "task_generator",
        initialValue = 1,
        allocationSize = 1
    )
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "task_id", nullable = false, updatable = false, length = 50)
    private String taskId;

    @Column(name = "public_id", nullable = false, updatable = false, length = 36)
    private String publicId;

    @NonNull
    @Column(nullable = false, length = 100)
    private String title;

    @NonNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "assigned_by_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tasks_assigned_by_user"),
        referencedColumnName = "id"
    )
    private User assignedBy;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "assigned_to_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tasks_assigned_to_user"),
        referencedColumnName = "id"
    )
    private User assignedTo;

    @Column(name = "completion_date", nullable = true)
    private Instant completionDate;

    @NonNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @PrePersist
    public void init(){
        this.status = TaskStatus.NOT_STARTED;
        this.publicId = UUID.randomUUID().toString();

        int currentYear = LocalDate.now().getYear();
        String deptName = "TECH";
        String taskType = "BUG";
        String uniqueSubSuffix = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        
        this.taskId = String.format("TSK-%d-%s-%s-%d-%s",
                             currentYear,
                             deptName,
                             taskType,
                             this.assignedBy.getId(),
                             uniqueSubSuffix);
        
        log.info("[DOMAIN LCA] Lifecycle PrePersist hook executed. Generated publicId: {}, businessId: {}", 
                 this.publicId, this.taskId);
    }

    public void markAsCompleted(){
        log.info("[DOMAIN ACT] Explicit marking complete command invoked. Business ID: {}", this.taskId);
        this.completionDate = Instant.now();
        this.status = TaskStatus.IS_COMPLETED;
    }

    public void update(String newDescription, LocalDate newDueDate, String newTitle){
        log.info("[DOMAIN MUT] Processing update validation loop. Current Status: {}, Business ID: {}", this.status, this.taskId);
        
        if(this.status == TaskStatus.IS_COMPLETED){
            log.warn("[MUTATION REJECTED] Cannot modify a task that is already completed. Business ID: {}", this.taskId);
            throw new IllegalStateException("Cannot modify a task that is already completed.");
        }
        if(this.status == TaskStatus.CANCELLED){
            log.warn("[MUTATION REJECTED] Cannot modify a cancelled task. Business ID: {}", this.taskId);
            throw new IllegalStateException("Cannot modify a cancelled task.");
        }

        if(newTitle != null){
            String trimmedTitle = newTitle.trim();
            log.debug("[DOMAIN MUT] Evaluating title payload string properties.");
            
            if (trimmedTitle.isEmpty() || trimmedTitle.length() < 3) {
                log.warn("[MUTATION REJECTED] Title payload criteria breach. Input length under threshold.");
                throw new IllegalArgumentException("Title cannot be blank or shorter than 3 characters.");
            }
            this.title = trimmedTitle;
        }
        
        if(newDescription != null){
            log.debug("[DOMAIN MUT] Evaluating description phase adjustments against active lifecycle.");
            if(this.status != TaskStatus.NOT_STARTED){
                log.warn("[MUTATION REJECTED] Description changes prohibited once task steps away from NOT_STARTED. Status: {}", this.status);
                throw new IllegalStateException("Description cannot be changed after the task has started.");
            }

            String trimmedDescription = newDescription.trim();

            if (trimmedDescription.isEmpty() || trimmedDescription.length() < 5) {
                log.warn("[MUTATION REJECTED] Description length check failed. Provided input text is too short.");
                throw new IllegalArgumentException("Description cannot be blank or shorter than 5 characters.");
            }
            this.description = trimmedDescription;
        }
        
        if (newDueDate != null) {
            log.debug("[DOMAIN MUT] Evaluating target calendar date sequence adjustments. Current: {}, Proposed: {}", this.dueDate, newDueDate);
            if (this.dueDate.isAfter(newDueDate)) {
                log.warn("[MUTATION REJECTED] Due date parameters must represent sequential calendar dates.");
                throw new IllegalArgumentException("Extended due date must be after the current due date.");
            }
            this.dueDate = newDueDate;
        }
        log.info("[DOMAIN MUT] All internal domain validation checks cleared. Modifications successfully applied.");
    }
    
    public void startTask(){
        log.info("[DOMAIN ACT] Processing start task action execution loop. Business ID: {}", this.taskId);
        if(this.status == TaskStatus.IN_PROGRESS){
            throw new IllegalStateException("Task is already in progress");
        }
        if(this.status == TaskStatus.IS_COMPLETED){
            throw new IllegalStateException("Task is already completed");
        }
        if(this.status == TaskStatus.CANCELLED){
            throw new IllegalStateException("Task is cancelled, you does't want to start task");
        }

        this.status = TaskStatus.IN_PROGRESS;
        log.info("[DOMAIN ACT] Status transition complete. State updated to IN_PROGRESS.");
    }

    public TaskStatus completeTask(){
        log.info("[DOMAIN ACT] Processing task completion transition sequence. Business ID: {}", this.taskId);
        if(this.status == TaskStatus.IS_COMPLETED){
            throw new IllegalStateException("Task is already completed");
        }
        
        TaskStatus previousStatus;

        if(this.status == TaskStatus.CANCELLED){
            previousStatus = TaskStatus.CANCELLED;
        }
        else if(this.status == TaskStatus.NOT_STARTED){
            previousStatus = TaskStatus.NOT_STARTED;
        }
        else{
            previousStatus = TaskStatus.IN_PROGRESS;
        }

        this.status = TaskStatus.IS_COMPLETED;
        this.completionDate = Instant.now();
        log.info("[DOMAIN ACT] Status transition complete. State locked to IS_COMPLETED.");

        return previousStatus;
    }

    public void cancelTask(){
        log.info("[DOMAIN ACT] Processing structural cancel instructions block. Business ID: {}", this.taskId);
        if(this.status == TaskStatus.IN_PROGRESS)
            throw new IllegalStateException("Cannot cancel the task which is in progress");
        
        if(this.status == TaskStatus.IS_COMPLETED)
            throw new IllegalStateException("Cannot cancel the task which is completed");

        if(this.status == TaskStatus.CANCELLED)
            throw new IllegalStateException("Task is already cancelled");
        
        this.status = TaskStatus.CANCELLED;
        log.info("[DOMAIN ACT] Status transition complete. State dropped to CANCELLED.");
    }
}
