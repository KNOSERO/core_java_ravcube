package com.ravcube.lib.event

import com.ravcube.lib.event.enums.EventSource
import com.ravcube.lib.event.inteface.AbstractListener

class OtherEventKafkaListener : AbstractListener<OtherEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.KAFKA_AFTER_COMMIT

    override fun on(event: OtherEvent) {
        calls++
    }
}
