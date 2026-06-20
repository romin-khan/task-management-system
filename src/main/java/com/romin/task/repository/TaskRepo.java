package com.romin.task.repository;

import com.romin.task.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepo extends JpaRepository<Task, Long>{
    
}
