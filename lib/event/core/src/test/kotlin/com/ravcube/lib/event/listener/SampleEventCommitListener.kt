package com.ravcube.lib.event

import com.ravcube.lib.event.routing.AbstractEventListener
import com.ravcube.lib.event.routing.EventSource

class SampleEventCommitListener : AbstractEventListener<SampleEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.SPRING_AFTER_COMMIT

    override fun on(event: SampleEvent) {
        calls++
    }
}
