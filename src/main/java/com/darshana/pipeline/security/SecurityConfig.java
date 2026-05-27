package com.darshana.pipeline.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security — HTTP Basic Auth with Role-Based Access Control.
 *
 * Roles:
 *   ADMIN → full access (GET + POST + PUT + PATCH + DELETE)
 *   USER  → read-only (GET only)
 *
 * Credentials:
 *   admin / admin123
 *   user  / user123
 *
 * IMPORTANT Spring Security rule:
 *   Matchers are evaluated top-down, first match wins.
 *   .anyRequest() MUST always be the very last rule — no matchers after it.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — REST APIs are stateless, no session cookie to exploit
            .csrf(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(auth -> auth

                // 1. PUBLIC — no authentication required
                .requestMatchers("/", "/health", "/h2-console/**", "/actuator/health")
                    .permitAll()

                // 2. READ-ONLY — GET requests accessible by both ADMIN and USER
                .requestMatchers(HttpMethod.GET, "/api/**")
                    .hasAnyRole("ADMIN", "USER")

                // 3. WRITE — POST, PUT, PATCH, DELETE only for ADMIN
                .requestMatchers("/api/**")
                    .hasRole("ADMIN")

                // 4. CATCH-ALL — anything else requires authentication
                //    MUST be last — no rules allowed after anyRequest()
                .anyRequest()
                    .authenticated()
            )
            // Allow H2 console to render in iframe
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
