package org.cresplanex.api.state.planservice.saga.proxy;

import org.cresplanex.api.state.common.saga.SagaCommandChannel;
import org.cresplanex.api.state.common.saga.validate.storage.FileObjectExistValidateCommand;
import org.cresplanex.api.state.common.saga.validate.team.TeamExistValidateCommand;
import org.cresplanex.core.saga.simpledsl.CommandEndpoint;
import org.cresplanex.core.saga.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class StorageServiceProxy {

    public final CommandEndpoint<FileObjectExistValidateCommand> fileObjectExistValidate
            = CommandEndpointBuilder
            .forCommand(FileObjectExistValidateCommand.class)
            .withChannel(SagaCommandChannel.STORAGE)
            .withCommandType(FileObjectExistValidateCommand.TYPE)
            .build();
}
