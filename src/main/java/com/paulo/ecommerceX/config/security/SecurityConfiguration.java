package com.paulo.ecommerceX.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/reset-password-request").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/auth").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/auth/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/auth/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sales").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/sales/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sales/by-month").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sales/this-week").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sales/by-day").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
