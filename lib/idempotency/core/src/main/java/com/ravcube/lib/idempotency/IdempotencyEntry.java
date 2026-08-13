package com.ravcube.lib.idempotency;

import io.github.josipmusa.idempotency.core.IdempotencyContext;
import io.github.josipmusa.idempotency.core.StoredResponse;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

record IdempotencyEntry(
        IdempotencyEntryStatus status,
        int statusCode,
        Map<String, List<String>> headers,
        byte[] body,
        Instant completedAt,
        Instant lockExpiresAt,
        Duration ttl,
        String requestFingerprint
) implements Serializable {

    IdempotencyEntry {
        headers = copyHeaders(headers);
        body = body.clone();
    }

    static IdempotencyEntry inProgress(IdempotencyContext context, Instant now) {
        return new IdempotencyEntry(
                IdempotencyEntryStatus.IN_PROGRESS,
                0,
                Map.of(),
                new byte[0],
                null,
                now.plus(context.lockTimeout()),
                context.ttl(),
                context.requestFingerprint()
        );
    }

    IdempotencyEntry complete(StoredResponse response) {
        return new IdempotencyEntry(
                IdempotencyEntryStatus.COMPLETE,
                response.statusCode(),
                response.headers(),
                response.body(),
                response.completedAt(),
                null,
                ttl,
                requestFingerprint
        );
    }

    IdempotencyEntry failed(Duration failedEntryTtl) {
        return new IdempotencyEntry(
                IdempotencyEntryStatus.FAILED,
                statusCode,
                headers,
                body,
                completedAt,
                null,
                failedEntryTtl,
                requestFingerprint
        );
    }

    IdempotencyEntry extendLock(Instant now, Duration lockTtl) {
        return new IdempotencyEntry(status, statusCode, headers, body, completedAt, now.plus(lockTtl), ttl, requestFingerprint);
    }

    boolean canBeAcquired(Instant now) {
        return status == IdempotencyEntryStatus.FAILED || (lockExpiresAt != null && lockExpiresAt.isBefore(now));
    }

    boolean hasSameFingerprint(IdempotencyContext context) {
        return requestFingerprint.equals(context.requestFingerprint());
    }

    boolean isInProgress() {
        return status == IdempotencyEntryStatus.IN_PROGRESS;
    }

    boolean isComplete() {
        return status == IdempotencyEntryStatus.COMPLETE;
    }

    StoredResponse toStoredResponse() {
        return new StoredResponse(statusCode, headers, body, completedAt);
    }

    @Override
    public byte[] body() {
        return body.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof IdempotencyEntry other)) {
            return false;
        }
        return statusCode == other.statusCode
                && status == other.status
                && Objects.equals(headers, other.headers)
                && Arrays.equals(body, other.body)
                && Objects.equals(completedAt, other.completedAt)
                && Objects.equals(lockExpiresAt, other.lockExpiresAt)
                && Objects.equals(ttl, other.ttl)
                && Objects.equals(requestFingerprint, other.requestFingerprint);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(status, statusCode, headers, completedAt, lockExpiresAt, ttl, requestFingerprint);
        result = 31 * result + Arrays.hashCode(body);
        return result;
    }

    private static Map<String, List<String>> copyHeaders(Map<String, List<String>> headers) {
        Objects.requireNonNull(headers, "headers must not be null");
        Map<String, List<String>> copy = new LinkedHashMap<>();
        headers.forEach((name, values) -> copy.put(name, List.copyOf(values)));
        return Map.copyOf(copy);
    }
}
