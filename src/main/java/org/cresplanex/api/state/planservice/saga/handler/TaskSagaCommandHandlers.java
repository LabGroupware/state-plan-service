package org.cresplanex.api.state.planservice.saga.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.constants.PlanServiceApplicationCode;
import org.cresplanex.api.state.common.entity.EntityWithPrevious;
import org.cresplanex.api.state.common.saga.LockTargetType;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.plan.CreateTaskAndAttachInitialFIleObjectsCommand;
import org.cresplanex.api.state.common.saga.command.plan.UpdateStatusTaskCommand;
import org.cresplanex.api.state.common.saga.reply.plan.CreateTaskAndAttachInitialFIleObjectsReply;
import org.cresplanex.api.state.common.saga.reply.plan.UpdateStatusTaskReply;
import org.cresplanex.api.state.planservice.constants.TaskStatus;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.exception.AlreadyExistTaskAttachmentException;
import org.cresplanex.api.state.planservice.mapper.dto.DtoMapper;
import org.cresplanex.api.state.planservice.service.TaskService;
import org.cresplanex.core.commands.consumer.CommandHandlers;
import org.cresplanex.core.commands.consumer.CommandMessage;
import org.cresplanex.core.commands.consumer.PathVariables;
import org.cresplanex.core.messaging.common.Message;
import org.cresplanex.core.saga.lock.LockTarget;
import org.cresplanex.core.saga.participant.SagaCommandHandlersBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.cresplanex.core.commands.consumer.CommandHandlerReplyBuilder.*;
import static org.cresplanex.core.saga.participant.SagaReplyMessageBuilder.withLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSagaCommandHandlers {

    private final TaskService taskService;

    public CommandHandlers commandHandlers() {
        return SagaCommandHandlersBuilder
                .fromChannel(SagaCommandChannel.PLAN)
                .onMessage(CreateTaskAndAttachInitialFIleObjectsCommand.Exec.class,
                        CreateTaskAndAttachInitialFIleObjectsCommand.Exec.TYPE,
                        this::handleCreateTaskAndAttachInitialFIleObjectsCommand
                )
                .onMessage(CreateTaskAndAttachInitialFIleObjectsCommand.Undo.class,
                        CreateTaskAndAttachInitialFIleObjectsCommand.Undo.TYPE,
                        this::handleUndoCreateTaskAndAttachInitialFIleObjectsCommand
                )
                .withPreLock(this::undoCreateTaskAndAttachInitialFIleObjectsPreLock)

                .onMessage(UpdateStatusTaskCommand.Exec.class,
                        UpdateStatusTaskCommand.Exec.TYPE,
                        this::handleUpdateStatusTaskCommand
                )
                .withPreLock(this::updateStatusTaskPreLock)
                .onMessage(UpdateStatusTaskCommand.Undo.class,
                        UpdateStatusTaskCommand.Undo.TYPE,
                        this::handleUndoUpdateStatusTaskCommand
                )
                .withPreLock(this::undoUpdateStatusTaskPreLock)
                .build();
    }

    private LockTarget undoCreateTaskAndAttachInitialFIleObjectsPreLock(
            CommandMessage<CreateTaskAndAttachInitialFIleObjectsCommand.Undo> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.PLAN_TASK, cmd.getCommand().getTaskId());
    }

    private LockTarget updateStatusTaskPreLock(
            CommandMessage<UpdateStatusTaskCommand.Exec> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.PLAN_TASK, cmd.getCommand().getTaskId());
    }

    private LockTarget undoUpdateStatusTaskPreLock(
            CommandMessage<UpdateStatusTaskCommand.Undo> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.PLAN_TASK, cmd.getCommand().getTaskId());
    }

    private Message handleCreateTaskAndAttachInitialFIleObjectsCommand(
            CommandMessage<CreateTaskAndAttachInitialFIleObjectsCommand.Exec> cmd) {
        try {
            CreateTaskAndAttachInitialFIleObjectsCommand.Exec command = cmd.getCommand();
            TaskEntity task = getTaskEntity(command);
            List<TaskAttachmentEntity> attachments = command.getAttachmentFileObjectIds().stream().map(attachment -> {
                TaskAttachmentEntity attachmentEntity = new TaskAttachmentEntity();
                attachmentEntity.setFileObjectId(attachment.getFileObjectId());
                return attachmentEntity;
            }).toList();
            task = taskService.createAndAttacheFiles(command.getOperatorId(), task, attachments);
            CreateTaskAndAttachInitialFIleObjectsReply.Success reply = new CreateTaskAndAttachInitialFIleObjectsReply.Success(
                    new CreateTaskAndAttachInitialFIleObjectsReply.Success.Data(
                            DtoMapper.convert(task),
                            DtoMapper.convert(task.getTaskAttachments())
                    ),
                    PlanServiceApplicationCode.SUCCESS,
                    "Task created successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return withLock(LockTargetType.PLAN_TASK, task.getTaskId())
                    .withSuccess(reply, CreateTaskAndAttachInitialFIleObjectsReply.Success.TYPE);
        } catch (Exception e) {
            CreateTaskAndAttachInitialFIleObjectsReply.Failure reply = new CreateTaskAndAttachInitialFIleObjectsReply.Failure(
                    null,
                    PlanServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to create task",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, CreateTaskAndAttachInitialFIleObjectsReply.Failure.TYPE);
        }
    }

    private static TaskEntity getTaskEntity(CreateTaskAndAttachInitialFIleObjectsCommand.Exec command) {
        TaskEntity task = new TaskEntity();
        task.setTeamId(command.getTeamId());
        task.setChargeUserId(command.getChargeUserId());
        task.setTitle(command.getTitle());
        task.setDescription(command.getDescription());
        task.setStartDatetime(LocalDateTime.parse(command.getStartDatetime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        task.setDueDatetime(LocalDateTime.parse(command.getDueDatetime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        task.setStatus(TaskStatus.DEFAULT);
        return task;
    }

    private Message handleUndoCreateTaskAndAttachInitialFIleObjectsCommand(
            CommandMessage<CreateTaskAndAttachInitialFIleObjectsCommand.Undo> cmd
    ) {
        try {
        CreateTaskAndAttachInitialFIleObjectsCommand.Undo command = cmd.getCommand();
            String taskId = command.getTaskId();
            taskService.undoCreate(taskId);
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }

    private Message handleUpdateStatusTaskCommand(
            CommandMessage<UpdateStatusTaskCommand.Exec> cmd
    ) {
        try {
            UpdateStatusTaskCommand.Exec command = cmd.getCommand();

            EntityWithPrevious<TaskEntity> tasks = taskService.update(
                    command.getOperatorId(), command.getTaskId(), command.getStatus());
            UpdateStatusTaskReply.Success reply = new UpdateStatusTaskReply.Success(
                    new UpdateStatusTaskReply.Success.Data(
                            DtoMapper.convert(tasks.getCurrent()),
                            DtoMapper.convert(tasks.getPrevious())
                    ),
                    PlanServiceApplicationCode.SUCCESS,
                    "Task status updated successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withSuccess(reply, UpdateStatusTaskReply.Success.TYPE);
        } catch (Exception e) {
            UpdateStatusTaskReply.Failure reply = new UpdateStatusTaskReply.Failure(
                    null,
                    PlanServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to update task status",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, UpdateStatusTaskReply.Failure.TYPE);
        }
    }

    private Message handleUndoUpdateStatusTaskCommand(CommandMessage<UpdateStatusTaskCommand.Undo> cmd) {
        try {
            UpdateStatusTaskCommand.Undo command = cmd.getCommand();
            taskService.undoUpdate(command.getTaskId(), command.getOriginStatus());
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }
}
