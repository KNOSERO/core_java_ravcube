package com.ravcube.lib.stream.domain

import java.util.Collections
import java.util.TreeSet

class ClientStreamSubscription(
    resourceName: String?,
    resourceIds: Set<String>?
) {
    val resourceName: String = requireText(resourceName, "resourceName")
    val resourceIds: Set<String> = normalizeIds(resourceIds)

    fun accepts(name: String?, id: String?): Boolean =
        resourceName == name && id != null && resourceIds.contains(id)

    private companion object {
        fun normalizeIds(values: Set<String>?): Set<String> {
            val nonNullValues = values
                ?: throw NullPointerException("resourceIds must not be null")
            val normalized = TreeSet<String>()

            for (value in nonNullValues) {
                normalized.add(requireText(value, "resourceId"))
            }

            require(normalized.isNotEmpty()) {
                "resourceIds must not be empty"
            }

            return Collections.unmodifiableSet(normalized)
        }

        fun requireText(value: String?, name: String): String {
            val nonNullValue = value
                ?: throw NullPointerException(name + " must not be null")
            require(nonNullValue.isNotBlank()) {
                name + " must not be blank"
            }
            return nonNullValue
        }
    }
}
