package com.ravcube.lib.event.publisher

import com.ravcube.lib.event.routing.AbstractEventPublisher
import com.ravcube.lib.event.routing.EventSource
import com.ravcube.lib.event.SampleEvent

class AnotherSampleEventPublisher : AbstractEventPublisher<SampleEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.SPRING_AFTER_COMMIT

    override fun publish(event: SampleEvent) {
        calls++
    }
}
