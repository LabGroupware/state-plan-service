package org.cresplanex.api.state.planservice.saga.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.constants.OrganizationServiceApplicationCode;
import org.cresplanex.api.state.common.saga.LockTargetType;
import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.organization.AddUsersOrganizationCommand;
import org.cresplanex.api.state.common.saga.command.organization.CreateOrganizationAndAddInitialOrganizationUserCommand;
import org.cresplanex.api.state.common.saga.reply.organization.AddUsersOrganizationReply;
import org.cresplanex.api.state.common.saga.reply.organization.CreateOrganizationAndAddInitialOrganizationUserReply;
import org.cresplanex.api.state.common.saga.reply.organization.OrganizationAndOrganizationUserExistValidateReply;
import org.cresplanex.api.state.common.saga.validate.organization.OrganizationAndOrganizationUserExistValidateCommand;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.exception.AlreadyExistTaskAttachmentException;
import org.cresplanex.api.state.planservice.exception.NotFoundTaskAttachmentException;
import org.cresplanex.api.state.planservice.mapper.dto.DtoMapper;
import org.cresplanex.api.state.planservice.service.OrganizationService;
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
public class OrganizationSagaCommandHandlers {

    private final OrganizationService organizationService;

    public CommandHandlers commandHandlers() {
        return SagaCommandHandlersBuilder
                .fromChannel(SagaCommandChannel.ORGANIZATION)
                .onMessage(CreateOrganizationAndAddInitialOrganizationUserCommand.Exec.class,
                        CreateOrganizationAndAddInitialOrganizationUserCommand.Exec.TYPE,
                        this::handleCreateOrganizationAndAddInitialOrganizationUserCommand
                )
                .onMessage(CreateOrganizationAndAddInitialOrganizationUserCommand.Undo.class,
                        CreateOrganizationAndAddInitialOrganizationUserCommand.Undo.TYPE,
                        this::handleUndoCreateOrganizationAndAddInitialOrganizationUserCommand
                )
                .withPreLock(this::undoCreateOrganizationAndAddInitialOrganizationUserPreLock)

                .onMessage(AddUsersOrganizationCommand.Exec.class,
                        AddUsersOrganizationCommand.Exec.TYPE,
                        this::handleAddUsersOrganizationCommand
                )
                .onMessage(AddUsersOrganizationCommand.Undo.class,
                        AddUsersOrganizationCommand.Undo.TYPE,
                        this::handleUndoAddUsersOrganizationCommand
                )

                .onMessage(OrganizationAndOrganizationUserExistValidateCommand.class,
                        OrganizationAndOrganizationUserExistValidateCommand.TYPE,
                        this::handleOrganizationAndOrganizationUserExistValidateCommand
                )
                .build();
    }

    private LockTarget undoCreateOrganizationAndAddInitialOrganizationUserPreLock(
            CommandMessage<CreateOrganizationAndAddInitialOrganizationUserCommand.Undo> cmd,
            PathVariables pathVariables
    ) {
        return new LockTarget(LockTargetType.ORGANIZATION, cmd.getCommand().getOrganizationId());
    }

    private Message handleCreateOrganizationAndAddInitialOrganizationUserCommand(
            CommandMessage<CreateOrganizationAndAddInitialOrganizationUserCommand.Exec> cmd) {
        try {
            CreateOrganizationAndAddInitialOrganizationUserCommand.Exec command = cmd.getCommand();
            TaskEntity organization = new TaskEntity();
            organization.setName(command.getName());
            organization.setPlan(command.getPlan());
            organization.setOwnerId(command.getOperatorId());
            List<TaskAttachmentEntity> users = command.getUsers().stream().map(user -> {
                TaskAttachmentEntity userEntity = new TaskAttachmentEntity();
                userEntity.setUserId(user.getUserId());
                return userEntity;
            }).toList();
            organization.setOrganizationUsers(users);
            organization = organizationService.createAndAddUsers(command.getOperatorId(), organization);
            CreateOrganizationAndAddInitialOrganizationUserReply.Success reply = new CreateOrganizationAndAddInitialOrganizationUserReply.Success(
                    new CreateOrganizationAndAddInitialOrganizationUserReply.Success.Data(
                            DtoMapper.convert(organization),
                            DtoMapper.convert(organization.getOrganizationUsers())
                    ),
                    OrganizationServiceApplicationCode.SUCCESS,
                    "Organization created successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return withLock(LockTargetType.ORGANIZATION, organization.getOrganizationId())
                    .withSuccess(reply, CreateOrganizationAndAddInitialOrganizationUserReply.Success.TYPE);
        } catch (Exception e) {
            CreateOrganizationAndAddInitialOrganizationUserReply.Failure reply = new CreateOrganizationAndAddInitialOrganizationUserReply.Failure(
                    null,
                    OrganizationServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to create organization",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, CreateOrganizationAndAddInitialOrganizationUserReply.Failure.TYPE);
        }
    }

    private Message handleUndoCreateOrganizationAndAddInitialOrganizationUserCommand(
            CommandMessage<CreateOrganizationAndAddInitialOrganizationUserCommand.Undo> cmd
    ) {
        try {
        CreateOrganizationAndAddInitialOrganizationUserCommand.Undo command = cmd.getCommand();
            String organizationId = command.getOrganizationId();
            organizationService.undoCreate(organizationId);
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }

    private Message handleAddUsersOrganizationCommand(
            CommandMessage<AddUsersOrganizationCommand.Exec> cmd
    ) {
        try {
            AddUsersOrganizationCommand.Exec command = cmd.getCommand();
            List<String> users = command.getUsers().stream().map(AddUsersOrganizationCommand.Exec.User::getUserId).toList();

            List<TaskAttachmentEntity> organizationUsers = organizationService.addUsers(command.getOperatorId(), command.getOrganizationId(), users);
            AddUsersOrganizationReply.Success reply = new AddUsersOrganizationReply.Success(
                    new AddUsersOrganizationReply.Success.Data(
                            DtoMapper.convert(organizationUsers)
                    ),
                    OrganizationServiceApplicationCode.SUCCESS,
                    "Users added successfully",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withSuccess(reply, AddUsersOrganizationReply.Success.TYPE);
        } catch (AlreadyExistTaskAttachmentException e) {
            AddUsersOrganizationReply.Failure reply = new AddUsersOrganizationReply.Failure(
                    new AddUsersOrganizationReply.Failure.AlreadyAddedOrganizationUser(e.getUserIds()),
                    OrganizationServiceApplicationCode.ALREADY_EXIST_USER,
                    "Users already added",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, AddUsersOrganizationReply.Failure.TYPE);
        } catch (Exception e) {
            AddUsersOrganizationReply.Failure reply = new AddUsersOrganizationReply.Failure(
                    null,
                    OrganizationServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to add users",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withException(reply, AddUsersOrganizationReply.Failure.TYPE);
        }
    }

    private Message handleUndoAddUsersOrganizationCommand(CommandMessage<AddUsersOrganizationCommand.Undo> cmd) {
        try {
            AddUsersOrganizationCommand.Undo command = cmd.getCommand();
            organizationService.undoAddUsers(command.getUserOrganizationIds());
            return withSuccess();
        } catch (Exception e) {
            return withException();
        }
    }

    private Message handleOrganizationAndOrganizationUserExistValidateCommand(
            CommandMessage<OrganizationAndOrganizationUserExistValidateCommand> cmd
    ) {
        try {
            OrganizationAndOrganizationUserExistValidateCommand command = cmd.getCommand();
            organizationService.validateOrganizationsAndOrganizationUsers(command.getOrganizationId(), command.getUserIds());
            return withSuccess();
        } catch (NotFoundOrganizationException e) {
            OrganizationAndOrganizationUserExistValidateReply.Failure reply = new OrganizationAndOrganizationUserExistValidateReply.Failure(
                    new OrganizationAndOrganizationUserExistValidateReply.Failure.OrganizationNotFound(e.getOrganizationIds()),
                    OrganizationServiceApplicationCode.NOT_FOUND,
                    "Organization not found",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withFailure(reply, OrganizationAndOrganizationUserExistValidateReply.Failure.TYPE);
        } catch (NotFoundTaskAttachmentException e) {
            OrganizationAndOrganizationUserExistValidateReply.Failure reply = new OrganizationAndOrganizationUserExistValidateReply.Failure(
                    new OrganizationAndOrganizationUserExistValidateReply.Failure.OrganizationUserNotFound(e.getUserIds()),
                    OrganizationServiceApplicationCode.NOT_EXIST_USER,
                    "Organization user not found",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withFailure(reply, OrganizationAndOrganizationUserExistValidateReply.Failure.TYPE);
        } catch (Exception e) {
            OrganizationAndOrganizationUserExistValidateReply.Failure reply =
                    new OrganizationAndOrganizationUserExistValidateReply.Failure(
                    null,
                    OrganizationServiceApplicationCode.INTERNAL_SERVER_ERROR,
                    "Failed to validate organization and organization users",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            return withFailure(reply, OrganizationAndOrganizationUserExistValidateReply.Failure.TYPE);
        }
    }
}
