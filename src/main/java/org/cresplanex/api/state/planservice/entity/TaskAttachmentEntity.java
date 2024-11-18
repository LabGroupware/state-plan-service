package org.cresplanex.api.state.planservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.cresplanex.api.state.common.entity.BaseEntity;
import org.cresplanex.api.state.common.utils.OriginalAutoGenerate;
import org.hibernate.Hibernate;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task_attachments", indexes = {
        @Index(name = "task_attachments_task_id_index", columnList = "task_id"),
        @Index(name = "task_attachments_file_object_id_index", columnList = "file_object_id"),
        @Index(name = "task_attachments_task_id_file_object_id_index", columnList = "task_id, file_object_id", unique = true)
})
public class TaskAttachmentEntity extends BaseEntity<TaskAttachmentEntity> {

    @Override
    public void setId(String id) {
        this.taskAttachmentId = id;
    }

    @Override
    public String getId() {
        return this.taskAttachmentId;
    }

    @Id
    @OriginalAutoGenerate
    @Column(name = "task_attachment_id", length = 100, nullable = false, unique = true)
    private String taskAttachmentId;

    @Column(name = "task_id", length = 100, nullable = false,
            insertable = false, updatable = false)
    private String taskId;

    @Column(name = "file_object_id", length = 100, nullable = false)
    private String fileObjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @Override
    public TaskAttachmentEntity clone() {
        TaskAttachmentEntity cloned = super.clone();
        // FetchされているもしくはすでにSetされている場合のみクローンを作成する
        if (this.task != null && Hibernate.isInitialized(this.task)) {
            cloned.task = this.task.clone();
        }

        return cloned;
    }
}
