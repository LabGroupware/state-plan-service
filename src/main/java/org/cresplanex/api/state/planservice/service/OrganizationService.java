package org.cresplanex.api.state.planservice.service;

import lombok.extern.slf4j.Slf4j;
import org.cresplanex.api.state.common.saga.local.LocalException;
import org.cresplanex.api.state.common.saga.local.organization.InvalidOrganizationPlanException;
import org.cresplanex.api.state.common.saga.local.organization.NotFoundOrganizationException;
import org.cresplanex.api.state.common.service.BaseService;
import org.cresplanex.api.state.planservice.constants.TaskStatus;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.exception.AlreadyExistTaskAttachmentException;
import org.cresplanex.api.state.planservice.exception.NotFoundTaskAttachmentException;
import org.cresplanex.api.state.planservice.exception.TaskNotFoundException;
import org.cresplanex.api.state.planservice.repository.TaskRepository;
import org.cresplanex.api.state.planservice.repository.TaskAttachmentRepository;
import org.cresplanex.api.state.planservice.saga.model.organization.AddUsersOrganizationSaga;
import org.cresplanex.api.state.planservice.saga.model.organization.CreateOrganizationSaga;
import org.cresplanex.api.state.planservice.saga.state.organization.AddUsersOrganizationSagaState;
import org.cresplanex.api.state.planservice.saga.state.organization.CreateOrganizationSagaState;
import org.cresplanex.core.saga.orchestration.SagaInstanceFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrganizationService extends BaseService {

    private final TaskRepository taskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final SagaInstanceFactory sagaInstanceFactory;

    private final CreateOrganizationSaga createOrganizationSaga;
    private final AddUsersOrganizationSaga addUsersOrganizationSaga;

    @Transactional(readOnly = true)
    public TaskEntity findById(String organizationId) {
        return internalFindById(organizationId);
    }

    private TaskEntity internalFindById(String organizationId) {
        return taskRepository.findById(organizationId).orElseThrow(() -> new TaskNotFoundException(
                TaskNotFoundException.FindType.BY_ID,
                organizationId
        ));
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> get() {
        return taskRepository.findAll();
    }

    @Transactional
    public String beginCreate(String operatorId, TaskEntity organization, List<TaskAttachmentEntity> users) {
        CreateOrganizationSagaState.InitialData initialData = CreateOrganizationSagaState.InitialData.builder()
                .name(organization.getName())
                .plan(organization.getPlan())
                .users(users.stream().map(user -> CreateOrganizationSagaState.InitialData.User.builder()
                        .userId(user.getUserId())
                        .build())
                        .toList())
                .build();
        CreateOrganizationSagaState state = new CreateOrganizationSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        // 一つ目がlocalStepであるため, 一つ目のLocal Exceptionが発生する場合,
        // ここで処理する必要がある.
        // ただし, rollbackExceptionに登録する必要がある.
        try {
            sagaInstanceFactory.create(createOrganizationSaga, state);
        } catch (LocalException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("LocalException: {}", e.getMessage());
            return jobId;
        }

        return jobId;
    }

    @Transactional
    public String beginAddUsers(String operatorId, String organizationId, List<TaskAttachmentEntity> users) {
        AddUsersOrganizationSagaState.InitialData initialData = AddUsersOrganizationSagaState.InitialData.builder()
                .organizationId(organizationId)
                .users(users.stream().map(user -> AddUsersOrganizationSagaState.InitialData.User.builder()
                        .userId(user.getUserId())
                        .build())
                        .toList())
                .build();
        AddUsersOrganizationSagaState state = new AddUsersOrganizationSagaState();
        state.setInitialData(initialData);
        state.setOperatorId(operatorId);

        String jobId = getJobId();
        state.setJobId(jobId);

        try {
            sagaInstanceFactory.create(addUsersOrganizationSaga, state);
        } catch (LocalException e) {
            // Jobで失敗イベント送信済みのため, ここでは何もしない
            log.debug("LocalException: {}", e.getMessage());
            return jobId;
        }

        return jobId;
    }

    public void validateCreatedOrganization(String name, String plan)
            throws InvalidOrganizationPlanException {
        if (!Arrays.asList(TaskStatus.ALL).contains(plan)) {
            throw new InvalidOrganizationPlanException(List.of(plan));
        }
    }

    public void validateOrganizations(List<String> organizationIds)
            throws NotFoundOrganizationException {
        taskRepository.countByOrganizationIdIn(organizationIds)
                .ifPresent(count -> {
                    if (count != organizationIds.size()) {
                        throw new NotFoundOrganizationException(organizationIds);
                    }
                });
    }

    public TaskEntity
    createAndAddUsers(String operatorId, TaskEntity organization) {
        return taskRepository.save(organization);
    }

    public List<TaskAttachmentEntity> addUsers(String operatorId, String organizationId, List<String> userIds) {
        List<TaskAttachmentEntity> existUsers = taskAttachmentRepository.
                findAllByOrganizationIdAndUserIds(organizationId, userIds);
        if (!existUsers.isEmpty()) {
            List<String> existUserIds = existUsers.stream()
                    .map(TaskAttachmentEntity::getUserId)
                    .toList();
            throw new AlreadyExistTaskAttachmentException(existUserIds);
        }
        List<TaskAttachmentEntity> users = userIds.stream()
                .map(userId -> {
                    TaskAttachmentEntity user = new TaskAttachmentEntity();
                    user.setOrganizationId(organizationId);
                    user.setUserId(userId);
                    return user;
                })
                .toList();
        return taskAttachmentRepository.saveAll(users);
    }

    public void undoCreate(String organizationId) {
        TaskEntity organization = taskRepository.findByIdWithUsers(organizationId)
                .orElseThrow(() -> new TaskNotFoundException(
                        TaskNotFoundException.FindType.BY_ID,
                        organizationId
                ));
        taskRepository.delete(organization);
    }

    public void undoAddUsers(List<String> organizationUserIds) {
        taskAttachmentRepository.deleteAllById(organizationUserIds);
    }

    public void validateOrganizationsAndOrganizationUsers(String organizationId, List<String> userIds) {
        taskRepository.countByOrganizationIdIn(List.of(organizationId))
                .ifPresent(count -> {
                    if (count != 1) {
                        throw new org.cresplanex.api.state.planservice.saga.handler.NotFoundOrganizationException(
                                List.of(organizationId)
                        );
                    }
                });
        List<String> existUserIds = taskAttachmentRepository.
                findAllByOrganizationIdAndUserIds(organizationId, userIds)
                .stream()
                .map(TaskAttachmentEntity::getUserId)
                .toList();
        if (existUserIds.size() != userIds.size()) {
            List<String> notExistUserIds = userIds.stream()
                    .filter(userId -> !existUserIds.contains(userId))
                    .toList();
            throw new NotFoundTaskAttachmentException(organizationId, notExistUserIds);
        }
    }
}
