package com.ravcube.lib.event

import com.ravcube.lib.event.enums.EventSource
import kotlin.test.Test
import kotlin.test.assertEquals

class StorageListenerTest {

    @Test
    fun shouldInvokeEachMatchingListenerExactlyOnce() {
        // given
        val first = SampleEventKafkaListener()
        val second = AnotherSampleEventKafkaListener()
        val storageListener = StorageListener.of(listOf(first, second))
        val event = SampleEvent("sample")

        // when
        storageListener.on(EventSource.KAFKA_AFTER_COMMIT, event)

        // then
        assertEquals(1, first.calls)
        assertEquals(1, second.calls)
    }

    @Test
    fun shouldInvokeOnlyListenersMatchingSourceAndEventType() {
        // given
        val matchingFirst = SampleEventKafkaListener()
        val matchingSecond = AnotherSampleEventKafkaListener()
        val wrongSource = SampleEventCommitListener()
        val wrongType = OtherEventKafkaListener()
        val storageListener = StorageListener.of(listOf(matchingFirst, matchingSecond, wrongSource, wrongType))
        val event = SampleEvent("sample")

        // when
        storageListener.on(EventSource.KAFKA_AFTER_COMMIT, event)

        // then
        assertEquals(1, matchingFirst.calls)
        assertEquals(1, matchingSecond.calls)
        assertEquals(0, wrongSource.calls)
        assertEquals(0, wrongType.calls)
    }

    @Test
    fun shouldDoNothingWhenNoListenersRegisteredForSource() {
        // given
        val listener = SampleEventCommitListener()
        val storageListener = StorageListener.of(listOf(listener))

        // when
        storageListener.on(EventSource.KAFKA_AFTER_COMMIT, SampleEvent("sample"))

        // then
        assertEquals(0, listener.calls)
    }

    @Test
    fun shouldDoNothingWhenSourceExistsButNoListenerForEventType() {
        // given
        val otherTypeKafkaListener = OtherEventKafkaListener()
        val storageListener = StorageListener.of(listOf(otherTypeKafkaListener))

        // when
        storageListener.on(EventSource.KAFKA_AFTER_COMMIT, SampleEvent("sample"))

        // then
        assertEquals(0, otherTypeKafkaListener.calls)
    }

    @Test
    fun shouldDoNothingForSubtypeWhenOnlyBaseTypeListenerIsRegistered() {
        // given
        val baseKafkaListener = BaseEventKafkaListener()
        val storageListener = StorageListener.of(listOf(baseKafkaListener))

        // when
        storageListener.on(EventSource.KAFKA_AFTER_COMMIT, SubEvent("sub"))

        // then
        assertEquals(0, baseKafkaListener.calls)
    }

    @Test
    fun shouldDoNothingWhenStorageListenerIsBuiltWithEmptyList() {
        // given
        val storageListener = StorageListener.of(emptyList())

        // when
        storageListener.on(EventSource.KAFKA_AFTER_COMMIT, SampleEvent("sample"))

        // then
        // no exception
    }
}
