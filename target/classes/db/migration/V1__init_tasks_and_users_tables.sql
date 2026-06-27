
CREATE SEQUENCE IF NOT EXISTS user_generator START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS task_generator START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS task_human_number_seq START WITH 1001 INCREMENT BY 1;

CREATE TYPE user_role_enum AS ENUM (
    'USER',
    'ADMIN'
);
CREATE TYPE user_position_enum AS ENUM (
    'CEO',
    'CTO',
    'ENGINEERING_MANAGER',
    'TEAM_LEADER',
    'SENIOR_ENGINEER',
    'JUNIOR_ENGINEER',
    'INTERN'
);
CREATE TYPE user_status_enum AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'IN_LEAVE'
);
CREATE TYPE task_status_enum AS ENUM (
    'NOT_STARTED',
    'IN_PROGRESS',
    'IS_COMPLETED',
    'CANCELLED'
);

CREATE TABLE users (
    id BIGINT NOT NULL DEFAULT nextval('user_generator'),
    user_identity_number VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role user_role_enum NOT NULL,
    position user_position_enum NOT NULL,
    user_status user_status_enum NOT NULL,
    user_email VARCHAR(100) NOT NULL,
    user_address VARCHAR(255) NOT NULL,
    joining_date DATE NOT NULL,
    resign_date DATE,
    manager_id BIGINT,
    
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_email UNIQUE (user_email),
    CONSTRAINT uk_identity UNIQUE (user_identity_number),
    CONSTRAINT uk_phone_number UNIQUE (phone_number),
    
    CONSTRAINT fk_user_manager FOREIGN KEY (manager_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_users_identity_number ON users (user_identity_number);

CREATE TABLE tasks (
    id BIGINT NOT NULL DEFAULT nextval('task_generator'),
    version BIGINT NOT NULL,
    
    public_id UUID NOT NULL DEFAULT gen_random_uuid(), 
    
    task_id VARCHAR(30) NOT NULL DEFAULT ('TSK-' || nextval('task_human_number_seq')),
    
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status task_status_enum NOT NULL, 
    assigned_by_id BIGINT NOT NULL,
    assigned_to_id BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITHOUT TIME ZONE,
    due_date DATE NOT NULL,
    
    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT uk_tasks_public_id UNIQUE (public_id),
    CONSTRAINT uk_tasks_task_id UNIQUE (task_id),
    
    CONSTRAINT fk_tasks_assigned_by_user FOREIGN KEY (assigned_by_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_tasks_assigned_to_user FOREIGN KEY (assigned_to_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_tasks_public_id ON tasks (public_id);
CREATE INDEX idx_tasks_assigned_to ON tasks (assigned_to_id);
