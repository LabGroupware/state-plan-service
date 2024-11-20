package org.cresplanex.api.state.planservice.specification;

import jakarta.persistence.criteria.Predicate;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.filter.task.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TaskSpecifications {

    public static Specification<TaskEntity> withTeamFilter(TeamFilter teamFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (teamFilter != null && teamFilter.isValid()) {
                if (teamFilter.getTeamIds() != null && !teamFilter.getTeamIds().isEmpty()) {
                    predicate = criteriaBuilder.and(predicate, root.get("teamId").in(teamFilter.getTeamIds()));
                }
            }
            return predicate;
        };
    }

    public static Specification<TaskEntity> withStatusFilter(StatusFilter statusFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (statusFilter != null && statusFilter.isValid()) {
                if (statusFilter.getStatuses() != null && !statusFilter.getStatuses().isEmpty()) {
                    predicate = criteriaBuilder.and(predicate, root.get("status").in(statusFilter.getStatuses()));
                }
            }
            return predicate;
        };
    }

    public static Specification<TaskEntity> withChargeUserFilter(ChargeUserFilter chargeUserFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (chargeUserFilter != null && chargeUserFilter.isValid()) {
                if (chargeUserFilter.getChargeUserIds() != null && !chargeUserFilter.getChargeUserIds().isEmpty()) {
                    predicate = criteriaBuilder.and(predicate, root.get("chargeUserId").in(chargeUserFilter.getChargeUserIds()));
                }
            }
            return predicate;
        };
    }

    public static Specification<TaskEntity> withAttachmentFileObjectsFilter(FileObjectsFilter fileObjectsFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (fileObjectsFilter != null && fileObjectsFilter.isValid()) {
                if (fileObjectsFilter.getFileObjectIds() != null && !fileObjectsFilter.getFileObjectIds().isEmpty()) {
                    if (!fileObjectsFilter.isAny()) {
                        // all
                        for (String fileObjectId : fileObjectsFilter.getFileObjectIds()) {
                            predicate = criteriaBuilder.and(predicate, criteriaBuilder.isMember(fileObjectId, root.get("taskAttachments")));
                        }
                    } else {
                        // any
                        predicate = criteriaBuilder.and(predicate, root.get("taskAttachments").get("fileObjectId").in(fileObjectsFilter.getFileObjectIds()));
                    }
                }
            }
            return predicate;
        };
    }

    public static Specification<TaskEntity> withStartDatetimeFilter(StartDatetimeFilter startDatetimeFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (startDatetimeFilter == null) {
                return null;
            }
            LocalDateTime earlierThan = null;
            LocalDateTime laterThan = null;
            boolean earlierInfinity = startDatetimeFilter.isEarlierInfinity();
            boolean laterInfinity = startDatetimeFilter.isLaterInfinity();

            if (!earlierInfinity && startDatetimeFilter.getEarlierThan() != null) {
                try {
                    earlierThan = LocalDateTime.parse(startDatetimeFilter.getEarlierThan());
                } catch (Exception e) {
                    earlierInfinity = true;
                }
            }
            if (!laterInfinity && startDatetimeFilter.getLaterThan() != null) {
                try {
                    laterThan = LocalDateTime.parse(startDatetimeFilter.getLaterThan());
                } catch (Exception e) {
                    laterInfinity = true;
                }
            }
            if (!earlierInfinity && !laterInfinity) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get("startDatetime"), earlierThan, laterThan));
            } else if (!earlierInfinity) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("startDatetime"), earlierThan));
            } else if (!laterInfinity) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("startDatetime"), laterThan));
            }

            return predicate;
        };
    }

    public static Specification<TaskEntity> withDueDatetimeFilter(DueDatetimeFilter dueDatetimeFilter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (dueDatetimeFilter == null) {
                return null;
            }
            LocalDateTime earlierThan = null;
            LocalDateTime laterThan = null;
            boolean earlierInfinity = dueDatetimeFilter.isEarlierInfinity();
            boolean laterInfinity = dueDatetimeFilter.isLaterInfinity();

            if (!earlierInfinity && dueDatetimeFilter.getEarlierThan() != null) {
                try {
                    earlierThan = LocalDateTime.parse(dueDatetimeFilter.getEarlierThan());
                } catch (Exception e) {
                    earlierInfinity = true;
                }
            }
            if (!laterInfinity && dueDatetimeFilter.getLaterThan() != null) {
                try {
                    laterThan = LocalDateTime.parse(dueDatetimeFilter.getLaterThan());
                } catch (Exception e) {
                    laterInfinity = true;
                }
            }
            if (!earlierInfinity && !laterInfinity) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get("dueDatetime"), earlierThan, laterThan));
            } else if (!earlierInfinity) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("dueDatetime"), earlierThan));
            } else if (!laterInfinity) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("dueDatetime"), laterThan));
            }

            return predicate;
        };
    }
}
