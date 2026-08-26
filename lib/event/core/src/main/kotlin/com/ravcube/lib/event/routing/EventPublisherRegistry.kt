package com.ravcube.lib.event.routing

import com.ravcube.lib.event.DomainEvent

class EventPublisherRegistry private constructor(
    private val publishers: Map<Class<out DomainEvent>, AbstractEventPublisher<out DomainEvent>>
) {
    fun <E : DomainEvent> publish(event: E) {
        val publisher = publishers[event.javaClass] ?: return
        @Suppress("UNCHECKED_CAST")
        (publisher as AbstractEventPublisher<E>).publish(event)
    }

    companion object {
        @JvmStatic
        fun of(
            publishers: List<AbstractEventPublisher<out DomainEvent>>
        ): EventPublisherRegistry =
            EventPublisherRegistry(
                buildMap {
                    publishers.forEach { publisher ->
                        putIfAbsent(publisher.eventType(), publisher)
                    }
                }
            )
    }
}
