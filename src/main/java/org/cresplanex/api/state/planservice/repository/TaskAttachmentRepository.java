package org.cresplanex.api.state.planservice.repository;

import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.enums.FileObjectOnTaskSortType;
import org.cresplanex.api.state.planservice.enums.TaskOnFileObjectSortType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachmentEntity, String>, JpaSpecificationExecutor<TaskAttachmentEntity> {
    /**
     * 特定のtaskIdとfileObjectIdsに紐づくTaskAttachmentEntityのリストを取得。
     *
     * @param taskId タスクID
     * @param fileObjectIds ファイルオブジェクトIDリスト
     * @return TaskAttachmentEntityのリスト
     */
    @Query("SELECT ou FROM TaskAttachmentEntity ou WHERE ou.taskId = :taskId AND ou.fileObjectId IN :fileObjectIds")
    List<TaskAttachmentEntity> findAllByTaskIdAndAttachmentIds(String taskId, List<String> fileObjectIds);

    @Query("SELECT ou FROM TaskAttachmentEntity ou WHERE ou.taskId = :taskId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC")
    List<TaskAttachmentEntity> findFileObjectsListOnTask(Specification<TaskEntity> specification, String taskId, FileObjectOnTaskSortType sortType);

    @Query("SELECT ou FROM TaskAttachmentEntity ou WHERE ou.taskId = :taskId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC")
    List<TaskAttachmentEntity> findFileObjectsListOnTaskWithOffsetPagination(Specification<TaskEntity> specification, String taskId, FileObjectOnTaskSortType sortType, Pageable pageable);

    @Query("SELECT ou FROM TaskAttachmentEntity ou JOIN FETCH ou.task WHERE ou.fileObjectId = :fileObjectId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC, " +
            "CASE WHEN :sortType = 'TITLE_ASC' THEN ou.task.title END ASC, " +
            "CASE WHEN :sortType = 'TITLE_DESC' THEN ou.task.title END DESC, " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN ou.task.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN ou.task.createdAt END DESC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_ASC' THEN ou.task.dueDatetime END ASC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_DESC' THEN ou.task.dueDatetime END DESC, " +
            "CASE WHEN :sortType = 'START_DATETIME_ASC' THEN ou.task.startDatetime END ASC, " +
            "CASE WHEN :sortType = 'START_DATETIME_DESC' THEN ou.task.startDatetime END DESC")
    List<TaskAttachmentEntity> findTasksOnFileObject(Specification<TaskEntity> specification, String fileObjectId, TaskOnFileObjectSortType sortType);

    @Query("SELECT ou FROM TaskAttachmentEntity ou JOIN FETCH ou.task WHERE ou.fileObjectId = :fileObjectId ORDER BY " +
            "CASE WHEN :sortType = 'ADD_AT_ASC' THEN ou.createdAt END ASC, " +
            "CASE WHEN :sortType = 'ADD_AT_DESC' THEN ou.createdAt END DESC, " +
            "CASE WHEN :sortType = 'TITLE_ASC' THEN ou.task.title END ASC, " +
            "CASE WHEN :sortType = 'TITLE_DESC' THEN ou.task.title END DESC, " +
            "CASE WHEN :sortType = 'CREATED_AT_ASC' THEN ou.task.createdAt END ASC, " +
            "CASE WHEN :sortType = 'CREATED_AT_DESC' THEN ou.task.createdAt END DESC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_ASC' THEN ou.task.dueDatetime END ASC, " +
            "CASE WHEN :sortType = 'DUE_DATETIME_DESC' THEN ou.task.dueDatetime END DESC, " +
            "CASE WHEN :sortType = 'START_DATETIME_ASC' THEN ou.task.startDatetime END ASC, " +
            "CASE WHEN :sortType = 'START_DATETIME_DESC' THEN ou.task.startDatetime END DESC")
    List<TaskAttachmentEntity> findTasksOnFileObjectWithOffsetPagination(Specification<TaskEntity> specification, String fileObjectId, TaskOnFileObjectSortType sortType, Pageable pageable);

    @Query("SELECT COUNT(ou) FROM TaskAttachmentEntity ou WHERE ou.taskId = :taskId")
    int countFileObjectsListOnTask(Specification<TaskEntity> specification, String taskId);

    @Query("SELECT COUNT(ou) FROM TaskAttachmentEntity ou WHERE ou.fileObjectId = :fileObjectId")
    int countTasksOnFileObject(Specification<TaskEntity> specification, String fileObjectId);
}
