package com.ravcube.lib.security;

import java.util.List;
import java.util.Map;

public class SecurityContext {

    private static final ThreadLocal<List<String>> ROLES = ThreadLocal.withInitial(List::of);
    private static final ThreadLocal<Map<String, Object>> CLAIMS = ThreadLocal.withInitial(Map::of);

    private SecurityContext() {
    }

    public static List<String> getRoles() {
        return ROLES.get();
    }

    public static void setRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            ROLES.set(List.of());
            return;
        }
        ROLES.set(List.copyOf(roles));
    }

    public static Map<String, Object> getClaims() {
        return CLAIMS.get();
    }

    public static void setClaims(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            CLAIMS.set(Map.of());
            return;
        }
        CLAIMS.set(Map.copyOf(claims));
    }

    public static void clear() {
        ROLES.remove();
        CLAIMS.remove();
    }
}
