package com.ravcube.lib.security.test.support;

import com.ravcube.lib.security.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/security")
class SecurityContextTestController {

    @GetMapping("/context")
    SecurityContextResponse context() {
        return new SecurityContextResponse(SecurityContext.getRoles(), SecurityContext.getClaims());
    }
}
