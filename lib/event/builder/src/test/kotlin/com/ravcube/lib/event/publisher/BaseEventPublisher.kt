package com.ravcube.lib.event.publisher

import com.ravcube.lib.event.inteface.AbstractPublisher
import com.ravcube.lib.event.BaseEvent
import com.ravcube.lib.event.enums.EventSource

class BaseEventPublisher : AbstractPublisher<BaseEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.KAFKA_AFTER_COMMIT

    override fun publish(event: BaseEvent) {
        calls++
    }
}