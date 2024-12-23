package org.cresplanex.api.state.planservice.exception;

import build.buf.gen.plan.v1.*;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@Slf4j
@GrpcAdvice
public class GrpcExceptionAdvice {

     @GrpcExceptionHandler(PlanNotFoundException.class)
     public Status handlePlanNotFoundException(PlanNotFoundException e) {
        PlanServicePlanNotFoundError.Builder descriptionBuilder =
                PlanServicePlanNotFoundError.newBuilder()
                .setMeta(buildErrorMeta(e));

        switch (e.getFindType()) {
            case BY_ID:
                descriptionBuilder
                        .setFindFieldType(PlanUniqueFieldType.PLAN_UNIQUE_FIELD_TYPE_PLAN_ID)
                        .setPlanId(e.getAggregateId());
                break;
        }

         return Status.NOT_FOUND
                 .withDescription(descriptionBuilder.build().toString())
                 .withCause(e);
     }

    @GrpcExceptionHandler(TaskNotFoundException.class)
    public Status handleTaskNotFoundException(TaskNotFoundException e) {
        PlanServiceTaskNotFoundError.Builder descriptionBuilder =
                PlanServiceTaskNotFoundError.newBuilder()
                        .setMeta(buildErrorMeta(e));

        switch (e.getFindType()) {
            case BY_ID:
                descriptionBuilder
                        .setFindFieldType(TaskUniqueFieldType.TASK_UNIQUE_FIELD_TYPE_TASK_ID)
                        .setTaskId(e.getAggregateId());
                break;
        }

        return Status.NOT_FOUND
                .withDescription(descriptionBuilder.build().toString())
                .withCause(e);
    }

     private PlanServiceErrorMeta buildErrorMeta(ServiceException e) {
         return PlanServiceErrorMeta.newBuilder()
                 .setCode(e.getServiceErrorCode())
                 .setMessage(e.getErrorCaption())
                 .build();
     }

    @GrpcExceptionHandler
    public Status handleInternal(Throwable e) {
         log.error("Internal error", e);

        String message = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";

         PlanServiceInternalError.Builder descriptionBuilder =
                 PlanServiceInternalError.newBuilder()
                         .setMeta(PlanServiceErrorMeta.newBuilder()
                                 .setCode(PlanServiceErrorCode.PLAN_SERVICE_ERROR_CODE_INTERNAL)
                                 .setMessage(message)
                                 .build());

         return Status.INTERNAL
                 .withDescription(descriptionBuilder.build().toString())
                 .withCause(e);
    }
}
