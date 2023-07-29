package com.real.time.chatapp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.real.time.chatapp.Repositories.UserRepository;

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
	
	@Bean
	public AuthenticationManager authenticationManger(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	/**
	 * Data Access Object which is responsible to fetch UserDetails and also encode password 
	 * @return
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		//Which User Details Service TO Use To Fetch Info About Our User
		authProvider.setUserDetailsService(userDetailsService());
		//Provide password encoder
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	
	/**
	 * Password encoder bean 
	 * 
	 * @return
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
