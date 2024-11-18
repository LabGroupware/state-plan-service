package org.cresplanex.api.state.planservice.handler;

import build.buf.gen.plan.v1.*;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
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

    // TODO: pagination + with count
    @Override
    public void getTasks(GetTasksRequest request, StreamObserver<GetTasksResponse> responseObserver) {
        List<TaskEntity> tasks = taskService.get();

        List<Task> taskProtos = tasks.stream()
                .map(ProtoMapper::convert).toList();
        GetTasksResponse response = GetTasksResponse.newBuilder()
                .addAllTasks(taskProtos)
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
