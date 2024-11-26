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
import org.cresplanex.api.state.planservice.specification.TaskSpecifications;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.data.domain.PageRequest;
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

        List<TaskEntity> data = switch (paginationType) {
            case OFFSET ->
                    taskRepository.findList(spec, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> taskRepository.findList(spec, sortType); // TODO: Implement cursor pagination
            default -> taskRepository.findList(spec, sortType);
        };

        int count = 0;
        if (withCount){
            count = taskRepository.countList(spec);
        }
        return new ListEntityWithCount<>(
                data,
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
        );

        List<TaskEntity> data = switch (paginationType) {
            case OFFSET ->
                    taskRepository.findListWithAttachments(spec, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> taskRepository.findListWithAttachments(spec, sortType); // TODO: Implement cursor pagination
            default -> taskRepository.findListWithAttachments(spec, sortType);
        };

        int count = 0;
        if (withCount){
            count = taskRepository.countList(spec);
        }
        return new ListEntityWithCount<>(
                data,
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
        Specification<TaskEntity> spec = Specification.where(null);

        List<TaskAttachmentEntity> data = switch (paginationType) {
            case OFFSET ->
                    taskAttachmentRepository.findFileObjectsListOnTaskWithOffsetPagination(spec, taskId, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> taskAttachmentRepository.findFileObjectsListOnTask(spec, taskId, sortType); // TODO: Implement cursor pagination
            default -> taskAttachmentRepository.findFileObjectsListOnTask(spec, taskId, sortType);
        };

        int count = 0;
        if (withCount){
            count = taskAttachmentRepository.countFileObjectsListOnTask(spec, taskId);
        }
        return new ListEntityWithCount<>(
                data,
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
        Specification<TaskEntity> spec = Specification.where(null);

        List<TaskAttachmentEntity> data = switch (paginationType) {
            case OFFSET ->
                    taskAttachmentRepository.findTasksOnFileObjectWithOffsetPagination(spec, fileObjectId, sortType, PageRequest.of(offset / limit, limit));
            case CURSOR -> taskAttachmentRepository.findTasksOnFileObject(spec, fileObjectId, sortType); // TODO: Implement cursor pagination
            default -> taskAttachmentRepository.findTasksOnFileObject(spec, fileObjectId, sortType);
        };

        int count = 0;
        if (withCount){
            count = taskAttachmentRepository.countTasksOnFileObject(spec, fileObjectId);
        }
        return new ListEntityWithCount<>(
                data,
                count
        );
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> getByTaskIds(
            List<String> taskIds,
            TaskSortType sortType
    ) {
        return taskRepository.findListByTaskIds(taskIds, sortType);
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> getByTaskIdsWithAttachments(
            List<String> taskIds,
            TaskWithFileObjectsSortType sortType
    ) {
        return taskRepository.findListByTaskIdsWithAttachments(taskIds, sortType);
    }

    @Transactional
    public String beginCreate(String operatorId, TaskEntity task, List<TaskAttachmentEntity> attachment) {
        CreateTaskSagaState.InitialData initialData = CreateTaskSagaState.InitialData.builder()
                .teamId(task.getTeamId())
                .chargeUserId(task.getChargeUserId())
                .title(task.getTitle())
                .description(task.getDescription())
                .startDatetime(task.getStartDatetime().format(DateTimeFormatter.ISO_DATE_TIME))
                .dueDatetime(task.getDueDatetime().format(DateTimeFormatter.ISO_DATE_TIME))
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
        TaskEntity existingTask = internalFindById(taskId);
        TaskEntity updatedTask = existingTask.clone();
        updatedTask.setStatus(status);
        return new EntityWithPrevious<>(taskRepository.save(updatedTask), existingTask);
    }

    public void undoUpdate(String taskId, String prevStatus) {
        TaskEntity existingTask = internalFindById(taskId);
        existingTask.setStatus(prevStatus);
        taskRepository.save(existingTask);
    }
}
