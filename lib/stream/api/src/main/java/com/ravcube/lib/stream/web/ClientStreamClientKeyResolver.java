package com.ravcube.lib.stream.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
final class ClientStreamClientKeyResolver {

    String resolve(HttpServletRequest request) {
        final Principal principal = request.getUserPrincipal();
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return "principal:" + principal.getName();
        }

        final String remoteAddress = request.getRemoteAddr();
        if (remoteAddress == null || remoteAddress.isBlank()) {
            return "address:unknown";
        }
        return "address:" + remoteAddress;
    }
}
