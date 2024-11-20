package org.cresplanex.api.state.planservice.filter.task;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StartDatetimeFilter {

    private boolean earlierInfinity;
    private boolean laterInfinity;
    private String earlierThan;
    private String laterThan;
}
