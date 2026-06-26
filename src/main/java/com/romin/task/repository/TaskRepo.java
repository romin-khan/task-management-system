package com.romin.task.repository;

import com.romin.task.entity.Task;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepo extends JpaRepository<Task, Long>{

    Optional<Task> findByPublicId(String publicId);
}
