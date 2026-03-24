package com.ravcube.lib.event.enums;

public enum EventSource {
    SPRING_AFTER_COMMIT("", ""),
    SPRING_AFTER_ROLLBACK("", ""),
    KAFKA_AFTER_COMMIT("", ".commit"),
    KAFKA_AFTER_ROLLBACK("", ".rollback");

    private final String prefix;
    private final String suffix;

    EventSource(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String formatTopic(String topic) {
        return this.prefix + topic + this.suffix;
    }
}
