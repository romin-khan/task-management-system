package com.romin.task.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.romin.task.entity.Task;

public interface TaskRepo extends JpaRepository<Task, Long>{

    @Query("SELECT t FROM Task t JOIN FETCH t.assignedBy JOIN FETCH t.assignedTo WHERE t.publicId = :publicId")
    Optional<Task> findByPublicId(@Param("publicId") UUID publicId);

    @Query(value = "SELECT t FROM Task t JOIN FETCH t.assignedBy JOIN FETCH t.assignedTo",
           countQuery = "SELECT count(t) FROM Task t")
    Page<Task> findAllTasksWithUsers(Pageable pageable);
}
