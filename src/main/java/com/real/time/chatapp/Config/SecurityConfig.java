package com.real.time.chatapp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthFilter;
	private final AuthenticationProvider authenticationProvider;
	
	
	/**
	 *  App startup will cause Spring to search for bean of type SecurityFilterChain
	 *  	- Permit Whitelisted Requests (No Auth Needed)
	 *  	- Authenticate Other Requests
	 *  	- Stateless Session Management
	 *  	- Provide Authentication provided
	 *  	- Bind JwtAuthFilter, Ensuring It Occurs Before UsernamePasswordAuthenticationFilter
	 * @param http
	 * @return
	 * @throws Exception
	 */
	

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		.csrf()
		.disable()
		.authorizeHttpRequests()
		.requestMatchers("/register", "/authenticate")
		.permitAll()
		.anyRequest()
		.authenticated()
		.and()
		.sessionManagement()
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
		.authenticationProvider(authenticationProvider)
		.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
}
