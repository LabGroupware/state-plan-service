package org.cresplanex.api.state.planservice.handler;

import build.buf.gen.cresplanex.nova.v1.Count;
import build.buf.gen.cresplanex.nova.v1.SortOrder;
import build.buf.gen.plan.v1.*;
import org.cresplanex.api.state.common.entity.ListEntityWithCount;
import org.cresplanex.api.state.common.enums.PaginationType;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.enums.FileObjectOnTaskSortType;
import org.cresplanex.api.state.planservice.enums.TaskOnFileObjectSortType;
import org.cresplanex.api.state.planservice.enums.TaskSortType;
import org.cresplanex.api.state.planservice.enums.TaskWithFileObjectsSortType;
import org.cresplanex.api.state.planservice.filter.task.*;
import org.cresplanex.api.state.planservice.mapper.proto.ProtoMapper;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.cresplanex.api.state.planservice.service.TaskService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@GrpcService
public class PlanServiceHandler extends PlanServiceGrpc.PlanServiceImplBase {

    private final TaskService taskService;

    @Override
    public void findTask(FindTaskRequest request, StreamObserver<FindTaskResponse> responseObserver) {
        TaskEntity task = taskService.findById(request.getTaskId());

        Task taskProto = ProtoMapper.convert(task);
        FindTaskResponse response = FindTaskResponse.newBuilder()
                .setTask(taskProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findTaskWithAttachments(FindTaskWithAttachmentsRequest request, StreamObserver<FindTaskWithAttachmentsResponse> responseObserver) {
        TaskEntity task = taskService.findByIdWithAttachments(request.getTaskId());

        TaskWithAttachments taskProto = ProtoMapper.convertWithFileObjects(task);
        FindTaskWithAttachmentsResponse response = FindTaskWithAttachmentsResponse.newBuilder()
                .setTask(taskProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTasks(GetTasksRequest request, StreamObserver<GetTasksResponse> responseObserver) {
        TaskSortType sortType = switch (request.getSort().getOrderField()) {
            case TASK_ORDER_FIELD_TITLE -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskSortType.TITLE_ASC : TaskSortType.TITLE_DESC;
            case TASK_ORDER_FIELD_START_DATETIME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskSortType.START_DATETIME_ASC : TaskSortType.START_DATETIME_DESC;
            case TASK_ORDER_FIELD_DUE_DATETIME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskSortType.DUE_DATETIME_ASC : TaskSortType.DUE_DATETIME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskSortType.CREATED_AT_ASC : TaskSortType.CREATED_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        TeamFilter teamFilter = new TeamFilter(
                request.getFilterTeam().getHasValue(), request.getFilterTeam().getTeamIdsList()
        );

        StatusFilter statusFilter = new StatusFilter(
                request.getFilterStatus().getHasValue(), request.getFilterStatus().getStatusesList()
        );

        ChargeUserFilter chargeUserFilter = new ChargeUserFilter(
                request.getFilterChargeUser().getHasValue(), request.getFilterChargeUser().getChargeUserIdsList()
        );

        FileObjectsFilter fileObjectsFilter = new FileObjectsFilter(
                request.getFilterFileObject().getHasValue(), request.getFilterFileObject().getAny(), request.getFilterFileObject().getFileObjectIdsList()
        );

        StartDatetimeFilter startDatetimeFilter = new StartDatetimeFilter(
                request.getFilterStartDatetime().getEarlierInfinite(),
                request.getFilterStartDatetime().getLaterInfinite(),
                request.getFilterStartDatetime().getEarlierThan(),
                request.getFilterStartDatetime().getLaterThan()
        );

        DueDatetimeFilter dueDatetimeFilter = new DueDatetimeFilter(
                request.getFilterDueDatetime().getEarlierInfinite(),
                request.getFilterDueDatetime().getLaterInfinite(),
                request.getFilterDueDatetime().getEarlierThan(),
                request.getFilterDueDatetime().getLaterThan()
        );

        ListEntityWithCount<TaskEntity> tasks = taskService.get(
                paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount(), teamFilter, statusFilter,
                chargeUserFilter, fileObjectsFilter, startDatetimeFilter, dueDatetimeFilter);

        List<Task> taskProtos = tasks.getData().stream()
                .map(ProtoMapper::convert).toList();
        GetTasksResponse response = GetTasksResponse.newBuilder()
                .addAllTasks(taskProtos)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(tasks.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTasksWithAttachments(GetTasksWithAttachmentsRequest request, StreamObserver<GetTasksWithAttachmentsResponse> responseObserver) {
        TaskWithFileObjectsSortType sortType = switch (request.getSort().getOrderField()) {
            case TASK_WITH_ATTACHMENTS_ORDER_FIELD_TITLE -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskWithFileObjectsSortType.TITLE_ASC : TaskWithFileObjectsSortType.TITLE_DESC;
            case TASK_WITH_ATTACHMENTS_ORDER_FIELD_START_DATETIME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskWithFileObjectsSortType.START_DATETIME_ASC : TaskWithFileObjectsSortType.START_DATETIME_DESC;
            case TASK_WITH_ATTACHMENTS_ORDER_FIELD_DUE_DATETIME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskWithFileObjectsSortType.DUE_DATETIME_ASC : TaskWithFileObjectsSortType.DUE_DATETIME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskWithFileObjectsSortType.CREATED_AT_ASC : TaskWithFileObjectsSortType.CREATED_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        TeamFilter teamFilter = new TeamFilter(
                request.getFilterTeam().getHasValue(), request.getFilterTeam().getTeamIdsList()
        );

        StatusFilter statusFilter = new StatusFilter(
                request.getFilterStatus().getHasValue(), request.getFilterStatus().getStatusesList()
        );

        ChargeUserFilter chargeUserFilter = new ChargeUserFilter(
                request.getFilterChargeUser().getHasValue(), request.getFilterChargeUser().getChargeUserIdsList()
        );

        FileObjectsFilter fileObjectsFilter = new FileObjectsFilter(
                request.getFilterFileObject().getHasValue(), request.getFilterFileObject().getAny(), request.getFilterFileObject().getFileObjectIdsList()
        );

        StartDatetimeFilter startDatetimeFilter = new StartDatetimeFilter(
                request.getFilterStartDatetime().getEarlierInfinite(),
                request.getFilterStartDatetime().getLaterInfinite(),
                request.getFilterStartDatetime().getEarlierThan(),
                request.getFilterStartDatetime().getLaterThan()
        );

        DueDatetimeFilter dueDatetimeFilter = new DueDatetimeFilter(
                request.getFilterDueDatetime().getEarlierInfinite(),
                request.getFilterDueDatetime().getLaterInfinite(),
                request.getFilterDueDatetime().getEarlierThan(),
                request.getFilterDueDatetime().getLaterThan()
        );

        ListEntityWithCount<TaskEntity> tasks = taskService.getWithAttachments(
                paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount(), teamFilter, statusFilter,
                chargeUserFilter, fileObjectsFilter, startDatetimeFilter, dueDatetimeFilter);

        List<TaskWithAttachments> taskProtos = tasks.getData().stream()
                .map(ProtoMapper::convertWithFileObjects).toList();
        GetTasksWithAttachmentsResponse response = GetTasksWithAttachmentsResponse.newBuilder()
                .addAllTasks(taskProtos)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(tasks.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPluralTasks(GetPluralTasksRequest request, StreamObserver<GetPluralTasksResponse> responseObserver) {
        TaskSortType sortType = switch (request.getSort().getOrderField()) {
            case TASK_ORDER_FIELD_TITLE -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskSortType.TITLE_ASC : TaskSortType.TITLE_DESC;
            case TASK_ORDER_FIELD_START_DATETIME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskSortType.START_DATETIME_ASC : TaskSortType.START_DATETIME_DESC;
            case TASK_ORDER_FIELD_DUE_DATETIME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskSortType.DUE_DATETIME_ASC : TaskSortType.DUE_DATETIME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskSortType.CREATED_AT_ASC : TaskSortType.CREATED_AT_DESC;
        };
        List<Task> taskProtos = this.taskService.getByTaskIds(
                        request.getTaskIdsList(), sortType).stream()
                .map(ProtoMapper::convert).toList();
        GetPluralTasksResponse response = GetPluralTasksResponse.newBuilder()
                .addAllTasks(taskProtos)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getFileObjectsOnTask(GetFileObjectsOnTaskRequest request, StreamObserver<GetFileObjectsOnTaskResponse> responseObserver) {
        FileObjectOnTaskSortType sortType = switch (request.getSort().getOrderField()) {
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    FileObjectOnTaskSortType.ADD_AT_ASC : FileObjectOnTaskSortType.ADD_AT_DESC;
        };
        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        ListEntityWithCount<TaskAttachmentEntity> tasks = taskService.getFileObjectsOnTask(
                request.getTaskId(), paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount());

        List<FileObjectOnTask> fileObjectOnTasks = tasks.getData().stream()
                .map(ProtoMapper::convert).toList();

        GetFileObjectsOnTaskResponse response = GetFileObjectsOnTaskResponse.newBuilder()
                .addAllFileObjects(fileObjectOnTasks)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(tasks.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTasksOnFileObject(GetTasksOnFileObjectRequest request, StreamObserver<GetTasksOnFileObjectResponse> responseObserver) {

        TaskOnFileObjectSortType sortType = switch (request.getSort().getOrderField()) {
            case TASK_ON_FILE_OBJECT_ORDER_FIELD_CREATE -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskOnFileObjectSortType.CREATED_AT_ASC : TaskOnFileObjectSortType.CREATED_AT_DESC;
            case TASK_ON_FILE_OBJECT_ORDER_FIELD_TITLE -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskOnFileObjectSortType.TITLE_ASC : TaskOnFileObjectSortType.TITLE_DESC;
            case TASK_ON_FILE_OBJECT_ORDER_FIELD_START_DATETIME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskOnFileObjectSortType.START_DATETIME_ASC : TaskOnFileObjectSortType.START_DATETIME_DESC;
            case TASK_ON_FILE_OBJECT_ORDER_FIELD_DUE_DATETIME -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskOnFileObjectSortType.DUE_DATETIME_ASC : TaskOnFileObjectSortType.DUE_DATETIME_DESC;
            default -> (request.getSort().getOrder() == SortOrder.SORT_ORDER_ASC) ?
                    TaskOnFileObjectSortType.ADD_AT_ASC : TaskOnFileObjectSortType.ADD_AT_DESC;
        };

        PaginationType paginationType;
        switch (request.getPagination().getType()) {
            case PAGINATION_TYPE_CURSOR -> paginationType = PaginationType.CURSOR;
            case PAGINATION_TYPE_OFFSET -> paginationType = PaginationType.OFFSET;
            default -> paginationType = PaginationType.NONE;
        }

        ListEntityWithCount<TaskAttachmentEntity> tasks = taskService.getTasksOnFileObject(
                request.getFileObjectId(), paginationType, request.getPagination().getLimit(), request.getPagination().getOffset(),
                request.getPagination().getCursor(), sortType, request.getWithCount());

        List<TaskOnFileObject> taskOnFileObjects = tasks.getData().stream()
                .map(ProtoMapper::convertOnFileObject).toList();

        GetTasksOnFileObjectResponse response = GetTasksOnFileObjectResponse.newBuilder()
                .addAllTasks(taskOnFileObjects)
                .setCount(
                        Count.newBuilder().setIsValid(request.getWithCount())
                                .setCount(tasks.getCount()).build()
                )
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createTask(CreateTaskRequest request, StreamObserver<CreateTaskResponse> responseObserver) {
        String operatorId = request.getOperatorId();
        TaskEntity task = new TaskEntity();
        task.setTeamId(request.getTeamId());
        task.setChargeUserId(request.getChargeUserId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDatetime(LocalDateTime.parse(request.getStartDatetime(), DateTimeFormatter.ISO_DATE_TIME));
        task.setDueDatetime(LocalDateTime.parse(request.getDueDatetime(), DateTimeFormatter.ISO_DATE_TIME));
        List<TaskAttachmentEntity> attachmentEntities = request.getAttachmentsList().stream()
                .map(attachment -> {
                    TaskAttachmentEntity attachmentEntity = new TaskAttachmentEntity();
                    attachmentEntity.setFileObjectId(attachment.getFileObjectId());
                    return attachmentEntity;
                })
                .toList();

        String jobId = taskService.beginCreate(operatorId, task, attachmentEntities);
        CreateTaskResponse response = CreateTaskResponse.newBuilder()
                .setJobId(jobId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateTaskStatus(UpdateTaskStatusRequest request, StreamObserver<UpdateTaskStatusResponse> responseObserver) {
        String operatorId = request.getOperatorId();
        String taskId = request.getTaskId();
        String status = request.getStatus();

        String jobId = taskService.beginUpdateStatus(operatorId, taskId, status);
        UpdateTaskStatusResponse response = UpdateTaskStatusResponse.newBuilder()
                .setJobId(jobId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
