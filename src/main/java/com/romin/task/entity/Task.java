package com.romin.task.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.romin.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Getter
@Entity
@Table(name = "tasks")
public class Task{

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

    @Column(name = "task_id", nullable = false)
    private String taskId;

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

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant completionDate;

    @NonNull
    @Column(nullable = false)
    private LocalDate dueDate;

    @PrePersist
    public void init(){
        this.createdAt = Instant.now();
        this.status = TaskStatus.NOT_STARTED;

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
    }

    public void markAsCompleted(){
        this.completionDate = Instant.now();
        this.status=TaskStatus.IS_COMPLETED;
    }

    public void extendDueDate(LocalDate extendedDueDate, TaskStatus status){
        if(dueDate.isAfter(extendedDueDate)){
            throw new IllegalArgumentException("Extended due date should be after previous date");
        }
        if(status == TaskStatus.IS_COMPLETED){
            throw new IllegalStateException("Doesn't need to extend due date because task is completed");
        }
        if(status == TaskStatus.CANCELLED){
            throw new IllegalStateException("couldn't able to extend due date because task is cancelled");
        }

        this.dueDate=extendedDueDate;
    }

    public void updateDescription(String updatedDescription){
        if(this.status != TaskStatus.NOT_STARTED){
            throw new IllegalStateException("Description cannot be changed after the task is executed");
        }

        this.description=updatedDescription;
    }
    
    public void startTask(){
        if(this.status == TaskStatus.IN_PROGRESS){
            throw new IllegalStateException("Task is already in progress");
        }
        if(this.status == TaskStatus.IS_COMPLETED){
            throw new IllegalStateException("Task is already completed");
        }
        if(this.status == TaskStatus.CANCELLED){
            throw new IllegalStateException("Task is cancelled, you does't want to start task");
        }

        this.status=TaskStatus.IN_PROGRESS;
    }

    public TaskStatus completeTask(){

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

        this.status=TaskStatus.IS_COMPLETED;
        this.completionDate=Instant.now();

        return previousStatus;
    }

    public void cancelTask(){
            if(this.status == TaskStatus.IN_PROGRESS)
                throw new IllegalStateException("Cannot cancel the task which is in progress");
            
            if(this.status == TaskStatus.IS_COMPLETED)
                throw new IllegalStateException("Cannot cancel the task which is completed");

            if(this.status == TaskStatus.CANCELLED)
                throw new IllegalStateException("Task is already cancelled");
            
            this.status=TaskStatus.CANCELLED;
    }
}