package com.real.time.chatapp.Controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.time.chatapp.Assemblers.ConversationModelAssembler;
import com.real.time.chatapp.Assemblers.UserModelAssembler;
import com.real.time.chatapp.Auth.AuthenticationRequest;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserRepository user_repository;
	private final UserModelAssembler user_assembler;
	private final ConversationRepository conversation_repository;
	private final ConversationModelAssembler conversation_assembler;
	private final MessageRepository message_repository;
	private final AuthenticationService service;
	private final AuthenticationManager authenticationManager;

	/**
	 * Fetching all users
	 * 
	 * @return
	 */
	@GetMapping("/users")
	public CollectionModel<EntityModel<User>> all() {
		List<EntityModel<User>> users = user_repository.findAll().stream().map(user_assembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
	}

	/**
	 * Fetching a specific user
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/users/{id}")
	public EntityModel<User> one(@PathVariable Long id) {

		User user = user_repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		return user_assembler.toModel(user);
	}

	/**
	 * Search for users based on name
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping("/search/users/name")
	CollectionModel<EntityModel<User>> searchUsersByName(@RequestParam String name) {
		String[] firstAndLast = name.split(" ");
		List<EntityModel<User>> entityModels = user_repository.searchUsersByName(firstAndLast[0], firstAndLast[1])
				.stream().map(user_assembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(UserController.class).searchUsersByName(name)).withSelfRel());
	}

	/**
	 * Search for users by username
	 * 
	 * @param userName
	 * @return
	 */
	@GetMapping("/search/users/userName")
	CollectionModel<EntityModel<User>> searchUsersByUserName(@RequestParam String userName) {
		List<EntityModel<User>> entityModels = user_repository.searchUsersByUserName(userName).stream()
				.map(user_assembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(UserController.class).searchUsersByUserName(userName)).withSelfRel());
	}
	
	/**
	 * Register a User 
	 * 
	 * @param user
	 * @return
	 */
	@PostMapping("/register")
	ResponseEntity<?> register(@RequestBody User user) {
		return service.register(user);
	}

	
	/**
	 * Authenticate a User 
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/authenticate")
	ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request){
		return service.authenticate(request);
	}

	/**
	 * Updating a user
	 * 
	 * @param newUser
	 * @param id
	 * @return
	 */
	@PutMapping("users/{id}")
	ResponseEntity<?> updateUser(@RequestBody User newUser, @PathVariable Long id) {
		User user = user_repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		if(!service.validateUser(user)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to perform this action.");
		}
		user.setFirstName(newUser.getFirstName());
		user.setLastName(newUser.getLastName());
		user.setPassword(newUser.getPassword());
		user.setRecievedMessages(newUser.getRecievedMessages());
		user.setRole(Role.USER);
		user.setSentMessages(newUser.getSentMessages());
		user.setUserName(newUser.getUsername());
		User updatedUser = user_repository.save(user);
		

		EntityModel<User> entityModel = user_assembler.toModel(updatedUser);

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	/**
	 * Deleting a User
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("/users/{id}")
	ResponseEntity<?> deleteUser(@PathVariable Long id) {
		// TODO: Consider another way to do this logic ??
		/**
		 * Delete messages sent by User and remove all recipients of each message sent
		 * by User
		 */

		User user = user_repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        //Ensure this user is either an admin or a user deleting their account
		if(!service.validateUser(user)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to perform this action.");
		}
		for (Message msg : user.getSentMessages()) {
			Set<User> recipients = msg.getRecipients();
			for (User current : recipients) {
				current.getRecievedMessages().remove(msg);
			}
			message_repository.deleteById(msg.getMessage_id());
		}
		user_repository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}