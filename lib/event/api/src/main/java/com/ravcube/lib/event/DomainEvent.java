package com.ravcube.lib.event;

import com.ravcube.lib.event.annotation.Topic;

public interface DomainEvent {

    static String getTopic(Class<? extends DomainEvent> clazz) {
        Topic topic = clazz.getAnnotation(Topic.class);

        if (topic == null) {
            throw new IllegalArgumentException("Missing @Topic on event type: " + clazz.getName());
        }
        return topic.value();
    }

    default String getKey() {
        return "";
    }
}
