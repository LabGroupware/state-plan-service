package org.cresplanex.api.state.planservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AlreadyExistTaskAttachmentException extends RuntimeException {

    private final String taskId;
    private final List<String> fileObjectIds;

    public AlreadyExistTaskAttachmentException(String taskId, List<String> fileObjectIds) {
        super("Already exist task attachments: " + fileObjectIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.taskId = taskId;
        this.fileObjectIds = fileObjectIds;
    }
}
