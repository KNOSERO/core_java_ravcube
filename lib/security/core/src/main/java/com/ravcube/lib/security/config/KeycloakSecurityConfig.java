package com.ravcube.lib.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("keycloak")
public class KeycloakSecurityConfig {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain keycloakSecurityFilterChain(HttpSecurity http, SecurityContextTokenFilter securityContextTokenFilter)
            throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(securityContextTokenFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    SecurityContextTokenFilter securityContextTokenFilter() {
        return new SecurityContextTokenFilter();
    }
}
