package org.cresplanex.api.state.planservice.repository;

import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
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
}
