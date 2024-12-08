package org.cresplanex.api.state.planservice.repository;

import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, String>, JpaSpecificationExecutor<TaskEntity> {

    /**
     * Taskを取得し、TaskAttachmentをJOINした状態で取得。
     *
     * @param taskId タスクID
     * @return Taskオプショナルオブジェクト
     */
    @Query("SELECT o FROM TaskEntity o LEFT JOIN FETCH o.taskAttachments WHERE o.taskId = :taskId")
    Optional<TaskEntity> findByIdWithAttachments(String taskId);

    /**
     * List<TaskId>の数を取得
     *
     * @param taskIds タスクIDリスト
     * @return タスクIDの数
     */
    Optional<Long> countByTaskIdIn(List<String> taskIds);
}
