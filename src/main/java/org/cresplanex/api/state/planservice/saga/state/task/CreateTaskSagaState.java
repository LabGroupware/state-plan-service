package org.cresplanex.api.state.planservice.saga.state.task;

import lombok.*;

import org.cresplanex.api.state.common.dto.plan.TaskWithAttachmentsDto;
import org.cresplanex.api.state.common.saga.command.plan.CreateTaskAndAttachInitialFIleObjectsCommand;
import org.cresplanex.api.state.common.saga.state.SagaState;
import org.cresplanex.api.state.common.saga.validate.storage.FileObjectExistValidateCommand;
import org.cresplanex.api.state.common.saga.validate.team.TeamExistValidateCommand;
import org.cresplanex.api.state.common.saga.validate.userprofile.UserExistValidateCommand;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.saga.model.task.CreateTaskSaga;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class CreateTaskSagaState
        extends SagaState<CreateTaskSaga.Action, TaskEntity> {
    private InitialData initialData;
    private TaskWithAttachmentsDto taskWithAttachmentsDto = TaskWithAttachmentsDto.empty();
    private String operatorId;

    @Override
    public String getId() {
        return taskWithAttachmentsDto.getTask().getTaskId();
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
        private String operatorId;
        private String teamId;
        private String chargeUserId;
        private String title;
        private String description;
        private String startDatetime;
        private String dueDatetime;
        private List<FileObject> attachmentFileObjects;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class FileObject {
            private String fileObjectId;
        }
    }

    public UserExistValidateCommand makeUserExistValidateCommand() {
        return new UserExistValidateCommand(
                List.of(initialData.getChargeUserId())
        );
    }

    public TeamExistValidateCommand makeTeamExistValidateCommand() {
        return new TeamExistValidateCommand(
                List.of(initialData.getTeamId())
        );
    }

    public FileObjectExistValidateCommand makeFileObjectExistValidateCommand() {
        return new FileObjectExistValidateCommand(
                initialData.getAttachmentFileObjects().stream()
                        .map(InitialData.FileObject::getFileObjectId)
                        .toList()
        );
    }

    public CreateTaskAndAttachInitialFIleObjectsCommand.Exec makeCreateTaskAndAttachInitialFIleObjectsCommand() {
        return new CreateTaskAndAttachInitialFIleObjectsCommand.Exec(
                this.operatorId,
                initialData.getTeamId(),
                initialData.getChargeUserId(),
                initialData.getTitle(),
                initialData.getDescription(),
                initialData.getStartDatetime(),
                initialData.getDueDatetime(),
                initialData.getAttachmentFileObjects().stream()
                        .map(fileObject -> new CreateTaskAndAttachInitialFIleObjectsCommand.Exec.FileObject(fileObject.getFileObjectId()))
                        .toList()
        );
    }

    public CreateTaskAndAttachInitialFIleObjectsCommand.Undo makeUndoCreateTaskAndAttachInitialFIleObjectsCommand() {
        return new CreateTaskAndAttachInitialFIleObjectsCommand.Undo(taskWithAttachmentsDto.getTask().getTaskId());
    }
}
