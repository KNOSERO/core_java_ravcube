package com.ravcube.lib.stream.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Profile("kafka")
public final class ClientStreamKafkaProperties {

    private static final Pattern KAFKA_NAME = Pattern.compile("[a-zA-Z0-9._-]+");

    private final String serviceName;
    private final String instanceId;

    public ClientStreamKafkaProperties(
            @Value("${ravcube.stream.kafka.service-name:${spring.application.name:}}") String serviceName,
            @Value("${ravcube.stream.kafka.instance-id:}") String configuredInstanceId,
            @Value("${HOSTNAME:}") String hostname
    ) {
        this.serviceName = requireKafkaName(serviceName, "service-name");
        this.instanceId = requireKafkaName(
                firstNonBlank(configuredInstanceId, hostname, UUID.randomUUID().toString()),
                "instance-id"
        );
    }

    public String topic() {
        return "stream.resource.refresh." + serviceName;
    }

    public String commitTopic() {
        return topic() + ".commit";
    }

    public String consumerGroup() {
        return "stream-refresh." + serviceName + "." + instanceId;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new IllegalArgumentException("at least one value must be present");
    }

    private static String requireKafkaName(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank() || !KAFKA_NAME.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    name + " must contain only letters, digits, '.', '_' or '-'\n"
            );
        }
        return value;
    }
}
