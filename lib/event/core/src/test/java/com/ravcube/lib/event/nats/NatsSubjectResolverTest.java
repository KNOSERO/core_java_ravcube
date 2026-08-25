package com.ravcube.lib.event.nats;

import com.ravcube.lib.event.domain.NatsDomainEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NatsSubjectResolverTest {

    @Test
    void shouldBuildSubjectFromServicePrefixAndEventTopic() {
        NatsProperties properties = new NatsProperties();
        properties.setSubjectPrefix("claims-service.");

        NatsSubjectResolver resolver = new NatsSubjectResolver(properties);

        assertEquals("claims-service.nats.event", resolver.subject(NatsDomainEvent.class));
    }

    @Test
    void shouldRejectWildcardSubjectPrefix() {
        NatsProperties properties = new NatsProperties();
        properties.setSubjectPrefix("claims.*");

        assertThrows(IllegalArgumentException.class, () -> new NatsSubjectResolver(properties));
    }
}
