package com.real.time.chatapp.ControllerServices;

import java.util.List;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.real.time.chatapp.Assemblers.UserModelAssembler;
import com.real.time.chatapp.Auth.AuthenticationRequest;
import com.real.time.chatapp.Config.JwtService;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UserNotAuthenticatedException;
import com.real.time.chatapp.Exception.UsernameTakenException;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	
	
	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final UserModelAssembler userAssembler;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	//Register A User
	public ResponseEntity<?> register(User user) {
		// Build user Based On Request
		List<User> users = repository.searchUsersByUserName(user.getUsername());
		if(users != null) {
			for(User u: users) {
				if(u.getUsername().trim().equals(user.getUsername())) {
					throw new UsernameTakenException(user.getUsername());
				}
			}	
		}
		var encodedUser = User.builder().firstName(user.getFirstName()).lastName(user.getLastName())
				.userName(user.getUsername()).password(passwordEncoder.encode(user.getPassword())).role(Role.USER)
				.build();
		repository.save(encodedUser);
		EntityModel<User> entityModel = userAssembler.toModel(encodedUser);
		var jwtToken = jwtService.generateToken(user);
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken).body(entityModel);

	}
	
	//TODO Registering/Authenticating an Admin User
	//Authenticate a User
	public ResponseEntity<?> authenticate(AuthenticationRequest request) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		var user = repository.findByUserName(request.getUsername()).orElseThrow(() -> new UserNotAuthenticatedException(request.getUsername()));
		var jwtToken = jwtService.generateToken(user);
		EntityModel<User> entityModel = userAssembler.toModel(user);
		return ResponseEntity.ok()
		        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
		        .body(entityModel);
	}
	
	//Validate A User
	public boolean validateUser(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedUsername = authentication.getName().trim();
		if(user.getRole() != Role.ADMIN && !user.getUsername().trim().equals(authenticatedUsername)) {
			return false;
		}
		return true;
	}

}
