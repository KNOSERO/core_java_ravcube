package com.ravcube.lib.event

import com.ravcube.lib.event.enums.EventSource
import com.ravcube.lib.event.inteface.AbstractListener

class SampleEventCommitListener : AbstractListener<SampleEvent> {
    var calls: Int = 0
        private set

    override fun source(): EventSource = EventSource.SPRING_AFTER_COMMIT

    override fun on(event: SampleEvent) {
        calls++
    }
}
