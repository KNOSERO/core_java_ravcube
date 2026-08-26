package com.ravcube.lib.event.routing

import com.ravcube.lib.event.OtherEvent
import com.ravcube.lib.event.SampleEvent
import com.ravcube.lib.event.SubEvent
import com.ravcube.lib.event.publisher.AnotherSampleEventPublisher
import com.ravcube.lib.event.publisher.BaseEventPublisher
import com.ravcube.lib.event.publisher.OtherEventPublisher
import com.ravcube.lib.event.publisher.SampleEventPublisher
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class EventPublisherRegistryTest {

    @Test
    fun firstPublisherForTheEventTypePublishesTheEvent() {
        val first = SampleEventPublisher()
        val second = AnotherSampleEventPublisher()
        val otherType = OtherEventPublisher()
        val registry = EventPublisherRegistry.of(listOf(first, second, otherType))

        registry.publish(SampleEvent("sample"))

        assertEquals(1, first.calls)
        assertEquals(0, second.calls)
        assertEquals(0, otherType.calls)
    }

    @Test
    fun eventIsIgnoredWhenNoPublisherIsRegisteredForItsType() {
        val publisher = OtherEventPublisher()
        val registry = EventPublisherRegistry.of(listOf(publisher))

        registry.publish(SampleEvent("sample"))

        assertEquals(0, publisher.calls)
    }

    @Test
    fun subtypeIsIgnoredWhenOnlyBaseTypePublisherIsRegistered() {
        val publisher = BaseEventPublisher()
        val registry = EventPublisherRegistry.of(listOf(publisher))

        registry.publish(SubEvent("sub"))

        assertEquals(0, publisher.calls)
    }

    @Test
    fun emptyRegistryIgnoresEvents() {
        val registry = EventPublisherRegistry.of(emptyList())

        assertDoesNotThrow {
            registry.publish(SampleEvent("sample"))
            registry.publish(OtherEvent("other"))
            registry.publish(SubEvent("sub"))
        }
    }
}
