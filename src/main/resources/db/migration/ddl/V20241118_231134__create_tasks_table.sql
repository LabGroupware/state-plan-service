CREATE TABLE tasks (
        task_id VARCHAR(100) PRIMARY KEY,
        team_id VARCHAR(100) NOT NULL,
        charge_user_id VARCHAR(100),
        version INTEGER DEFAULT 0 NOT NULL,
        title VARCHAR(255) NOT NULL,
        description TEXT NOT NULL,
        status VARCHAR(50) NOT NULL,
        start_datetime TIMESTAMP NOT NULL,
        due_datetime TIMESTAMP NOT NULL,
        created_at TIMESTAMP NOT NULL,
        created_by varchar(50) NOT NULL,
        updated_at TIMESTAMP DEFAULT NULL,
        updated_by varchar(50) DEFAULT NULL
);

CREATE INDEX tasks_team_id_index ON tasks (team_id);
CREATE INDEX tasks_charge_user_id_index ON tasks (charge_user_id);
CREATE INDEX tasks_status_index ON tasks (status);
CREATE INDEX tasks_start_datetime_index ON tasks (start_datetime);
CREATE INDEX tasks_due_datetime_index ON tasks (due_datetime);