package com.ravcube.lib.security.config;

import com.ravcube.lib.security.SecurityContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

class SecurityContextTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            mapFromAuthentication();
            filterChain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
        }
    }

    private void mapFromAuthentication() {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken authentication)
                || !authentication.isAuthenticated()) {
            SecurityContext.clear();
            return;
        }

        SecurityContext.setRoles(extractRoles(authentication));
        SecurityContext.setClaims(authentication.getToken().getClaims());
    }

    private List<String> extractRoles(JwtAuthenticationToken authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .toList();
    }
}
