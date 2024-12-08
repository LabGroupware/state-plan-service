package org.cresplanex.api.state.planservice.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.filter.task.*;
import org.hibernate.type.descriptor.java.StringJavaType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskSpecifications {

    public static Specification<TaskEntity> whereTaskIds(Iterable<String> taskIds) {
        List<String> taskIdList = new ArrayList<>();
        taskIds.forEach(taskId -> {
            taskIdList.add(new StringJavaType().wrap(taskId, null));
        });

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            predicate = criteriaBuilder.and(predicate, root.get("taskId").in(taskIdList));
            return predicate;
        };
    }

    public static Specification<TaskEntity> fetchTaskAttachments() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) {
                return null;
            }
            if (Long.class != query.getResultType()) {
                root.fetch("taskAttachments", JoinType.LEFT);
                query.distinct(true);
                return null;
            }

            return null;
        };
    }

    public static Specification<TaskEntity> withTeamFilter(TeamFilter teamFilter) {
        List<String> teamList = new ArrayList<>();
        if (teamFilter != null && teamFilter.isValid()) {
            teamFilter.getTeamIds().forEach(team -> {
                teamList.add(new StringJavaType().wrap(team, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (teamFilter != null && teamFilter.isValid()) {
                predicate = criteriaBuilder.and(predicate, root.get("teamId").in(teamList));
            }
            return predicate;
        };
    }

    public static Specification<TaskEntity> withStatusFilter(StatusFilter statusFilter) {
        List<String> statusList = new ArrayList<>();
        if (statusFilter != null && statusFilter.isValid()) {
            statusFilter.getStatuses().forEach(status -> {
                statusList.add(new StringJavaType().wrap(status, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (statusFilter != null && statusFilter.isValid()) {
                predicate = criteriaBuilder.and(predicate, root.get("status").in(statusList));
            }
            return predicate;
        };
    }

    public static Specification<TaskEntity> withChargeUserFilter(ChargeUserFilter chargeUserFilter) {
        List<String> chargeUserList = new ArrayList<>();
        if (chargeUserFilter != null && chargeUserFilter.isValid()) {
            chargeUserFilter.getChargeUserIds().forEach(chargeUser -> {
                chargeUserList.add(new StringJavaType().wrap(chargeUser, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (chargeUserFilter != null && chargeUserFilter.isValid()) {
                predicate = criteriaBuilder.and(predicate, root.get("chargeUserId").in(chargeUserList));
            }
            return predicate;
        };
    }

    public static Specification<TaskEntity> withAttachmentFileObjectsFilter(FileObjectsFilter fileObjectsFilter) {
        List<String> fileObjectList = new ArrayList<>();
        if (fileObjectsFilter != null && fileObjectsFilter.isValid()) {
            fileObjectsFilter.getFileObjectIds().forEach(fileObject -> {
                fileObjectList.add(new StringJavaType().wrap(fileObject, null));
            });
        }

        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (fileObjectsFilter != null && fileObjectsFilter.isValid()) {
                if (!fileObjectsFilter.isAny()) {
                    // all
                    for (String fileObjectId : fileObjectList) {
                        predicate = criteriaBuilder.and(predicate, criteriaBuilder.isMember(fileObjectId, root.get("taskAttachments")));
                    }
                } else {
                    // any
                    predicate = criteriaBuilder.and(predicate, root.get("taskAttachments").get("fileObjectId").in(fileObjectList));
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
