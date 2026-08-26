package com.ravcube.lib.event.publisher

import com.ravcube.lib.event.OtherEvent
import com.ravcube.lib.event.routing.AbstractEventPublisher
import com.ravcube.lib.event.routing.EventSource

class OtherEventPublisher : AbstractEventPublisher<OtherEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.SPRING_AFTER_ROLLBACK

    override fun publish(event: OtherEvent) {
        calls++
    }
}
