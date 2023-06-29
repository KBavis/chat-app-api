package com.real.time.chatapp.Rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UserNotFoundException;

//TODO: Implement This As A REST Controller (i.e Use Spring HATEOAS and UserModelAssembler)
@RestController
class UserController {

	private final UserRepository user_repository;
	private final UserModelAssembler user_assembler;

	UserController(UserRepository user_repository, UserModelAssembler user_assembler) {
		this.user_repository = user_repository;
		this.user_assembler = user_assembler;
	}

	// Fetching All Users
	@GetMapping("/users")
	CollectionModel<EntityModel<User>> all() {
		List<EntityModel<User>> users = user_repository.findAll().stream().map(user_assembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
	}

	@GetMapping("/users/{id}")
	EntityModel<User> one(@PathVariable Long id) {

		User user = user_repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		return user_assembler.toModel(user);
	}

	@PostMapping("/users")
	ResponseEntity<?> newUser(@RequestBody User newUser) {
		EntityModel<User> entityModel = user_assembler.toModel(user_repository.save(newUser));
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@PutMapping("users/{id}")
	ResponseEntity<?> updateUser(@RequestBody User newUser, @PathVariable Long id) {
		User updatedUser = user_repository.findById(id).map(user -> {
			user.setFirstName(newUser.getFirstName());
			user.setLastName(newUser.getLastName());
			user.setPassword(newUser.getPassword());
			user.setRecievedMessages(newUser.getRecievedMessages());
			user.setSentMessages(newUser.getSentMessages());
			user.setUserName(newUser.getUserName());
			return user_repository.save(user);
		}).orElseGet(() -> {
			newUser.setId(id);
			return user_repository.save(newUser);
		});

		EntityModel<User> entityModel = user_assembler.toModel(updatedUser);

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@DeleteMapping("/users/{id}")
	ResponseEntity<?> deleteUser(@PathVariable Long id) {
		user_repository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}