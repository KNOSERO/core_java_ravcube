package com.ravcube.lib.stream;

public interface ClientStreamPublisher {

    void publish(String resourceName, String resourceId, Object payload);
}
