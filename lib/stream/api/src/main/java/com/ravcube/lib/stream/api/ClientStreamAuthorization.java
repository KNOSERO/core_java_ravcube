package com.ravcube.lib.stream.api;

@FunctionalInterface
public interface ClientStreamAuthorization {

    boolean canRead(String resourceName, String resourceId);
}
