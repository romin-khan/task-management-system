package com.romin.task.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(nullable = false)
    private String assignedBy;

    @Column(nullable = false)
    private String assignedTo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime completionDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @PrePersist
    public void init(){
        this.createdAt = LocalDateTime.now();
        this.status = TaskStatus.NOT_STARTED;
    }

     public Task(String title,
                String description,
                String assignedBy,
                String assignedTo,
                LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.dueDate = dueDate; 
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void markAsCompleted(){
        this.completionDate = LocalDateTime.now();
        this.status=TaskStatus.IS_COMPLETED;
    }

    public void extendDueDate(LocalDate extendedDueDate, TaskStatus status){
        if(!dueDate.isAfter(extendedDueDate)){
            throw new IllegalArgumentException("Extended due date should be after privious date");
        }
        if(status == TaskStatus.IS_COMPLETED){
            throw new IllegalStateException("Doesn't need to extend due date because task is completed");
        }
        if(status == TaskStatus.CANCELLED){
            throw new IllegalStateException("Coudn't able to extend due date because task is cancelled");
        }

        this.dueDate=extendedDueDate;
    }

    public void updateDescription(String updatedDescription){
        if(this.status != TaskStatus.NOT_STARTED){
            throw new IllegalStateException("Description cannot be changed after the task is excecuted");
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
        
        TaskStatus previousstatus;

        if(this.status == TaskStatus.CANCELLED){
            previousstatus = TaskStatus.CANCELLED;
        }
        else if(this.status == TaskStatus.NOT_STARTED){
            previousstatus = TaskStatus.NOT_STARTED;
        }
        else{
            previousstatus = TaskStatus.IN_PROGRESS;
        }

        this.status=TaskStatus.IS_COMPLETED;
        this.completionDate=LocalDateTime.now();

        return previousstatus;
    }

    public void cancelTask(){
            if(this.status == TaskStatus.IN_PROGRESS)
                throw new IllegalStateException("Cannot cancel the task which is in progres");
            
            if(this.status == TaskStatus.IS_COMPLETED)
                throw new IllegalStateException("Cannot cancel the task which is completed");

            if(this.status == TaskStatus.CANCELLED)
                throw new IllegalStateException("Task is already cancelled");
            
            this.status=TaskStatus.CANCELLED;
    }
}