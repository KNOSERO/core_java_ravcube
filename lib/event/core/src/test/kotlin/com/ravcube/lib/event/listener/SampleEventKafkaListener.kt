package com.ravcube.lib.event

import com.ravcube.lib.event.routing.AbstractEventListener
import com.ravcube.lib.event.routing.EventSource

class SampleEventKafkaListener : AbstractEventListener<SampleEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.KAFKA_AFTER_COMMIT

    override fun on(event: SampleEvent) {
        calls++
    }
}
