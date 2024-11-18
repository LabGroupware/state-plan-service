package org.cresplanex.api.state.planservice.event.publisher;

import org.cresplanex.api.state.common.event.EventAggregateType;
import org.cresplanex.api.state.common.event.model.plan.TaskDomainEvent;
import org.cresplanex.api.state.common.event.publisher.AggregateDomainEventPublisher;
import org.cresplanex.api.state.planservice.entity.TaskEntity;
import org.cresplanex.core.events.publisher.DomainEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class TaskDomainEventPublisher extends AggregateDomainEventPublisher<TaskEntity, TaskDomainEvent> {

    public TaskDomainEventPublisher(DomainEventPublisher eventPublisher) {
        super(eventPublisher, TaskEntity.class, EventAggregateType.PLAN_TASK);
    }
}
