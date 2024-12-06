package org.cresplanex.api.state.planservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.entity.EntityWithPrevious;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.saga.local.plan.InvalidDueDateTimeException;
import org.cresplanex.api.state.common.saga.local.plan.InvalidStartDateTimeException;
import org.cresplanex.api.state.common.saga.local.plan.StartTimeMustBeEarlierDueTimeException;
import org.cresplanex.api.state.common.saga.local.plan.WillAddedTaskAttachmentsDuplicatedException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.exception.TaskNotFoundException;
import org.cresplanex.api.state.planservice.repository.TaskAttachmentRepository;
import org.cresplanex.api.state.planservice.repository.TaskRepository;
import org.cresplanex.api.state.planservice.saga.model.task.CreateTaskSaga;
import org.cresplanex.api.state.planservice.saga.model.task.UpdateStatusTaskSaga;
import org.cresplanex.api.state.planservice.saga.state.task.CreateTaskSagaState;
import org.cresplanex.api.state.planservice.saga.state.task.UpdateStatusTaskSagaState;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskLocalValidateService extends BaseService {

    private final TaskRepository taskRepository;

    public void validateCreatedTask(
            String teamId,
            String chargeUserId,
            String title,
            String description,
            String startDatetime,
            String dueDatetime,
            List<String> taskAttachmentIds
    )
            throws InvalidStartDateTimeException, InvalidDueDateTimeException, StartTimeMustBeEarlierDueTimeException {
        LocalDateTime startTimestamp;
        try {
            startTimestamp = LocalDateTime.parse(startDatetime);
        } catch (Exception e) {
            throw new InvalidStartDateTimeException(startDatetime);
        }
        LocalDateTime dueTimestamp;
        try {
            dueTimestamp = LocalDateTime.parse(dueDatetime);
        } catch (Exception e) {
            throw new InvalidDueDateTimeException(dueDatetime);
        }
        if (startTimestamp.isAfter(dueTimestamp)) {
            throw new StartTimeMustBeEarlierDueTimeException(startTimestamp, dueTimestamp);
        }

        if (taskAttachmentIds.size() != taskAttachmentIds.stream().distinct().count()) {
            throw new WillAddedTaskAttachmentsDuplicatedException(taskAttachmentIds);
        }
    }

    public void validateTasks(List<String> taskIds)
            throws org.cresplanex.api.state.common.saga.local.plan.NotFoundTaskException {
        taskRepository.countByTaskIdIn(taskIds)
                .ifPresent(count -> {
                    if (count != taskIds.size()) {
                        throw new org.cresplanex.api.state.common.saga.local.plan.NotFoundTaskException(taskIds);
                    }
                });
    }
}
