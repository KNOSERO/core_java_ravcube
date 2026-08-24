package com.ravcube.lib.stream;

import java.util.Collection;

public interface ClientStreamPublisher {

    <T> void refresh(String resourceName, String resourceId, T payload);

    <T> void refresh(String resourceName, T payload);

    <T> void refresh(String resourceName, Collection<String> resourceIds, T payload);
}
