package com.real.time.chatapp.unitteests.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.real.time.chatapp.Auth.AuthenticationRequest;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.RegisterRequest;
import com.real.time.chatapp.Config.JwtService;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.BadRegisterRequestException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Exception.UsernameTakenException;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Authentication Service Unit Tests
 * 
 * @author bavis
 *
 */
@SpringBootTest
public class AuthenticationServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private AuthenticationManager authManager;

	@InjectMocks
	private AuthenticationService authService;

	@Test
	@Transactional
	void test_register_isSuccesful() {
		RegisterRequest registerRequest = RegisterRequest.builder().username("test").password("test").build();

		// Mock
		when(userRepository.findByUserName("test")).thenReturn(Optional.empty());
		when(userRepository.save(any())).thenReturn(new User());
		when(passwordEncoder.encode(any())).thenReturn("test");
		when(jwtService.generateToken(any())).thenReturn("testToken");

		// Act
		AuthenticationResponse authResponse = authService.register(registerRequest);

		// Assert
		assertNotNull(authResponse);
		assertEquals(authResponse.getToken(), "testToken");

		// Ensure Stubbed Methods Are Called
		verify(userRepository, times(1)).findByUserName("test");
		verify(userRepository, times(1)).save(any(User.class));
		verify(passwordEncoder, times(1)).encode(any());
		verify(jwtService, times(1)).generateToken(any());
	}

	@Test
	@Transactional
	void test_register_usernameTaken() {
		RegisterRequest registerRequest = RegisterRequest.builder().username("test").password("test").build();
		// Mock
		when(userRepository.findByUserName("test")).thenReturn(Optional.of(new User()));

		// Act
		UsernameTakenException exception = assertThrows(UsernameTakenException.class, () -> {
			authService.register(registerRequest);
		});

		// Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "Username test already exists.");

		// Ensure Stubbed Methods Are Called
		verify(userRepository, times(1)).findByUserName("test");
	}

	@Test
	@Transactional
	void test_register_badRequest() {
		RegisterRequest registerRequest = new RegisterRequest();

		// Act
		BadRegisterRequestException exception = assertThrows(BadRegisterRequestException.class, () -> {
			authService.register(registerRequest);
		});

		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "Bad Register Request: Please Enter A Valid Username And Password");
	}

	@Test
	@Transactional
	void test_authenticate_isSuccesful() {
		AuthenticationRequest request = new AuthenticationRequest("username", "password");
		Authentication authentication = new UsernamePasswordAuthenticationToken(request.getUsername(),
				request.getPassword());

		// Mock
		User user = new User();
		when(userRepository.findByUserName("username")).thenReturn(Optional.of(user));
		when(authManager.authenticate(authentication)).thenReturn(authentication);
		when(jwtService.generateToken(user)).thenReturn("testToken");

		// Act
		AuthenticationResponse authResponse = authService.authenticate(request);

		// Assert
		assertNotNull(authResponse);
		assertEquals(authResponse.getToken(), "testToken");

		// Ensure Stubbed Methods Are Called
		verify(userRepository, times(1)).findByUserName("username");
		verify(authManager, times(1)).authenticate(authentication);
		verify(jwtService, times(1)).generateToken(user);
	}

	@Test
	@Transactional
	void test_authenticate_badCredentials() {
		AuthenticationRequest request = new AuthenticationRequest("username", "invalid_password");

		// Mock
		when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		// Act and Assert
		BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
			authService.authenticate(request);
		});
		
		// Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "Bad credentials");
		
		// Ensure Stubbed Methods Are Called
		verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
	}

	@Test
	@Transactional
	void test_authenticate_userNotFoundExcetpion() {
		AuthenticationRequest request = new AuthenticationRequest("username", "password");

		// Mock
		when(userRepository.findByUserName(request.getUsername())).thenReturn(Optional.empty());

		// Act
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
			authService.authenticate(request);
		});

		// Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "Could not find user " + request.getUsername());
		
		// Ensure Stubbed Methods Are Called
		verify(userRepository, times(1)).findByUserName("username");

	}
}
