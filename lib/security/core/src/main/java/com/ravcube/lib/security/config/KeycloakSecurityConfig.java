package com.ravcube.lib.security.config;

import com.ravcube.lib.security.auth.keycloak.KeycloakAuthClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("keycloak")
@EnableFeignClients(basePackageClasses = KeycloakAuthClient.class)
public class KeycloakSecurityConfig {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain keycloakSecurityFilterChain(
            HttpSecurity http,
            SecurityContextTokenFilter securityContextTokenFilter,
            @Value("${ravcube.security.auth.path:/auth}") String authPath
    )
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(authPath + "/login", authPath + "/refresh", authPath + "/logout").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(securityContextTokenFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    SecurityContextTokenFilter securityContextTokenFilter() {
        return new SecurityContextTokenFilter();
    }
}
