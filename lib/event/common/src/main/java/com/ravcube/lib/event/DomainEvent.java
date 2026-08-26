package com.ravcube.lib.event;

import com.ravcube.lib.event.annotation.Topic;

public interface DomainEvent {

    static String getTopic(Class<? extends DomainEvent> eventType) {
        Topic topic = eventType.getAnnotation(Topic.class);

        if (topic == null) {
            throw new IllegalArgumentException(
                    "Missing @Topic on event type: " + eventType.getName()
            );
        }
        return topic.value();
    }

    default String getKey() {
        return "";
    }
}
