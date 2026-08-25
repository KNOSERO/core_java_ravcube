package com.ravcube.lib.stream.infrastructure.sse;

import com.ravcube.lib.stream.infrastructure.config.ClientStreamProperties;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public final class ClientStreamRegistryTestSupport {

    private ClientStreamRegistryTestSupport() {
    }

    public static ClientStreamProperties properties() {
        return new ClientStreamProperties(Duration.ofMinutes(10));
    }

    public static ClientStreamRegistry registry(RecordingSseEmitter... emitters) {
        final Deque<SseEmitter> availableEmitters = new ArrayDeque<>(Arrays.asList(emitters));
        return new ClientStreamRegistry(
                properties(),
                timeout -> availableEmitters.removeFirst()
        );
    }

    public static final class RecordingSseEmitter extends SseEmitter {

        private int eventCount;

        public int eventCount() {
            return eventCount;
        }

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            eventCount++;
        }
    }
}
