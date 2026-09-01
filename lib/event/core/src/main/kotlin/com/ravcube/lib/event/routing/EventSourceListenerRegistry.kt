package com.ravcube.lib.event.routing

import com.ravcube.lib.event.DomainEvent

internal class EventSourceListenerRegistry private constructor(
    private val listeners: Map<
        Class<out DomainEvent>,
        List<AbstractEventListener<out DomainEvent>>
    >
) {

    fun <E : DomainEvent> on(event: E) {
        listeners[event.javaClass]
            .orEmpty()
            .forEach { listener ->
                @Suppress("UNCHECKED_CAST")
                (listener as AbstractEventListener<E>).on(event)
            }
    }

    fun topics(): List<String> = listeners.keys.map(DomainEvent::getTopic)

    companion object {
        @JvmStatic
        fun of(
            listeners: List<AbstractEventListener<out DomainEvent>>
        ): EventSourceListenerRegistry =
            EventSourceListenerRegistry(
                listeners.groupBy { listener -> listener.eventType() }
            )
    }
}
