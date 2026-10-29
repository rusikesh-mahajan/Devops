package com.gatepass.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isGuard = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GUARD"));
            boolean isResident = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RESIDENT"));

            if (isAdmin) {
                response.sendRedirect("/admin/dashboard");
            } else if (isGuard) {
                response.sendRedirect("/guard/dashboard");
            } else if (isResident) {
                response.sendRedirect("/resident/dashboard");
            } else {
                response.sendRedirect("/dashboard");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabled for MVP simplicity
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/actuator/health", "/login", "/access-denied").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/guard/**").hasRole("GUARD")
                .requestMatchers("/resident/**").hasRole("RESIDENT")
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(roleBasedSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );
        return http.build();
    }
}
