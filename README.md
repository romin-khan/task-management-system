# Task Management System

## Overview
A backend Task Management System built using Java, Spring Boot, and PostgreSQL. The project is designed using layered architecture and focuses on real-world business rules, task lifecycle management, and clean API design.

## Features
- Create, update, delete tasks
- Task lifecycle management (NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED)
- Strict business rule validation for state transitions
- Due date validation
- Global exception handling
- Standardized API response structure
- PostgreSQL database integration using Spring Data JPA

## Tech Stack
- Java
- Spring Boot
- Spring Data JPA (Hibernate)
- PostgreSQL
- Maven

## Architecture
The project follows a layered architecture:

Controller → Service → Repository → Entity

- Controller Layer: Handles HTTP requests and responses
- Service Layer: Contains business logic and validations
- Repository Layer: Handles database operations
- Entity Layer: Represents database models

## Database
- PostgreSQL is used as the primary database
- JPA/Hibernate is used for ORM mapping
- Entity relationships are managed using annotations

## API Response Format
All APIs follow a standard response structure:

{
  "status": 200,
  "message": "Success",
  "timestamp": "2026-01-01T12:00:00",
  "data": {}
}

## Business Rules
- A completed task cannot be modified
- A cancelled task cannot be extended or completed
- Invalid state transitions are restricted at service/entity level
- Due date must be logically valid

## Future Improvements
- Spring Security (JWT authentication)
- Role-based access control (Admin/User)
- Pagination and filtering
- Docker deployment
- Redis caching
- Microservices migration (if needed)

## Author
Romin Khan

Backend Developer (Java | Spring Boot | DSA)
