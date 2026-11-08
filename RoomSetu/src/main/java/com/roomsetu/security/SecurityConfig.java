package com.roomsetu.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/listings/public/**").permitAll()
                        .requestMatchers("/api/support/ask").permitAll()
                        // Bootstrap must be reachable without a token — it's how the very
                        // first admin gets created. AdminService still enforces that it
                        // only ever works once (see the "already have an admin" guard).
                        .requestMatchers("/api/admin/bootstrap").permitAll()

                        // Every admin.* path across every phase, locked to ROLE_ADMIN.
                        // This single line replaces the "trust me, guard this later" notes
                        // left in Phases 1, 3, and 5.
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/listings/admin/**").hasRole("ADMIN")
// etc. for each module that has an admin sub-path
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}