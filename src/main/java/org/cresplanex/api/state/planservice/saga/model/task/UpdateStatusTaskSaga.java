package org.cresplanex.api.state.planservice.saga.model.task;

import org.cresplanex.api.state.common.constants.PlanServiceApplicationCode;
import org.cresplanex.api.state.common.event.model.plan.TaskDomainEvent;
import org.cresplanex.api.state.common.event.model.plan.TaskUpdatedStatus;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.data.plan.UpdateStatusTaskResultData;
import org.cresplanex.api.state.common.saga.local.plan.NotFoundTaskException;
import org.cresplanex.api.state.common.saga.model.SagaModel;
import org.cresplanex.api.state.common.saga.reply.plan.UpdateStatusTaskReply;
import org.cresplanex.api.state.common.saga.type.PlanSagaType;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.event.publisher.TaskDomainEventPublisher;
import org.cresplanex.api.state.planservice.saga.proxy.PlanServiceProxy;
import org.cresplanex.api.state.planservice.saga.proxy.TeamServiceProxy;
import org.cresplanex.api.state.planservice.saga.proxy.UserProfileServiceProxy;
import org.cresplanex.api.state.planservice.saga.state.task.UpdateStatusTaskSagaState;
import org.cresplanex.api.state.planservice.service.TaskService;
import org.cresplanex.core.saga.orchestration.SagaDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpdateStatusTaskSaga extends SagaModel<
        TaskEntity,
        TaskDomainEvent,
        UpdateStatusTaskSaga.Action,
        UpdateStatusTaskSagaState> {

    private final SagaDefinition<UpdateStatusTaskSagaState> sagaDefinition;
    private final TaskDomainEventPublisher domainEventPublisher;
    private final TaskService organizationLocalService;

    public UpdateStatusTaskSaga(
            TaskService organizationLocalService,
            PlanServiceProxy organizationService,
            TeamServiceProxy teamService,
            UserProfileServiceProxy userProfileService,
            TaskDomainEventPublisher domainEventPublisher
    ) {
        this.sagaDefinition = step()
                .invokeLocal(this::validateTask)
                .onException(NotFoundTaskException.class, this::failureLocalExceptionPublish)
                .step()
                .invokeParticipant(
                        organizationService.updateStatusTask,
                        UpdateStatusTaskSagaState::makeUpdateStatusTaskCommand
                )
                .onReply(
                        UpdateStatusTaskReply.Success.class,
                        UpdateStatusTaskReply.Success.TYPE,
                        this::handleUpdateStatusTaskReply
                )
                .onReply(
                        UpdateStatusTaskReply.Failure.class,
                        UpdateStatusTaskReply.Failure.TYPE,
                        this::handleFailureReply
                )
                .withCompensation(
                        organizationService.undoUpdateStatusTask,
                        UpdateStatusTaskSagaState::makeUndoUpdateStatusTaskCommand
                )
                .build();
        this.organizationLocalService = organizationLocalService;
        this.domainEventPublisher = domainEventPublisher;
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
        return TaskUpdatedStatus.BeginJobDomainEvent.TYPE;
    }

    @Override
    protected String getProcessedEventType() {
        return TaskUpdatedStatus.ProcessedJobDomainEvent.TYPE;
    }

    @Override
    protected String getFailedEventType() {
        return TaskUpdatedStatus.FailedJobDomainEvent.TYPE;
    }

    @Override
    protected String getSuccessfullyEventType() {
        return TaskUpdatedStatus.SuccessJobDomainEvent.TYPE;
    }

    private void validateTask(UpdateStatusTaskSagaState state)
            throws NotFoundTaskException {
        this.organizationLocalService.validateTasks(
                List.of(state.getInitialData().getTaskId())
        );

        this.localProcessedEventPublish(
                state, PlanServiceApplicationCode.SUCCESS, "Task validated"
        );
    }

    private void handleUpdateStatusTaskReply(
            UpdateStatusTaskSagaState state, UpdateStatusTaskReply.Success reply) {
        UpdateStatusTaskReply.Success.Data data = reply.getData();
        state.setTaskDto(data.getTask());
        state.setPrevTaskStatus(data.getPrevTask().getStatus());
        this.processedEventPublish(state, reply);
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, UpdateStatusTaskSagaState data) {
        UpdateStatusTaskResultData resultData = new UpdateStatusTaskResultData(data.getTaskDto());
        successfullyEventPublish(data, resultData);
    }

    public enum Action {
        VALIDATE_TASK,
        UPDATE_TASK_STATUS,
    }

    @Override
    public SagaDefinition<UpdateStatusTaskSagaState> getSagaDefinition() {
        return sagaDefinition;
    }

    @Override
    public String getSagaType() {
        return PlanSagaType.UPDATE_TASK_STATUS;
    }

    @Override
    public String getSagaCommandSelfChannel() {
        return SagaCommandChannel.PLAN;
    }
}
