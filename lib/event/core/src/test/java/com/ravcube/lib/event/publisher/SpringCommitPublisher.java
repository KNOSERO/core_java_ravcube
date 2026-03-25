package com.ravcube.lib.event.publisher;

import com.ravcube.lib.event.domain.SpringDomainEvent;
import org.springframework.stereotype.Component;

@Component
public class SpringCommitPublisher extends DefaultCommitPublisher<SpringDomainEvent> {
}
