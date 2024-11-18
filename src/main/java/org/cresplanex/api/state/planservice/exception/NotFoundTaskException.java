package org.cresplanex.api.state.planservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class NotFoundTaskException extends RuntimeException {

    private final List<String> taskIds;

    public NotFoundTaskException(List<String> taskIds) {
        super("Not found task with taskIds: " + taskIds.stream().reduce((a, b) -> a + ", " + b).orElse(""));
        this.taskIds = taskIds;
    }
}
