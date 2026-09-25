package com.romin.task.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.romin.task.entity.Task;
import com.romin.task.entity.TaskStatus;

public class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Task> hasAssignedTo(UUID userID) {
        return (root, query, criteriaBuilder) -> {
            if(userID == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("assignedTo").get("userId"), userID);
        };
    }

    public static Specification<Task> hasAssignedBy(UUID userID) {
        return (root, query, criteriaBuilder) -> {
            if(userID == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("assignedBy").get("userId"), userID);
        };
    }

    public static Specification<Task> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> {
            if(title == null || title.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<Task> hasDescription(String description) {
        return (root, query, criteriaBuilder) -> {
            if(description == null || description.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }

    public static Specification<Task> hasDueDate(Instant dueDate) {
        return (root, query, criteriaBuilder) -> {
            if(dueDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("dueDate"), dueDate);
        };
    }

    public static Specification<Task> hasDueDateBefore(Instant dueDate) {
        return (root, query, criteriaBuilder) -> {
            if(dueDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("dueDate"), dueDate);
        };
    }

    public static Specification<Task> hasDueDateAfter(Instant dueDate) {
        return (root, query, criteriaBuilder) -> {
            if(dueDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("dueDate"), dueDate);
        };
    }

    public static Specification<Task> hasDueDateBetween(Instant startDate, Instant endDate) {
        return (root, query, criteriaBuilder) -> {
            if(startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            } else if(startDate == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), endDate);
            } else if(endDate == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), startDate);
            } else {
                return criteriaBuilder.between(root.get("dueDate"), startDate, endDate);
            }
        };
    }

    public static Specification<Task> hasCompletionDate(Instant completionDate) {
        return (root, query, criteriaBuilder) -> {
            if(completionDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("completionDate"), completionDate);
        };
    }

    public static Specification<Task> hasCompletionDateBefore(Instant completionDate) {
        return (root, query, criteriaBuilder) -> {
            if(completionDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("completionDate"), completionDate);
        };
    }

    public static Specification<Task> hasCompletionDateAfter(Instant completionDate) {
        return (root, query, criteriaBuilder) -> {
            if(completionDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("completionDate"), completionDate);
        };
    }

    public static Specification<Task> hasCompletionDateBetween(Instant startDate, Instant endDate) {
        return (root, query, criteriaBuilder) -> {
            if(startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            } else if(startDate == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("completionDate"), endDate);
            } else if(endDate == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("completionDate"), startDate);
            } else {
                return criteriaBuilder.between(root.get("completionDate"), startDate, endDate);
            }
        };
    }

    public static Specification<Task> completionDateIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("completionDate"));
    }

    public static Specification<Task> completionDateIsNotNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get("completionDate"));
    }
 }
