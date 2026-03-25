package com.ravcube.lib.event.publisher

import com.ravcube.lib.event.inteface.AbstractPublisher
import com.ravcube.lib.event.enums.EventSource
import com.ravcube.lib.event.SampleEvent

class AnotherSampleEventPublisher : AbstractPublisher<SampleEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.SPRING_AFTER_COMMIT

    override fun publish(event: SampleEvent) {
        calls++
    }
}