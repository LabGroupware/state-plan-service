CREATE TABLE task_attachments (
        task_attachment_id VARCHAR(100) PRIMARY KEY,
        task_id VARCHAR(100) NOT NULL,
        file_object_id VARCHAR(100) NOT NULL,
        version INTEGER DEFAULT 0 NOT NULL,
        created_at TIMESTAMP NOT NULL,
        created_by varchar(50) NOT NULL,
        updated_at TIMESTAMP DEFAULT NULL,
        updated_by varchar(50) DEFAULT NULL
);

CREATE INDEX task_attachments_task_id_index ON task_attachments (task_id);
CREATE INDEX task_attachments_file_object_id_index ON task_attachments (file_object_id);
CREATE UNIQUE INDEX task_attachments_task_id_file_object_id_index ON task_attachments (task_id, file_object_id);

ALTER TABLE task_attachments ADD CONSTRAINT task_attachments_task_id_fk FOREIGN KEY (task_id) REFERENCES tasks (task_id) ON DELETE CASCADE;