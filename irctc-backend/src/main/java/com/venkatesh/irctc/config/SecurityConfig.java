package com.venkatesh.irctc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;


@Configuration
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

	    http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	                .requestMatchers("/auth/register", "/auth/login").permitAll()
	                .requestMatchers(HttpMethod.GET,
			                "/trains", "/trains/search", "/trains/availability").permitAll()
	                .requestMatchers(HttpMethod.POST, "/trains").hasRole("ADMIN")
	                .requestMatchers("/admin/**").hasRole("ADMIN")
	                .requestMatchers("/booking/**").authenticated()
	                .anyRequest().authenticated()
	        )
	        .sessionManagement(session ->
	                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        );

	    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}
	
	
	@Bean
	public PasswordEncoder passEncoder()
	{
		return new BCryptPasswordEncoder();
	}

}
