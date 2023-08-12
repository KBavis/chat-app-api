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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.time.chatapp.Assemblers.UserModelAssembler;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.ControllerServices.UserService;
import com.real.time.chatapp.DTO.UserDTO;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserModelAssembler userAssembler;
	private final UserService userService;

	/**
	 * Fetching all users
	 * 
	 * @return
	 */
	@GetMapping("/users")
	public CollectionModel<EntityModel<User>> all() {
		List<EntityModel<User>> users = userService.getAllUsers().stream().map(userAssembler::toModel)
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

		User user = userService.getUserById(id);
		return userAssembler.toModel(user);
	}

	/**
	 * Search for users based on name
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping("/search/users/name")
	public CollectionModel<EntityModel<User>> searchUsersByName(@RequestParam String name) {
		List<EntityModel<User>> entityModels = userService.searchUserByName(name)
				.stream().map(userAssembler::toModel).collect(Collectors.toList());

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
	public CollectionModel<EntityModel<User>> searchUsersByUserName(@RequestParam String userName) {
		List<EntityModel<User>> entityModels = userService.searchUserByUsername(userName).stream()
				.map(userAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(UserController.class).searchUsersByUserName(userName)).withSelfRel());
	}
	
	/**
	 * Updating a user
	 * 
	 * @param newUser
	 * @param id
	 * @return
	 */
	@PutMapping("users/{id}")
	public ResponseEntity<?> updateUser(@RequestBody UserDTO newUser, @PathVariable Long id) {
		try {
			User updatedUser = userService.updateUser(id, newUser);
			EntityModel<User> entityModel = userAssembler.toModel(updatedUser);
			return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
		} catch(UnauthorizedException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to perform this action.");
		}

	}

	/**
	 * Deleting a User
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("/users/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		try {
			userService.deleteUser(id);
			return ResponseEntity.noContent().build();
		}catch(UnauthorizedException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to perform this action.");
		}

	}
}