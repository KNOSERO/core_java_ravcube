package com.ravcube.lib.event

import com.ravcube.lib.event.enums.EventSource
import com.ravcube.lib.event.inteface.AbstractListener

class StorageListener private constructor(
    private val listeners: Map<EventSource, StorageSourceListener>
) {

    fun <E : DomainEvent> on(type: EventSource, event: E) {
        listeners[type]?.on(event)
    }

    fun getTopics(type: EventSource): List<String> = listeners[type]?.getTopics().orEmpty()

    companion object {
        @JvmStatic
        fun of(listeners: List<AbstractListener<out DomainEvent>>): StorageListener =
            StorageListener(
                listeners
                    .groupBy { listener -> listener.source() }
                    .mapValues { (_, sourceListeners) -> StorageSourceListener.of(sourceListeners) }
            )
    }
}
