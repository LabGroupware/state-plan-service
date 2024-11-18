package org.cresplanex.api.state.planservice.saga.model.task;

import org.cresplanex.api.state.common.constants.PlanServiceApplicationCode;
import org.cresplanex.api.state.common.dto.plan.TaskWithAttachmentsDto;
import org.cresplanex.api.state.common.event.model.plan.TaskCreated;
import org.cresplanex.api.state.common.event.model.plan.TaskDomainEvent;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.data.plan.CreateTaskResultData;
import org.cresplanex.api.state.common.saga.local.plan.InvalidDueDateTimeException;
import org.cresplanex.api.state.common.saga.local.plan.InvalidStartDateTimeException;
import org.cresplanex.api.state.common.saga.local.plan.StartTimeMustBeEarlierDueTimeException;
import org.cresplanex.api.state.common.saga.model.SagaModel;
import org.cresplanex.api.state.common.saga.reply.plan.CreateTaskAndAttachInitialFIleObjectsReply;
import org.cresplanex.api.state.common.saga.reply.storage.FileObjectExistValidateReply;
import org.cresplanex.api.state.common.saga.reply.team.TeamExistValidateReply;
import org.cresplanex.api.state.common.saga.type.PlanSagaType;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.event.publisher.TaskDomainEventPublisher;
import org.cresplanex.api.state.planservice.saga.proxy.PlanServiceProxy;
import org.cresplanex.api.state.planservice.saga.proxy.StorageServiceProxy;
import org.cresplanex.api.state.planservice.saga.proxy.TeamServiceProxy;
import org.cresplanex.api.state.planservice.saga.proxy.UserProfileServiceProxy;
import org.cresplanex.api.state.planservice.saga.state.task.CreateTaskSagaState;
import org.cresplanex.api.state.planservice.service.TaskLocalValidateService;
import org.cresplanex.core.saga.orchestration.SagaDefinition;
import org.cresplanex.api.state.common.saga.reply.userprofile.UserExistValidateReply;
import org.springframework.stereotype.Component;

@Component
public class CreateTaskSaga extends SagaModel<
        TaskEntity,
        TaskDomainEvent,
        CreateTaskSaga.Action,
        CreateTaskSagaState> {

    private final SagaDefinition<CreateTaskSagaState> sagaDefinition;
    private final TaskDomainEventPublisher domainEventPublisher;
    private final TaskLocalValidateService taskLocalService;

    public CreateTaskSaga(
            TaskLocalValidateService taskLocalService,
            PlanServiceProxy taskService,
            StorageServiceProxy storageService,
            TeamServiceProxy teamService,
            UserProfileServiceProxy userProfileService,
            TaskDomainEventPublisher domainEventPublisher
    ) {
        this.sagaDefinition = step()
                .invokeLocal(this::validateTask)
                .onException(InvalidStartDateTimeException.class, this::failureLocalExceptionPublish)
                .onException(InvalidDueDateTimeException.class, this::failureLocalExceptionPublish)
                .onException(StartTimeMustBeEarlierDueTimeException.class, this::failureLocalExceptionPublish)
                .step()
                .invokeParticipant(
                        userProfileService.userExistValidate,
                        CreateTaskSagaState::makeUserExistValidateCommand
                )
                .onReply(
                        UserExistValidateReply.Success.class,
                        UserExistValidateReply.Success.TYPE,
                        this::processedEventPublish
                )
                .onReply(
                        UserExistValidateReply.Failure.class,
                        UserExistValidateReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .step()
                .invokeParticipant(
                        teamService.teamExistValidate,
                        CreateTaskSagaState::makeTeamExistValidateCommand
                )
                .onReply(
                        TeamExistValidateReply.Success.class,
                        TeamExistValidateReply.Success.TYPE,
                        this::processedEventPublish
                )
                .onReply(
                        TeamExistValidateReply.Failure.class,
                        TeamExistValidateReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .step()
                .invokeParticipant(
                        storageService.fileObjectExistValidate,
                        CreateTaskSagaState::makeFileObjectExistValidateCommand
                )
                .onReply(
                        FileObjectExistValidateReply.Success.class,
                        FileObjectExistValidateReply.Success.TYPE,
                        this::processedEventPublish
                )
                .onReply(
                        FileObjectExistValidateReply.Failure.class,
                        FileObjectExistValidateReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .step()
                .invokeParticipant(
                        taskService.createTaskAndAttachInitialFIleObjects,
                        CreateTaskSagaState::makeCreateTaskAndAttachInitialFIleObjectsCommand
                )
                .onReply(
                        CreateTaskAndAttachInitialFIleObjectsReply.Success.class,
                        CreateTaskAndAttachInitialFIleObjectsReply.Success.TYPE,
                        this::handleCreateTaskAndAttachInitialFIleObjectsReply
                )
                .onReply(
                        CreateTaskAndAttachInitialFIleObjectsReply.Failure.class,
                        CreateTaskAndAttachInitialFIleObjectsReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        taskService.undoCreateTaskAndAttachInitialFIleObjects,
                        CreateTaskSagaState::makeUndoCreateTaskAndAttachInitialFIleObjectsCommand
                )
                .build();
        this.domainEventPublisher = domainEventPublisher;
        this.taskLocalService = taskLocalService;
    }

    @Override
    protected AggregateDomainEventPublisher<TaskEntity, TaskDomainEvent>
    getDomainEventPublisher() {
        return domainEventPublisher;
    }

    @Override
    protected Action[] getActions() {
        return Action.values();
    }

    @Override
    protected String getBeginEventType() {
        return TaskCreated.BeginJobDomainEvent.TYPE;
    }

    @Override
    protected String getProcessedEventType() {
        return TaskCreated.ProcessedJobDomainEvent.TYPE;
    }

    @Override
    protected String getFailedEventType() {
        return TaskCreated.FailedJobDomainEvent.TYPE;
    }

    @Override
    protected String getSuccessfullyEventType() {
        return TaskCreated.SuccessJobDomainEvent.TYPE;
    }

    private void validateTask(CreateTaskSagaState state)
    throws InvalidStartDateTimeException, InvalidDueDateTimeException, StartTimeMustBeEarlierDueTimeException {
        this.taskLocalService.validateCreatedTask(
                state.getInitialData().getTeamId(),
                state.getInitialData().getChargeUserId(),
                state.getInitialData().getTitle(),
                state.getInitialData().getDescription(),
                state.getInitialData().getStartDatetime(),
                state.getInitialData().getDueDatetime()
        );

        this.localProcessedEventPublish(
                state, PlanServiceApplicationCode.SUCCESS, "Task validated"
        );
    }

    private void handleCreateTaskAndAttachInitialFIleObjectsReply(
            CreateTaskSagaState state,
            CreateTaskAndAttachInitialFIleObjectsReply.Success reply
    ) {
        CreateTaskAndAttachInitialFIleObjectsReply.Success.Data data = reply.getData();
        TaskWithAttachmentsDto taskWithAttachmentsDto = new TaskWithAttachmentsDto(
                data.getTask(),
                data.getFileObjects()
        );
        state.setTaskWithAttachmentsDto(taskWithAttachmentsDto);
        this.processedEventPublish(state, reply);
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, CreateTaskSagaState data) {
        CreateTaskResultData resultData = new CreateTaskResultData(data.getTaskWithAttachmentsDto());
        successfullyEventPublish(data, resultData);
    }

    public enum Action {
        VALIDATE_TASK,
        VALIDATE_USER,
        VALIDATE_TEAM,
        VALIDATE_FILE_OBJECT,
        CREATE_TASK_AND_ATTACH_FILE_OBJECT,
    }

    @Override
    public SagaDefinition<CreateTaskSagaState> getSagaDefinition() {
        return sagaDefinition;
    }

    @Override
    public String getSagaType() {
        return PlanSagaType.CREATE_TASK;
    }

    @Override
    public String getSagaCommandSelfChannel() {
        return SagaCommandChannel.PLAN;
    }
}
