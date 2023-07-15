package com.real.time.chatapp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.real.time.chatapp.Rest.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
	private final UserRepository repository;

	/**
	 * Utilizes lambda to define the behavior of the UserDetailsService interface loadUserByUsername method 
	 * The lambda expression is returned as the bean instance 
	 * - Since the lambda expression implements the UserDetailsService, it can be used as UserDetailService bean in Spring Context
	 * - The lambda expression will return a UserDetails instance (as thats what loadUserByUsername return type is) 
	 * - When utilizing UserDetailsService, Spring Securtity will invoke the lambda expression below
	 * 
	 * @return
	 */
	@Bean
	public UserDetailsService userDetailsService() {
		return username -> repository.findByUserName(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
