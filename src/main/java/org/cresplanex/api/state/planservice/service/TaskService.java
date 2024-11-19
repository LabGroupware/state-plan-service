package org.cresplanex.api.state.planservice.service;

import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.entity.EntityWithPrevious;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.exception.TaskNotFoundException;
import org.cresplanex.api.state.planservice.repository.TaskRepository;
import org.cresplanex.api.state.planservice.repository.TaskAttachmentRepository;
import org.cresplanex.api.state.planservice.saga.model.task.UpdateStatusTaskSaga;
import org.cresplanex.api.state.planservice.saga.model.task.CreateTaskSaga;
import org.cresplanex.api.state.planservice.saga.state.task.UpdateStatusTaskSagaState;
import org.cresplanex.api.state.planservice.saga.state.task.CreateTaskSagaState;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
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

    private TaskEntity internalFindById(String taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(
                TaskNotFoundException.FindType.BY_ID,
                taskId
        ));
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> get() {
        return taskRepository.findAll();
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
