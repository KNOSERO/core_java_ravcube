package com.ravcube.lib.stream.infrastructure.sse;

record ClientStreamRefreshNotification(String resourceId, long version) {

    ClientStreamRefreshNotification {
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceId must not be blank");
        }
        if (version < 0) {
            throw new IllegalArgumentException("version must not be negative");
        }
    }
}
