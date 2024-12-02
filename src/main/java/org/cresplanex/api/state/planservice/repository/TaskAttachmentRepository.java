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

    @Query("SELECT ou FROM TaskAttachmentEntity ou")
    List<TaskAttachmentEntity> findList(Specification<TaskAttachmentEntity> specification, Pageable pageable);

    @Query("SELECT ou FROM TaskAttachmentEntity ou JOIN FETCH ou.task")
    List<TaskAttachmentEntity> findListWithTask(Specification<TaskAttachmentEntity> specification, Pageable pageable);

    @Query("SELECT COUNT(ou) FROM TaskAttachmentEntity ou")
    int countList(Specification<TaskAttachmentEntity> specification);
}
