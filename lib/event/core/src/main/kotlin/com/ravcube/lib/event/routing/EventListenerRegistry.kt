package com.ravcube.lib.event.routing

import com.ravcube.lib.event.DomainEvent

class EventListenerRegistry private constructor(
    private val listeners: Map<EventSource, EventSourceListenerRegistry>
) {

    fun <E : DomainEvent> on(source: EventSource, event: E) {
        listeners[source]?.on(event)
    }

    fun topics(source: EventSource): List<String> = listeners[source]?.topics().orEmpty()

    companion object {
        @JvmStatic
        fun of(listeners: List<AbstractEventListener<out DomainEvent>>): EventListenerRegistry =
            EventListenerRegistry(
                listeners
                    .groupBy { listener -> listener.source() }
                    .mapValues { (_, sourceListeners) ->
                        EventSourceListenerRegistry.of(sourceListeners)
                    }
            )
    }
}
