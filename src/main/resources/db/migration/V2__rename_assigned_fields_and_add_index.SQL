
ALTER TABLE tasks RENAME COLUMN assigned_by_id to assigned_by;
ALTER TABLE tasks RENAME COLUMN assigned_to_id to assigned_to;

CREATE INDEX idx_tasks_assigned_by ON tasks (assigned_by);