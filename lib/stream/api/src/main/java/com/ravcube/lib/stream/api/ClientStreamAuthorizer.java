package com.ravcube.lib.stream.api;

import java.util.Set;

@FunctionalInterface
public interface ClientStreamAuthorizer {

    ClientStreamAccess authorize(String resourceName, Set<String> resourceIds);
}
