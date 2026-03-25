package com.ravcube.lib.event

import com.ravcube.lib.event.inteface.AbstractListener

class StorageSourceListener private constructor(
    private val listeners: Map<Class<out DomainEvent>, List<AbstractListener<out DomainEvent>>>
) {

    fun <E : DomainEvent> on(event: E) {
        listeners[event.javaClass]
            .orEmpty()
            .forEach { listeners ->
                @Suppress("UNCHECKED_CAST")
                (listeners as AbstractListener<E>).on(event)
            }
    }

    fun getTopics(): List<String>  {
        return listeners.keys.map { eventType -> DomainEvent.getTopic(eventType) }
    }

    companion object {

        @JvmStatic
        fun of(listeners: List<AbstractListener<out DomainEvent>>): StorageSourceListener {
            return StorageSourceListener(
                listeners.groupBy {
                        listener -> listener.domainClass()
                }
            )
        }
    }
}
