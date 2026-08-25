package com.ravcube.lib.stream.api;

public interface ClientStreamResourceReader<T> {

    String resourceName();

    T resource(String resourceId);
}
