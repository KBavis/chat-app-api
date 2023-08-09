package com.real.time.chatapp.unittests.rest;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.annotation.DirtiesContext;

import com.real.time.chatapp.Auth.AuthenticationRequest;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.RegisterRequest;
import com.real.time.chatapp.Config.JwtService;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.Controllers.AuthenticationController;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.BadRegisterRequestException;
import com.real.time.chatapp.Exception.UsernameTakenException;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Unit Tests for Conversation Controlelr
 * 
 * @author bavis
 *
 */
@SpringBootTest
@DirtiesContext
public class AuthenticationControllerTests {
	private static final Logger log = LoggerFactory.getLogger(AuthenticationControllerTests.class);

	@Mock
	private AuthenticationService authService;

	@InjectMocks
	private AuthenticationController authController;
	
	@Test
	@Transactional
	void test_registerUser_isSuccesful() {
        // Mocking
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testUser");
        when(authService.register(registerRequest)).thenReturn(
            AuthenticationResponse.builder().token("dummyToken").build());

        // Act
        ResponseEntity<AuthenticationResponse> responseEntity = authController.register(registerRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("dummyToken", responseEntity.getBody().getToken());
	}
	
    @Test
    @Transactional
    void test_registerUser_returnsUsernameTakenException() {
        // Mocking
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existingUsername");
        when(authService.register(registerRequest)).thenThrow(new UsernameTakenException("existingUsername"));

        // Act and Assert
        assertThrows(UsernameTakenException.class, () -> authController.register(registerRequest));
    }
    
    @Test
    @Transactional
    void test_registerUser_returnsBadRequestException() {
        // Mocking
        RegisterRequest registerRequest = new RegisterRequest();
        when(authService.register(registerRequest)).thenThrow(new BadRegisterRequestException(registerRequest));
        
        // Act and Assert
        assertThrows(BadRegisterRequestException.class, () -> authController.register(registerRequest));
    }

    
    @Test
    @Transactional
    void test_authenticateUser_isSuccesful() {
        // Mocking
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setUsername("testUser");
        authRequest.setPassword("test");
        when(authService.authenticate(authRequest)).thenReturn(
            AuthenticationResponse.builder().token("dummyToken").build());

        // Act
        ResponseEntity<AuthenticationResponse> responseEntity = authController.authenticate(authRequest);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("dummyToken", responseEntity.getBody().getToken());
    }
    
    @Test
    @Transactional
    void test_authenticateuser_wrongPassword() {
        // Mocking
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setUsername("testUser");
        authRequest.setPassword("wrongPassword");
        when(authService.authenticate(authRequest)).thenThrow(new BadCredentialsException("Authentication failed"));
        
        // Act and Assert
        assertThrows(BadCredentialsException.class, () -> authController.authenticate(authRequest));
    }
    
    @Test
    @Transactional
    void test_authenticateuser_wrongUsername() {
        // Mocking
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setUsername("wrongUser");
        authRequest.setPassword("password");
        when(authService.authenticate(authRequest)).thenThrow(new BadCredentialsException("Authentication failed"));
        
        // Act and Assert
        assertThrows(BadCredentialsException.class, () -> authController.authenticate(authRequest));
    }
}
