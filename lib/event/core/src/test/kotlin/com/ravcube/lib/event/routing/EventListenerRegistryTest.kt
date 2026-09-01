package com.ravcube.lib.event.routing

import com.ravcube.lib.event.AnotherSampleEventKafkaListener
import com.ravcube.lib.event.BaseEventKafkaListener
import com.ravcube.lib.event.OtherEventKafkaListener
import com.ravcube.lib.event.SampleEvent
import com.ravcube.lib.event.SampleEventCommitListener
import com.ravcube.lib.event.SampleEventKafkaListener
import com.ravcube.lib.event.SubEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class EventListenerRegistryTest {

    @Test
    fun everyMatchingListenerReceivesTheEvent() {
        val first = SampleEventKafkaListener()
        val second = AnotherSampleEventKafkaListener()
        val registry = EventListenerRegistry.of(listOf(first, second))

        registry.on(EventSource.KAFKA_AFTER_COMMIT, SampleEvent("sample"))

        assertEquals(1, first.calls)
        assertEquals(1, second.calls)
    }

    @Test
    fun listenerReceivesOnlyEventsForItsSourceAndType() {
        val matchingFirst = SampleEventKafkaListener()
        val matchingSecond = AnotherSampleEventKafkaListener()
        val wrongSource = SampleEventCommitListener()
        val wrongType = OtherEventKafkaListener()
        val registry = EventListenerRegistry.of(
            listOf(matchingFirst, matchingSecond, wrongSource, wrongType)
        )

        registry.on(EventSource.KAFKA_AFTER_COMMIT, SampleEvent("sample"))

        assertEquals(1, matchingFirst.calls)
        assertEquals(1, matchingSecond.calls)
        assertEquals(0, wrongSource.calls)
        assertEquals(0, wrongType.calls)
    }

    @Test
    fun eventIsIgnoredWhenItsSourceHasNoListeners() {
        val listener = SampleEventCommitListener()
        val registry = EventListenerRegistry.of(listOf(listener))

        registry.on(EventSource.KAFKA_AFTER_COMMIT, SampleEvent("sample"))

        assertEquals(0, listener.calls)
    }

    @Test
    fun eventIsIgnoredWhenItsTypeHasNoListener() {
        val listener = OtherEventKafkaListener()
        val registry = EventListenerRegistry.of(listOf(listener))

        registry.on(EventSource.KAFKA_AFTER_COMMIT, SampleEvent("sample"))

        assertEquals(0, listener.calls)
    }

    @Test
    fun subtypeIsIgnoredWhenOnlyBaseTypeListenerIsRegistered() {
        val listener = BaseEventKafkaListener()
        val registry = EventListenerRegistry.of(listOf(listener))

        registry.on(EventSource.KAFKA_AFTER_COMMIT, SubEvent("sub"))

        assertEquals(0, listener.calls)
    }

    @Test
    fun emptyRegistryIgnoresEvents() {
        val registry = EventListenerRegistry.of(emptyList())

        registry.on(EventSource.KAFKA_AFTER_COMMIT, SampleEvent("sample"))
    }
}
