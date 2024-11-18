package org.cresplanex.api.state.planservice.entity;

import jakarta.persistence.*;
import org.cresplanex.api.state.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.cresplanex.api.state.common.utils.OriginalAutoGenerate;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks", indexes = {
        @Index(name = "tasks_team_id_index", columnList = "team_id"),
        @Index(name = "tasks_charge_user_id_index", columnList = "charge_user_id"),
        @Index(name = "tasks_status_index", columnList = "status"),
        @Index(name = "tasks_start_datetime_index", columnList = "start_datetime"),
        @Index(name = "tasks_due_datetime_index", columnList = "duw_datetime")
})
public class TaskEntity extends BaseEntity<TaskEntity> {

    @Override
    public void setId(String id) {
        this.taskId = id;
    }

    @Override
    public String getId() {
        return this.taskId;
    }

    @Id
    @OriginalAutoGenerate
    @Column(name = "task_id", length = 100, nullable = false, unique = true)
    private String taskId;

    @Column(name = "team_id", length = 100, nullable = false)
    private String teamId;

    @Column(name = "charge_user_id", length = 100)
    private String chargeUserId;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    @Lob
    private String description;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "duw_datetime", nullable = false)
    private LocalDateTime dueDatetime;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAttachmentEntity> taskAttachments;

    @Override
    public TaskEntity clone() {
        TaskEntity cloned = super.clone();

        // FetchされているもしくはすでにSetされている場合のみクローンを作成する
        if (this.taskAttachments != null && Hibernate.isInitialized(this.taskAttachments)) {
            cloned.taskAttachments = this.taskAttachments.stream()
                    .map(TaskAttachmentEntity::clone)
                    .toList();
        }

        return cloned;
    }
}
