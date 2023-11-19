package com.real.time.chatapp.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.real.time.chatapp.Auth.AuthenticationRequest;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.RegisterRequest;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.ControllerServices.ConversationService;
import com.real.time.chatapp.Exception.BadRegisterRequestException;
import com.real.time.chatapp.Exception.UsernameTakenException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
	private static Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);
	
	private final AuthenticationService service;
	
	/**
	 * Register a User 
	 * 
	 * @param user
	 * @return
	 */
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		AuthenticationResponse response = null;
		try {
			response = service.register(request);
		} catch(UsernameTakenException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
		} catch(BadRegisterRequestException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
		}
		return ResponseEntity.ok(response); 
	}
	

	
	/**
	 * Authenticate a User 
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/authenticate")
	public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request){
		AuthenticationResponse response;
		try {
			response = service.authenticate(request);
		} catch(BadCredentialsException ex) {
			String errorMessage = "The provided credentials are invalid.";
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage);
		}
		return ResponseEntity.ok(response);
	}

}
