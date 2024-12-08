package org.cresplanex.api.state.planservice.service;

import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.entity.EntityWithPrevious;
import org.cresplanex.api.state.common.entity.ListEntityWithCount;
import org.cresplanex.api.state.common.enums.PaginationType;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.enums.FileObjectOnTaskSortType;
import org.cresplanex.api.state.planservice.enums.TaskOnFileObjectSortType;
import org.cresplanex.api.state.planservice.enums.TaskSortType;
import org.cresplanex.api.state.planservice.enums.TaskWithFileObjectsSortType;
import org.cresplanex.api.state.planservice.exception.TaskNotFoundException;
import org.cresplanex.api.state.planservice.filter.task.*;
import org.cresplanex.api.state.planservice.repository.TaskRepository;
import org.cresplanex.api.state.planservice.repository.TaskAttachmentRepository;
import org.cresplanex.api.state.planservice.saga.model.task.UpdateStatusTaskSaga;
import org.cresplanex.api.state.planservice.saga.model.task.CreateTaskSaga;
import org.cresplanex.api.state.planservice.saga.state.task.UpdateStatusTaskSagaState;
import org.cresplanex.api.state.planservice.saga.state.task.CreateTaskSagaState;
import org.cresplanex.api.state.planservice.specification.TaskAttachmentSpecifications;
import org.cresplanex.api.state.planservice.specification.TaskSpecifications;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskService extends BaseService {

    private final TaskRepository taskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final SagaInstanceFactory sagaInstanceFactory;

    private final CreateTaskSaga createTaskSaga;
    private final UpdateStatusTaskSaga updateStatusTaskSaga;

    @Transactional(readOnly = true)
    public TaskEntity findById(String taskId) {
        return internalFindById(taskId);
    }

    @Transactional(readOnly = true)
    public TaskEntity findByIdWithAttachments(String taskId) {
        return taskRepository.findByIdWithAttachments(taskId)
                .orElseThrow(() -> new TaskNotFoundException(
                        TaskNotFoundException.FindType.BY_ID,
                        taskId
                ));
    }

    private TaskEntity internalFindById(String taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(
                TaskNotFoundException.FindType.BY_ID,
                taskId
        ));
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<TaskEntity> get(
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            TaskSortType sortType,
            boolean withCount,
            TeamFilter teamFilter,
            StatusFilter statusFilter,
            ChargeUserFilter chargeUserFilter,
            FileObjectsFilter fileObjectsFilter,
            StartDatetimeFilter startDatetimeFilter,
            DueDatetimeFilter dueDatetimeFilter
    ) {
        Specification<TaskEntity> spec = Specification.where(
                TaskSpecifications.withTeamFilter(teamFilter)
                        .and(TaskSpecifications.withStatusFilter(statusFilter))
                        .and(TaskSpecifications.withChargeUserFilter(chargeUserFilter))
                        .and(TaskSpecifications.withAttachmentFileObjectsFilter(fileObjectsFilter))
                        .and(TaskSpecifications.withStartDatetimeFilter(startDatetimeFilter))
                        .and(TaskSpecifications.withDueDatetimeFilter(dueDatetimeFilter))
        );

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<TaskEntity> data = taskRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int) data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<TaskEntity> getWithAttachments(
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            TaskWithFileObjectsSortType sortType,
            boolean withCount,
            TeamFilter teamFilter,
            StatusFilter statusFilter,
            ChargeUserFilter chargeUserFilter,
            FileObjectsFilter fileObjectsFilter,
            StartDatetimeFilter startDatetimeFilter,
            DueDatetimeFilter dueDatetimeFilter
    ) {
        Specification<TaskEntity> spec = Specification.where(
                TaskSpecifications.withTeamFilter(teamFilter)
                        .and(TaskSpecifications.withStatusFilter(statusFilter))
                        .and(TaskSpecifications.withChargeUserFilter(chargeUserFilter))
                        .and(TaskSpecifications.withAttachmentFileObjectsFilter(fileObjectsFilter))
                        .and(TaskSpecifications.withStartDatetimeFilter(startDatetimeFilter))
                        .and(TaskSpecifications.withDueDatetimeFilter(dueDatetimeFilter))
                        .and(TaskSpecifications.fetchTaskAttachments())
        );

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<TaskEntity> data = taskRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int) data.getTotalElements();
        }

        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<TaskAttachmentEntity> getFileObjectsOnTask(
            String taskId,
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            FileObjectOnTaskSortType sortType,
            boolean withCount
    ) {
        Specification<TaskAttachmentEntity> spec = Specification.where(
                TaskAttachmentSpecifications.whereTaskId(taskId)
        );

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<TaskAttachmentEntity> data = taskAttachmentRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int) data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public ListEntityWithCount<TaskAttachmentEntity> getTasksOnFileObject(
            String fileObjectId,
            PaginationType paginationType,
            int limit,
            int offset,
            String cursor,
            TaskOnFileObjectSortType sortType,
            boolean withCount
    ) {
        Specification<TaskAttachmentEntity> spec = Specification.where(
                TaskAttachmentSpecifications.whereFileObjectId(fileObjectId)
                        .and(TaskAttachmentSpecifications.fetchTask())
        );

        Sort sort = createSort(sortType);

        Pageable pageable = switch (paginationType) {
            case OFFSET -> PageRequest.of(offset / limit, limit, sort);
            case CURSOR -> PageRequest.of(0, limit, sort); // TODO: Implement cursor pagination
            default -> Pageable.unpaged(sort);
        };

        Page<TaskAttachmentEntity> data = taskAttachmentRepository.findAll(spec, pageable);

        int count = 0;
        if (withCount){
            count = (int) data.getTotalElements();
        }
        return new ListEntityWithCount<>(
                data.getContent(),
                count
        );
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> getByTaskIds(
            List<String> taskIds,
            TaskSortType sortType
    ) {
        Specification<TaskEntity> spec = Specification.where(
                TaskSpecifications.whereTaskIds(taskIds)
        );

        return taskRepository.findAll(spec, createSort(sortType));
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> getByTaskIdsWithAttachments(
            List<String> taskIds,
            TaskWithFileObjectsSortType sortType
    ) {
        Specification<TaskEntity> spec = Specification.where(
                TaskSpecifications.whereTaskIds(taskIds)
                        .and(TaskSpecifications.fetchTaskAttachments())
        );

        return taskRepository.findAll(spec, createSort(sortType));
    }

    @Transactional
    public String beginCreate(
            String operatorId,
            TaskEntity task,
            List<TaskAttachmentEntity> attachment,
            String startDatetime,
            String dueDatetime
    ) {
        CreateTaskSagaState.InitialData initialData = CreateTaskSagaState.InitialData.builder()
                .teamId(task.getTeamId())
                .chargeUserId(task.getChargeUserId())
                .title(task.getTitle())
                .description(task.getDescription())
                .startDatetime(startDatetime)
                .dueDatetime(dueDatetime)
                .attachmentFileObjects(attachment.stream().map(object -> CreateTaskSagaState.InitialData.FileObject.builder()
                        .fileObjectId(object.getFileObjectId())
                        .build())
                        .toList())
                .build();
        CreateTaskSagaState state = new CreateTaskSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        try {
            sagaInstanceFactory.create(createTaskSaga, state);
        } catch (LocalException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("LocalException: {}", e.getMessage());
            return jobId;
        }

        return jobId;
    }

    public TaskEntity createAndAttacheFiles(String operatorId, TaskEntity task, List<TaskAttachmentEntity> attachments) {
        task = taskRepository.save(task);
        TaskEntity finalTask = task;
        attachments = attachments.stream()
                .peek(attachment -> attachment.setTask(finalTask))
                .toList();
        taskAttachmentRepository.saveAll(attachments);
        task.setTaskAttachments(attachments);
        return task;
    }

    public void undoCreate(String taskId) {
        TaskEntity task = taskRepository.findByIdWithAttachments(taskId)
                .orElseThrow(() -> new TaskNotFoundException(
                        TaskNotFoundException.FindType.BY_ID,
                        taskId
                ));
        taskRepository.delete(task);
    }

    @Transactional
    public String beginUpdateStatus(String operatorId, String taskId, String status) {
        UpdateStatusTaskSagaState.InitialData initialData = UpdateStatusTaskSagaState.InitialData.builder()
                .taskId(taskId)
                .status(status)
                .build();
        UpdateStatusTaskSagaState state = new UpdateStatusTaskSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        sagaInstanceFactory.create(updateStatusTaskSaga, state);

        return jobId;
    }

    public EntityWithPrevious<TaskEntity> update(String operatorId, String taskId, String status) {
        TaskEntity newTask = internalFindById(taskId);
        TaskEntity existingTask = newTask.clone();
        newTask.setStatus(status);
        return new EntityWithPrevious<>(taskRepository.save(newTask), existingTask);
    }

    public void undoUpdate(String taskId, String prevStatus) {
        TaskEntity existingTask = internalFindById(taskId);
        existingTask.setStatus(prevStatus);
        taskRepository.save(existingTask);
    }

    private Sort createSort(TaskSortType sortType) {
        return switch (sortType) {
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case TITLE_ASC -> Sort.by(Sort.Order.asc("title"), Sort.Order.desc("createdAt"));
            case TITLE_DESC -> Sort.by(Sort.Order.desc("title"), Sort.Order.desc("createdAt"));
            case START_DATETIME_ASC -> Sort.by(Sort.Order.asc("startDatetime"), Sort.Order.desc("createdAt"));
            case START_DATETIME_DESC -> Sort.by(Sort.Order.desc("startDatetime"), Sort.Order.desc("createdAt"));
            case DUE_DATETIME_ASC -> Sort.by(Sort.Order.asc("dueDatetime"), Sort.Order.desc("createdAt"));
            case DUE_DATETIME_DESC -> Sort.by(Sort.Order.desc("dueDatetime"), Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(TaskWithFileObjectsSortType sortType) {
        return switch (sortType) {
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case TITLE_ASC -> Sort.by(Sort.Order.asc("title"), Sort.Order.desc("createdAt"));
            case TITLE_DESC -> Sort.by(Sort.Order.desc("title"), Sort.Order.desc("createdAt"));
            case START_DATETIME_ASC -> Sort.by(Sort.Order.asc("startDatetime"), Sort.Order.desc("createdAt"));
            case START_DATETIME_DESC -> Sort.by(Sort.Order.desc("startDatetime"), Sort.Order.desc("createdAt"));
            case DUE_DATETIME_ASC -> Sort.by(Sort.Order.asc("dueDatetime"), Sort.Order.desc("createdAt"));
            case DUE_DATETIME_DESC -> Sort.by(Sort.Order.desc("dueDatetime"), Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(FileObjectOnTaskSortType sortType) {
        return switch (sortType) {
            case ADD_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case ADD_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
        };
    }

    private Sort createSort(TaskOnFileObjectSortType sortType) {
        return switch (sortType) {
            case ADD_AT_ASC -> Sort.by(Sort.Order.asc("createdAt"));
            case ADD_AT_DESC -> Sort.by(Sort.Order.desc("createdAt"));
            case TITLE_ASC -> Sort.by(Sort.Order.asc("task.title"));
            case TITLE_DESC -> Sort.by(Sort.Order.desc("task.title"));
            case CREATED_AT_ASC -> Sort.by(Sort.Order.asc("task.createdAt"));
            case CREATED_AT_DESC -> Sort.by(Sort.Order.desc("task.createdAt"));
            case DUE_DATETIME_ASC -> Sort.by(Sort.Order.asc("task.dueDatetime"));
            case DUE_DATETIME_DESC -> Sort.by(Sort.Order.desc("task.dueDatetime"));
            case START_DATETIME_ASC -> Sort.by(Sort.Order.asc("task.startDatetime"));
            case START_DATETIME_DESC -> Sort.by(Sort.Order.desc("task.startDatetime"));
        };
    }
}
