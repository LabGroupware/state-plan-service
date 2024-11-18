package org.cresplanex.api.state.planservice.handler;

import build.buf.gen.organization.v1.*;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.api.state.planservice.entity.TaskAttachmentEntity;
import org.cresplanex.api.state.planservice.mapper.proto.ProtoMapper;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.cresplanex.api.state.planservice.service.OrganizationService;

import java.util.List;

@RequiredArgsConstructor
@GrpcService
public class PlanServiceHandler extends OrganizationServiceGrpc.OrganizationServiceImplBase {

    private final OrganizationService organizationService;

    @Override
    public void findOrganization(FindOrganizationRequest request, StreamObserver<FindOrganizationResponse> responseObserver) {
        TaskEntity organization = organizationService.findById(request.getOrganizationId());

        Organization organizationProto = ProtoMapper.convert(organization);
        FindOrganizationResponse response = FindOrganizationResponse.newBuilder()
                .setOrganization(organizationProto)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // TODO: pagination + with count
    @Override
    public void getOrganizations(GetOrganizationsRequest request, StreamObserver<GetOrganizationsResponse> responseObserver) {
        List<TaskEntity> organizations = organizationService.get();

        List<Organization> organizationProtos = organizations.stream()
                .map(ProtoMapper::convert).toList();
        GetOrganizationsResponse response = GetOrganizationsResponse.newBuilder()
                .addAllOrganizations(organizationProtos)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createOrganization(CreateOrganizationRequest request, StreamObserver<CreateOrganizationResponse> responseObserver) {
        String operatorId = request.getOperatorId();
        TaskEntity organization = new TaskEntity();
        organization.setName(request.getName());
        organization.setPlan(request.getPlan());
        List<TaskAttachmentEntity> users = request.getUsersList().stream()
                .map(user -> {
                    TaskAttachmentEntity userEntity = new TaskAttachmentEntity();
                    userEntity.setUserId(user.getUserId());
                    return userEntity;
                })
                .toList();

        String jobId = organizationService.beginCreate(operatorId, organization, users);
        CreateOrganizationResponse response = CreateOrganizationResponse.newBuilder()
                .setJobId(jobId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addOrganizationUser(AddOrganizationUserRequest request, StreamObserver<AddOrganizationUserResponse> responseObserver) {
        String operatorId = request.getOperatorId();
        String organizationId = request.getOrganizationId();
        List<TaskAttachmentEntity> users = request.getUsersList().stream()
                .map(user -> {
                    TaskAttachmentEntity userEntity = new TaskAttachmentEntity();
                    userEntity.setUserId(user.getUserId());
                    return userEntity;
                })
                .toList();

        String jobId = organizationService.beginAddUsers(operatorId, organizationId, users);
        AddOrganizationUserResponse response = AddOrganizationUserResponse.newBuilder()
                .setJobId(jobId)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
