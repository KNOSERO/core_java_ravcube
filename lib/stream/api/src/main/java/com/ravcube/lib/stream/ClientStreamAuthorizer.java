package com.ravcube.lib.stream;

import java.util.Set;

@FunctionalInterface
public interface ClientStreamAuthorizer {

    ClientStreamAccess authorize(String resourceName, Set<String> resourceIds);
}
