package com.ravcube.lib.stream.api;

public interface ClientStreamPublisher {

    void publish(String resourceName, String resourceId, Object payload);
}
