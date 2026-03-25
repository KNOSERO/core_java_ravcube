package com.ravcube.lib.event

import com.ravcube.lib.event.publisher.AnotherSampleEventPublisher
import com.ravcube.lib.event.publisher.BaseEventPublisher
import com.ravcube.lib.event.publisher.OtherEventPublisher
import com.ravcube.lib.event.publisher.SampleEventPublisher
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class StoragePublisherTest {

    @Test
    fun shouldPublishUsingFirstPublisherForMatchingEventType() {
        // given
        val first = SampleEventPublisher()
        val second = AnotherSampleEventPublisher()
        val otherType = OtherEventPublisher()
        val storagePublisher = StoragePublisher.of(listOf(first, second, otherType))
        val event = SampleEvent("sample")

        // when
        storagePublisher.publish(event)

        // then
        assertEquals(1, first.calls)
        assertEquals(0, second.calls)
        assertEquals(0, otherType.calls)
    }

    @Test
    fun shouldDoNothingWhenNoPublisherRegisteredForEventType() {
        // given
        val otherType = OtherEventPublisher()
        val storagePublisher = StoragePublisher.of(listOf(otherType))

        // when
        storagePublisher.publish(SampleEvent("sample"))

        // then
        assertEquals(0, otherType.calls)
    }

    @Test
    fun shouldDoNothingForSubtypeWhenOnlyExactBaseTypePublisherIsRegistered() {
        // given
        val basePublisher = BaseEventPublisher()
        val storagePublisher = StoragePublisher.of(listOf(basePublisher))

        // when
        storagePublisher.publish(SubEvent("sub"))

        // then
        assertEquals(0, basePublisher.calls)
    }

    @Test
    fun shouldNotThrowWhenStoragePublisherIsBuiltWithEmptyList() {
        // given
        val storagePublisher = StoragePublisher.of(emptyList())

        // when/then
        assertDoesNotThrow {
            storagePublisher.publish(SampleEvent("sample"))
            storagePublisher.publish(OtherEvent("other"))
            storagePublisher.publish(SubEvent("sub"))
        }
    }
}
