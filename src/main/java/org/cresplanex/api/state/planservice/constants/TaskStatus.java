package org.cresplanex.api.state.planservice.constants;

public class TaskStatus {

    public static final String PREPARE = "prepare";
    public static final String IN_PROGRESS = "in_progress";
    public static final String DONE = "done";
    public static final String CANCEL = "cancel";

    public static final String[] ALL = {PREPARE, IN_PROGRESS, DONE, CANCEL};

    public static final String DEFAULT = PREPARE;
}
