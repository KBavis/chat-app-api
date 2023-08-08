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
import org.springframework.test.annotation.DirtiesContext;

import com.real.time.chatapp.Auth.AuthenticationRequest;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.RegisterRequest;
import com.real.time.chatapp.Config.JwtService;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.Controllers.AuthenticationController;
import com.real.time.chatapp.Entities.User;
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
	private AuthenticationManager authManager;
	
	@Mock
	private JwtService jwtService;
	
	@Mock
	private UserRepository userRepository;
	
	@InjectMocks 
	private AuthenticationService authService;
	
	@InjectMocks
	private AuthenticationController authController;
	
//	@Test
//	@Transactional
//	void testRegisterUser_returnsOK()	throws Exception {
//		RegisterRequest registerRequest = new RegisterRequest();
//		
//		AuthenticationResponse authResponse = new AuthenticationResponse();
//		
//		//Mock Authentication Service 
//		when(authService.register(registerRequest)).thenReturn(authResponse);
//
//		ResponseEntity<AuthenticationResponse> responseEntity = authController.register(registerRequest);
//		assertNotNull(responseEntity);
//		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//		assertEquals(authResponse,responseEntity.getBody());
//	}
//	
//	@Test
//	@Transactional
//	void testAuthenticate_returnsOK() throws Exception {
//		AuthenticationRequest authRequest = new AuthenticationRequest();
//		AuthenticationResponse authResponse = new AuthenticationResponse();
//		//Mock Authentication Service 
//		when(authService.authenticate(any())).thenReturn(authResponse);
//		
//		ResponseEntity<AuthenticationResponse> responseEntity = authController.authenticate(authRequest);
//		assertNotNull(responseEntity);
//		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//		assertEquals(authResponse, responseEntity.getBody());
//	}
	
	@Test
	@Transactional
	void testRegister_returnsUsernameTaken() {
		RegisterRequest registerRequest = new RegisterRequest();
		registerRequest.setUsername("existingUsername");
		
		//mock the services findByUserName to return non-null user
		User existingUser = User.builder().firstName("test").lastName("test").userName("existingUsername").password("test").build();
		when(userRepository.findByUserName("existingUsername")).thenReturn(Optional.of(existingUser));

		assertThrows(UsernameTakenException.class, () -> authController.register(registerRequest));
//		
//		//verifying the find by username method has been called exactly 1 time with argument "exisitngusername"
//		verify(userRepository, times(1)).findByUserName("existingUsername");
	}
	
	
//	@Test
//	@Transactional
//	void testRegister_invalidRequest(){
//		RegisterRequest registerRequest = new RegisterRequest();
//		
//		when(authService.register(registerRequest)).thenThrow(new BadRegisterRequestException(registerRequest));
//		
//		ResponseEntity<AuthenticationResponse> response = authController.register(registerRequest);
//		
//		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//	}
//	@Test
//	@Transactional 
//	void testAuthenticate_returnsUNAUTHORIZED() {
//		AuthenticationRequest authRequest = new AuthenticationRequest();
//		
//        // Mock the behavior of authService to throw BadCredentialsException
//        when(authService.authenticate(any(AuthenticationRequest.class)))
//            .thenThrow(BadCredentialsException.class);
//
//        // Act
//        ResponseEntity<AuthenticationResponse> responseEntity = null;
//        try {
//        	 responseEntity = authController.authenticate(authRequest);	
//        } catch(Exception ex) {
//            // Assert
//            assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
//            assertNull(responseEntity.getBody());
//        	assertTrue((ex.getCause()) instanceof BadCredentialsException);
//        	
//        }
//	}
}
