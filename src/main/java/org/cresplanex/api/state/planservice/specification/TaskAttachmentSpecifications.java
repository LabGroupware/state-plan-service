package org.cresplanex.api.state.planservice.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
public class TaskAttachmentSpecifications {

    public static Specification<org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity> fetchTask() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) {
                return null;
            }
            if (Long.class != query.getResultType()) {
                root.fetch("task", JoinType.LEFT);
                query.distinct(true);
                return null;
            }

            return null;
        };
    }

    public static Specification<TaskAttachmentEntity> whereTaskId(String taskId) {
        String newTaskId = new StringJavaType().wrap(taskId, null);

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (taskId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("taskId"), newTaskId));
            }
            return predicate;
        };
    }

    public static Specification<TaskAttachmentEntity> whereFileObjectId(String fileObjectId) {
        String newFileObjectId = new StringJavaType().wrap(fileObjectId, null);

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (fileObjectId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("fileObjectId"), newFileObjectId));
            }
            return predicate;
        };
    }
}
