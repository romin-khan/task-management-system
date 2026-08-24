# Task Management API

A Java-based task management backend built with Spring Boot, Spring Data JPA, PostgreSQL, and Flyway. The project follows a layered architecture and models a realistic task lifecycle with validation, persistence, auditing, and API-level error handling.

## Overview
This application manages task creation, assignment, update, lifecycle transitions, retrieval, and deletion. It is structured around a domain-driven approach where the `Task` entity owns task state transitions and validation rules, while the service layer orchestrates business operations and the repository handles persistence queries.

## Core Features
- Create and assign tasks to users
- Retrieve tasks by public ID or paginated collection
- Update task details and due date
- Transition task state across lifecycle stages
- Prevent invalid state transitions via domain validation
- Enforce database-level integrity for user-task references
- Use auditing timestamps for record creation and updates
- Handle API and business errors consistently through `ProblemDetail` responses
- Support integration tests with PostgreSQL Testcontainers

## Technology Stack
- Java 21
- Spring Boot 3.5.16
- Spring Web MVC
- Spring Data JPA
- Hibernate ORM
- PostgreSQL 16
- Flyway for schema migration
- Lombok
- MapStruct
- Maven
- JUnit 5 + Mockito
- Testcontainers

## Project Architecture
The application follows a modular layered design:

- Controller layer: exposes REST endpoints and validates request contracts
- Service layer: enforces business logic and coordinates repository access
- Repository layer: performs database operations and fetch-join queries
- Entity layer: holds the domain model and lifecycle behavior
- DTO layer: separates API contracts from persistence model
- Mapper layer: converts domain entities to DTOs and vice versa
- Exception layer: centralizes API error mapping and business validation messaging

## Domain Model
### Task entity
The `Task` aggregate represents a user-assigned work item and includes:
- task ID and public UUID
- title and description
- status enum
- assignedBy and assignedTo users
- due date and completion date
- audit timestamps (`createdAt`, `updatedAt`)
- optimistic locking via `@Version`

### Task status
The module uses the following lifecycle states:
- `NOT_STARTED`
- `IN_PROGRESS`
- `IS_COMPLETED`
- `CANCELLED`

Business rules enforce that completed and cancelled tasks cannot be modified in the same way as active tasks.

## Task Lifecycle Rules
The domain model validates the following rules:
- title must be meaningful
- description must not be empty or too short
- due date cannot be null and must be in the present or future for new/updated tasks
- completed tasks cannot be edited or cancelled
- cancelled tasks cannot be completed
- a task cannot be started unless it is still in the `NOT_STARTED` state
- update operations reject changes on closed tasks

## API Endpoints
The task module exposes the following main HTTP operations:

### Task collection
- `POST /tasks` — create a new task
- `GET /tasks` — list tasks with pagination

### Task by ID
- `GET /tasks/{publicId}` — fetch task by UUID
- `PATCH /tasks/{publicId}` — update task fields
- `DELETE /tasks/{publicId}` — delete task

### Task state transitions
- `PATCH /tasks/{publicId}/start`
- `PATCH /tasks/{publicId}/complete`
- `PATCH /tasks/{publicId}/cancel`

## Persistence and Data Access
The repository layer uses Spring Data JPA and custom JPQL queries to fetch tasks together with their assigned users.

### Included repository behavior
- find task by public UUID
- fetch task with both `assignedBy` and `assignedTo` relations joined
- retrieve paginated task collections with user data loaded eagerly for the response layer

Database constraints and relationships are enforced so that deleting a user that is still referenced by task assignments triggers a constraint violation.

## Validation and Error Handling
The project includes a task-specific global exception handler that translates domain and validation issues into structured `ProblemDetail` responses.

Examples of handled failures:
- task not found
- invalid arguments
- invalid state transitions
- validation failures on request payloads
- optimistic locking conflicts
- database integrity violations

## Testing Strategy
The project includes both unit and integration tests for the task module.

### Service tests
The service test suite validates:
- successful task creation
- missing user validation
- delete logic
- null and invalid public IDs
- update, cancel, start, and complete operations
- get-by-id and get-all behavior

### Repository tests
The repository test suite validates:
- task persistence and generation of identifiers
- audit fields initialization
- fetch joins for assigned users
- empty-page results
- pagination behavior
- database constraints on task-user references
- transactional deletion behavior

The repository tests use PostgreSQL Testcontainers to validate real persistence behavior under a containerized database.

## Project Structure
```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── romin/
│   │           ├── TaskManagementApplication.java
│   │           ├── infra/
│   │           ├── task/
│   │           └── user/
│   └── resources/
│       ├── application.properties
│       └── db/
│           └── migration/
└── test/
    └── java/
        └── com/
            └── romin/
                └── task/
```

## Local Setup
### Prerequisites
- Java 21+
- Maven
- Docker (required for Testcontainers integration tests)
- PostgreSQL (test profile uses containerized PostgreSQL automatically)

### Run the application
```bash
mvn spring-boot:run
```

### Run tests
```bash
mvn test
```

### Build project
```bash
mvn clean package
```

## Current State and Maturity
The task module is already structured as a proper backend domain module with:
- domain entities and validation
- repository persistence logic
- service orchestration
- controller endpoints
- DTO mapping
- test coverage

It is production-ready in structure, but still has room to mature further with advanced filtering, richer role-based ownership rules, and expanded integration coverage before the user module is built on the same pattern.

## Future Enhancements
Recommended next steps for the module:
- specification-based advanced filtering
- ownership and authorization checks
- user-specific task queries
- overdue and upcoming task views
- controller integration tests for HTTP error payloads
- audit trail and activity log support
- security and JWT-based authentication

## Author
Romin Khan

Java / Spring Boot Backend Developer
