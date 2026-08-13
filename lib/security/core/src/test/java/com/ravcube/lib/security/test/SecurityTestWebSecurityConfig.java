package com.ravcube.lib.security.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import static com.ravcube.test.keycloak.KeycloakTestProfiles.TEST_KEYCLOAK_PROFILE;

@Configuration
@Profile(TEST_KEYCLOAK_PROFILE)
class SecurityTestWebSecurityConfig {

    @Bean
    WebSecurityCustomizer testLoginEndpointCustomizer() {
        return web -> web.ignoring().requestMatchers("/test/security/login");
    }
}
