package com.venkatesh.irctc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.venkatesh.irctc.repository.UserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            .securityMatcher("/auth/**", "/trains/**", "/booking/**", "/admin/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/register", "/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/trains", "/trains/search", "/trains/availability").permitAll()
                .requestMatchers(HttpMethod.POST, "/trains").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/booking/**").hasRole("USER")
                .anyRequest().denyAll())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain pageSecurityFilterChain(HttpSecurity http,
            UserDetailsService userDetailsService) throws Exception {
        http
            .securityMatcher("/", "/login", "/register", "/logout", "/app/**", "/webjars/**", "/css/**", "/js/**", "/error")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/webjars/**", "/css/**", "/js/**", "/error").permitAll()
                .requestMatchers("/app/trains/search", "/app/trains/*/availability").permitAll()
                .requestMatchers("/app/admin/**").hasRole("ADMIN")
                .requestMatchers("/app/bookings/**").hasRole("USER")
                .requestMatchers("/app/**").authenticated()
                .anyRequest().denyAll())
            .userDetailsService(userDetailsService)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/app/dashboard", true)
                .failureUrl("/login?error")
                .permitAll())
            .exceptionHandling(exception -> exception.accessDeniedPage("/app/forbidden"))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getRole())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
