package org.cresplanex.api.state.planservice.saga.proxy;

import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.command.plan.CreateTaskAndAttachInitialFIleObjectsCommand;
import org.cresplanex.api.state.common.saga.command.plan.UpdateStatusTaskCommand;
import org.cresplanex.core.saga.simpledsl.CommandEndpoint;
import org.cresplanex.core.saga.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class PlanServiceProxy {

    public final CommandEndpoint<CreateTaskAndAttachInitialFIleObjectsCommand.Exec> createTaskAndAttachInitialFIleObjects
            = CommandEndpointBuilder
            .forCommand(CreateTaskAndAttachInitialFIleObjectsCommand.Exec.class)
            .withChannel(SagaCommandChannel.PLAN)
            .withCommandType(CreateTaskAndAttachInitialFIleObjectsCommand.Exec.TYPE)
            .build();

    public final CommandEndpoint<CreateTaskAndAttachInitialFIleObjectsCommand.Undo> undoCreateTaskAndAttachInitialFIleObjects
            = CommandEndpointBuilder
            .forCommand(CreateTaskAndAttachInitialFIleObjectsCommand.Undo.class)
            .withChannel(SagaCommandChannel.PLAN)
            .withCommandType(CreateTaskAndAttachInitialFIleObjectsCommand.Undo.TYPE)
            .build();

    public final CommandEndpoint<UpdateStatusTaskCommand.Exec> updateStatusTask
            = CommandEndpointBuilder
            .forCommand(UpdateStatusTaskCommand.Exec.class)
            .withChannel(SagaCommandChannel.PLAN)
            .withCommandType(UpdateStatusTaskCommand.Exec.TYPE)
            .build();

    public final CommandEndpoint<UpdateStatusTaskCommand.Undo> undoUpdateStatusTask
            = CommandEndpointBuilder
            .forCommand(UpdateStatusTaskCommand.Undo.class)
            .withChannel(SagaCommandChannel.PLAN)
            .withCommandType(UpdateStatusTaskCommand.Undo.TYPE)
            .build();
}
