package org.cresplanex.api.state.planservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class NotFoundTaskAttachmentException extends RuntimeException {

    private final String taskId;
    private final List<String> fileObjectIds;

    public NotFoundTaskAttachmentException(String taskId, List<String> fileObjectIds) {
        super("Not found task fileObject with fileObjectIds: " + fileObjectIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.taskId = taskId;
        this.fileObjectIds = fileObjectIds;
    }
}
