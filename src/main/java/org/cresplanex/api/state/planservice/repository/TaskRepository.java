package org.cresplanex.api.state.planservice.repository;

import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.enums.TaskSortType;
import org.cresplanex.api.state.planservice.enums.TaskWithFileObjectsSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @Query("SELECT o FROM TaskEntity o WHERE o.taskId IN :taskIds ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC, " +
            "CASE WHEN :sortType = 'TITLE_ASC' THEN o.title END ASC, " +
            "CASE WHEN :sortType = 'TITLE_DESC' THEN o.title END DESC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_ASC' THEN o.dueDatetime END ASC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_DESC' THEN o.dueDatetime END DESC, " +
            "CASE WHEN :sortType = 'START_DATETIME_ASC' THEN o.startDatetime END ASC, " +
            "CASE WHEN :sortType = 'START_DATETIME_DESC' THEN o.startDatetime END DESC")
    List<TaskEntity> findListByTaskIds(List<String> taskIds, TaskSortType sortType);

    @Query("SELECT o FROM TaskEntity o LEFT JOIN FETCH o.taskAttachments WHERE o.taskId IN :taskIds ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC, " +
            "CASE WHEN :sortType = 'TITLE_ASC' THEN o.title END ASC, " +
            "CASE WHEN :sortType = 'TITLE_DESC' THEN o.title END DESC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_ASC' THEN o.dueDatetime END ASC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_DESC' THEN o.dueDatetime END DESC, " +
            "CASE WHEN :sortType = 'START_DATETIME_ASC' THEN o.startDatetime END ASC, " +
            "CASE WHEN :sortType = 'START_DATETIME_DESC' THEN o.startDatetime END DESC")
    List<TaskEntity> findListByTaskIdsWithAttachments(List<String> taskIds, TaskWithFileObjectsSortType sortType);

    @Query("SELECT o FROM TaskEntity o ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC, " +
            "CASE WHEN :sortType = 'TITLE_ASC' THEN o.title END ASC, " +
            "CASE WHEN :sortType = 'TITLE_DESC' THEN o.title END DESC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_ASC' THEN o.dueDatetime END ASC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_DESC' THEN o.dueDatetime END DESC, " +
            "CASE WHEN :sortType = 'START_DATETIME_ASC' THEN o.startDatetime END ASC, " +
            "CASE WHEN :sortType = 'START_DATETIME_DESC' THEN o.startDatetime END DESC")
    List<TaskEntity> findList(Specification<TaskEntity> specification, TaskSortType sortType);

    @Query("SELECT o FROM TaskEntity o ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC, " +
            "CASE WHEN :sortType = 'TITLE_ASC' THEN o.title END ASC, " +
            "CASE WHEN :sortType = 'TITLE_DESC' THEN o.title END DESC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_ASC' THEN o.dueDatetime END ASC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_DESC' THEN o.dueDatetime END DESC, " +
            "CASE WHEN :sortType = 'START_DATETIME_ASC' THEN o.startDatetime END ASC, " +
            "CASE WHEN :sortType = 'START_DATETIME_DESC' THEN o.startDatetime END DESC")
    List<TaskEntity> findList(Specification<TaskEntity> specification, TaskSortType sortType, Pageable pageable);

    @Query("SELECT o FROM TaskEntity o LEFT JOIN FETCH o.taskAttachments ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC, " +
            "CASE WHEN :sortType = 'TITLE_ASC' THEN o.title END ASC, " +
            "CASE WHEN :sortType = 'TITLE_DESC' THEN o.title END DESC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_ASC' THEN o.dueDatetime END ASC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_DESC' THEN o.dueDatetime END DESC, " +
            "CASE WHEN :sortType = 'START_DATETIME_ASC' THEN o.startDatetime END ASC, " +
            "CASE WHEN :sortType = 'START_DATETIME_DESC' THEN o.startDatetime END DESC")
    List<TaskEntity> findListWithAttachments(Specification<TaskEntity> specification, TaskWithFileObjectsSortType sortType);

    @Query("SELECT o FROM TaskEntity o LEFT JOIN FETCH o.taskAttachments ORDER BY " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN o.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN o.createdAt END DESC, " +
            "CASE WHEN :sortType = 'TITLE_ASC' THEN o.title END ASC, " +
            "CASE WHEN :sortType = 'TITLE_DESC' THEN o.title END DESC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_ASC' THEN o.dueDatetime END ASC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_DESC' THEN o.dueDatetime END DESC, " +
            "CASE WHEN :sortType = 'START_DATETIME_ASC' THEN o.startDatetime END ASC, " +
            "CASE WHEN :sortType = 'START_DATETIME_DESC' THEN o.startDatetime END DESC")
    List<TaskEntity> findListWithAttachments(Specification<TaskEntity> specification, TaskWithFileObjectsSortType sortType, Pageable pageable);

    @Query("SELECT COUNT(o) FROM TaskEntity o")
    int countList(Specification<TaskEntity> specification);
}
