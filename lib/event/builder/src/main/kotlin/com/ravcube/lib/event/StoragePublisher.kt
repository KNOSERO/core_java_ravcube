package com.ravcube.lib.event

import com.ravcube.lib.event.inteface.AbstractPublisher

class StoragePublisher private constructor(
    private val publishers: Map<Class<out DomainEvent>, AbstractPublisher<out DomainEvent>>
) {
    fun <E : DomainEvent> publish(event: E) {
        val publisher = publishers[event.javaClass] ?: return
        @Suppress("UNCHECKED_CAST")
        (publisher as AbstractPublisher<E>).publish(event)
    }

    companion object {
        @JvmStatic
        fun of(publishers: List<AbstractPublisher<out DomainEvent>>): StoragePublisher =
            StoragePublisher(
                buildMap {
                    publishers.forEach { publisher ->
                        putIfAbsent(publisher.domainClass(), publisher)
                    }
                }
            )
    }
}
