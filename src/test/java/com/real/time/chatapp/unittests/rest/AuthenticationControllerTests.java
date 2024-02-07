package com.real.time.chatapp.unittests.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.web.server.ResponseStatusException;

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
 * Unit Tests for Authentication Controller 
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
        ResponseEntity<?> responseEntity = authController.register(registerRequest);

        // Assert
        
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody() instanceof AuthenticationResponse);
        @SuppressWarnings("unchecked")
		ResponseEntity<AuthenticationResponse> typeResponse = (ResponseEntity<AuthenticationResponse>) responseEntity;
        assertEquals("dummyToken", typeResponse.getBody().getToken());
        
        //Ensure Stubbed Methods Are Called
        verify(authService, times(1)).register(registerRequest);
	}
	
    @Test
    @Transactional
    void test_registerUser_returnsConflict() {
        // Mocking
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existingUsername");
        when(authService.register(registerRequest)).thenThrow(new UsernameTakenException("existingUsername"));
        
        //Act and Assert
        ResponseEntity<?> responseEntity = authController.register(registerRequest);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals("Username existingUsername already exists.", responseEntity.getBody());
        
        //Ensure Stubbed Methods Are Called
        verify(authService, times(1)).register(registerRequest);
    }
    
    @Test
    @Transactional
    void test_registerUser_returnsBadRequest() {
        // Mocking
        RegisterRequest registerRequest = new RegisterRequest();
        when(authService.register(registerRequest)).thenThrow(new BadRegisterRequestException(registerRequest));
        
        
        //Act and Assert
        ResponseEntity<?> responseEntity = authController.register(registerRequest);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Bad Register Request: Please Enter A Valid Username And Password", responseEntity.getBody());
        
        //Ensure Stubbed Methods Are Called
        verify(authService, times(1)).register(registerRequest);
    }

    //TODO: Fix me
//    @Test
//    @Transactional
//    void test_authenticateUser_isSuccesful() {
//        // Mocking
//        AuthenticationRequest authRequest = new AuthenticationRequest();
//        authRequest.setUsername("testUser");
//        authRequest.setPassword("test");
//        when(authService.authenticate(authRequest)).thenReturn(
//            AuthenticationResponse.builder().token("dummyToken").build());
//
//        // Act
//        ResponseEntity<AuthenticationResponse> responseEntity = authController.authenticate(authRequest);
//
//        // Assert
//        assertNotNull(responseEntity);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertNotNull(responseEntity.getBody());
//        assertEquals("dummyToken", responseEntity.getBody().getToken());
//        
//        //Ensure Stubbed Methods Are Called
//        verify(authService, times(1)).authenticate(authRequest);
//    }
    
//    @Test
//    @Transactional
//    void test_authenticateuser_wrongPassword_returnsUnauthorized() {
//        // Mocking
//        AuthenticationRequest authRequest = new AuthenticationRequest();
//        authRequest.setUsername("testUser");
//        authRequest.setPassword("wrongPassword");
//        when(authService.authenticate(authRequest)).thenThrow(new BadCredentialsException("Authentication failed"));
//        
//        //Act and Assert
//        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
//            authController.authenticate(authRequest);
//        });
//        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
//        assertEquals("The provided credentials are invalid.", exception.getReason());
//        
//        //Ensure Stubbed Methods Are Called
//        verify(authService, times(1)).authenticate(authRequest);
//    }
//    
//    @Test
//    @Transactional
//    void test_authenticateuser_wrongUsername_returnsUnauthorized() {
//        // Mocking
//        AuthenticationRequest authRequest = new AuthenticationRequest();
//        authRequest.setUsername("wrongUser");
//        authRequest.setPassword("password");
//        when(authService.authenticate(authRequest)).thenThrow(new BadCredentialsException("Authentication failed"));
//        
//        //Act and Assert
//        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
//            authController.authenticate(authRequest);
//        });
//        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
//        assertEquals("The provided credentials are invalid.", exception.getReason());
//        
//        //Ensure Stubbed Methods Are Called
//        verify(authService, times(1)).authenticate(authRequest);
//    }
//}
}
