package com.real.time.chatapp.Controllers;

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
import com.real.time.chatapp.Exception.BadRegisterRequestException;
import com.real.time.chatapp.Exception.UsernameTakenException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
	
	private final AuthenticationService service;
	
	/**
	 * Register a User 
	 * 
	 * @param user
	 * @return
	 */
	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		AuthenticationResponse response;
		try {
			response = service.register(request);
		} catch(UsernameTakenException ex) {
			String errorMessage = "The requested username is already taken.";
			throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage);
		} catch(BadRegisterRequestException ex) {
			String errorMessage = "The register request is invalid.";
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
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
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
		AuthenticationResponse response;
		try {
			response = service.authenticate(request);
		} catch(BadCredentialsException ex) {
			String errorMessage = "The provided credentials are invalid.";
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage);
		}
		return ResponseEntity.ok(response);
	}

}
