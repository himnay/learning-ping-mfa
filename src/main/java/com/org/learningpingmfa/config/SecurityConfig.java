package com.org.learningpingmfa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Having spring-boot-starter-oauth2-client on the classpath auto-configures a default
 * OAuth2 *login* filter chain for the whole app (Boot assumes you want browser-facing SSO).
 * This demo only uses OAuth2 client_credentials outbound, to call PingOne as a worker — not
 * inbound login — so we replace that default with a plain permit-all chain. A real service
 * would put its own auth here (JWT resource server, session, mTLS, whatever fronts this API).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
