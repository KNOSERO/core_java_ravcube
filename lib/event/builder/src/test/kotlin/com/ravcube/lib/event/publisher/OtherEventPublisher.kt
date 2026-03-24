package com.ravcube.lib.event.publisher

import com.ravcube.lib.event.inteface.AbstractPublisher
import com.ravcube.lib.event.enums.EventSource
import com.ravcube.lib.event.OtherEvent

class OtherEventPublisher : AbstractPublisher<OtherEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.SPRING_AFTER_ROLLBACK

    override fun publish(event: OtherEvent) {
        calls++
    }
}