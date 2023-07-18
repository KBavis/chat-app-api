package com.real.time.chatapp.ControllerServices;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.MessageNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Exception.UsernameTakenException;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserModelAssembler userAssembler;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final MessageRepository messageRepository;
	
	//Register A User
	public ResponseEntity<?> register(User user) {
		// Build user Based On Request
		List<User> users = userRepository.searchUsersByUserName(user.getUsername());
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
		userRepository.save(encodedUser);
		EntityModel<User> entityModel = userAssembler.toModel(encodedUser);
		var jwtToken = jwtService.generateToken(user);
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken).body(entityModel);

	}
	
	//TODO Registering/Authenticating an Admin User
	//Authenticate a User
	public ResponseEntity<?> authenticate(AuthenticationRequest request) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		var user = userRepository.findByUserName(request.getUsername()).orElseThrow(() -> new UserNotFoundException(request.getUsername()));
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
	
	//Validating A User Is An Admin
	public boolean validateAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName().trim();
        var user = userRepository.findByUserName(userName).orElseThrow(() -> new UserNotFoundException(userName));
        if(user.getRole() == Role.ADMIN) {
        	return true;
        }
        return false;
	}

	public boolean validateMessage(Long id) {
		Message message = messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
		User sender = message.getSender();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName().trim();
        if(!userName.equals(sender.getUsername())) {
        	return false;
        }
        return true;
	}

	public boolean validateUserConversation(Conversation conversation) {
		Set<User> users = conversation.getConversation_users();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName().trim();
        for(User user: users) {
        	if(user.getUsername().equals(userName)) {
        		return true;
        	}
        }
        return false;
	}

	public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName().trim();
        Optional<User> user = userRepository.findByUserName(userName);
        User foundUser = user.orElseThrow(() -> new UserNotFoundException(userName));
        return foundUser;
	}

	

}
