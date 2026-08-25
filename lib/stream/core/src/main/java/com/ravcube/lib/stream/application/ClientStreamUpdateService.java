package com.ravcube.lib.stream.application;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public final class ClientStreamUpdateService {

    private final ClientStreamResourceCatalog resourceCatalog;

    public ClientStreamUpdateService(ClientStreamResourceCatalog resourceCatalog) {
        this.resourceCatalog = Objects.requireNonNull(resourceCatalog, "resourceCatalog must not be null");
    }

    public boolean update(String resourceName, String resourceId) {
        return resourceCatalog.find(resourceName)
                .map(resourceStream -> resourceStream.update(resourceId))
                .orElse(false);
    }
}
