package org.cresplanex.api.state.planservice.saga.state.task;

import lombok.*;
import org.cresplanex.api.state.common.dto.plan.TaskDto;
import org.cresplanex.api.state.common.saga.command.plan.UpdateStatusTaskCommand;
import org.cresplanex.api.state.common.saga.state.SagaState;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.saga.model.task.UpdateStatusTaskSaga;

@Getter
@Setter
@NoArgsConstructor
public class UpdateStatusTaskSagaState
        extends SagaState<UpdateStatusTaskSaga.Action, TaskEntity> {
    private InitialData initialData;
    private TaskDto taskDto;
    private String prevTaskStatus;
    private String operatorId;

    @Override
    public String getId() {
        return initialData.taskId;
    }

    @Override
    public Class<TaskEntity> getEntityClass() {
        return TaskEntity.class;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InitialData {
        private String taskId;
        private String status;
    }

    public UpdateStatusTaskCommand.Exec makeUpdateStatusTaskCommand() {
        return new UpdateStatusTaskCommand.Exec(
                this.operatorId,
                initialData.getTaskId(),
                initialData.getStatus()
        );
    }

    public UpdateStatusTaskCommand.Undo makeUndoUpdateStatusTaskCommand() {
        return new UpdateStatusTaskCommand.Undo(
                initialData.getTaskId(),
                prevTaskStatus
        );
    }
}
