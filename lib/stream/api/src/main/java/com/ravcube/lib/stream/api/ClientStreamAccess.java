package com.ravcube.lib.stream.api;

@FunctionalInterface
public interface ClientStreamAccess {

    boolean allows(String resourceId);
}
