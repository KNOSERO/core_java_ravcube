package com.ravcube.lib.stream.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ClientStreamKafkaPropertiesTest {

    @Test
    void podsOfSameServiceUseOneTopicAndDifferentConsumerGroups() {
        final ClientStreamKafkaProperties firstPod =
                new ClientStreamKafkaProperties("claims-service", "pod-1", "");
        final ClientStreamKafkaProperties secondPod =
                new ClientStreamKafkaProperties("claims-service", "pod-2", "");

        assertEquals(firstPod.topic(), secondPod.topic());
        assertEquals("stream.resource.refresh.claims-service.commit", firstPod.commitTopic());
        assertNotEquals(firstPod.consumerGroup(), secondPod.consumerGroup());
    }

    @Test
    void differentServicesUseDifferentTopics() {
        final ClientStreamKafkaProperties claims =
                new ClientStreamKafkaProperties("claims-service", "pod-1", "");
        final ClientStreamKafkaProperties payments =
                new ClientStreamKafkaProperties("payments-service", "pod-1", "");

        assertNotEquals(claims.topic(), payments.topic());
        assertNotEquals(claims.commitTopic(), payments.commitTopic());
    }
}
