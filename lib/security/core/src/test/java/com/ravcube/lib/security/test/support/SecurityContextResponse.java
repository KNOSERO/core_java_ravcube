package com.ravcube.lib.security.test.support;

import java.util.List;
import java.util.Map;

public record SecurityContextResponse(List<String> roles, Map<String, Object> claims) {
}
