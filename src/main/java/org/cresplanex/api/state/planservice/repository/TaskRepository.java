package org.cresplanex.api.state.planservice.repository;

import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, String> {

    /**
     * Taskを取得し、TaskAttachmentをJOINした状態で取得。
     *
     * @param taskId タスクID
     * @return Taskオプショナルオブジェクト
     */
    @Query("SELECT o FROM TaskEntity o LEFT JOIN FETCH o.taskAttachments WHERE o.taskId = :taskId")
    Optional<TaskEntity> findByIdWithAttachments(String taskId);

    /**
     * List<TaskId>を取得し, TaskAttachmentをJOINした状態で取得。
     *
     * @param taskIds タスクIDリスト
     * @return TaskEntityのリスト
     */
    @Query("SELECT o FROM TaskEntity o LEFT JOIN FETCH o.taskAttachments WHERE o.taskId IN :taskIds")
    List<TaskEntity> findAllByIdWithAttachments(List<String> taskIds);

    /**
     * Team IDでTaskを取得。
     *
     * @param teamId チームID
     * @return Taskのリスト
     */
    List<TaskEntity> findByTeamId(String teamId);

    /**
     * Charge User IDでTaskを取得。
     *
     * @param chargeUserId 担当者ID
     * @return Taskのリスト
     */
    List<TaskEntity> findByChargeUserId(String chargeUserId);

    /**
     * List<TaskId>の数を取得
     *
     * @param taskIds タスクIDリスト
     * @return タスクIDの数
     */
    Optional<Long> countByTaskIdIn(List<String> taskIds);

    /**
     * List<TaskId>をdue_datetimeでソートして取得
     *
     * @param taskIds タスクIDリスト
     * @return TaskEntityのリスト
     */
    List<TaskEntity> findAllByTaskIdInOrderByDueDatetime(List<String> taskIds);
}
