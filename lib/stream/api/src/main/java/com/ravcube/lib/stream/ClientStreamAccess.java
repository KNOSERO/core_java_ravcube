package com.ravcube.lib.stream;

@FunctionalInterface
public interface ClientStreamAccess {

    boolean allows(String resourceId);
}
