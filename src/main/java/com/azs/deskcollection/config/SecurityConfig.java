package com.azs.deskcollection.config;

import com.azs.deskcollection.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in dev
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/**").hasAnyRole("USER", "SUPERADMIN")
                        .requestMatchers("/debtors/**").hasAnyRole("USER", "SUPERADMIN") // Allow Page Access
                        .requestMatchers("/dashboard/**").hasAnyRole("USER", "SUPERADMIN")
                        .requestMatchers("/devices/**").hasRole("SUPERADMIN")
                        .requestMatchers("/admin/**").hasRole("SUPERADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll())
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
