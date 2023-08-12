package com.real.time.chatapp.Controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.real.time.chatapp.Assemblers.ConversationModelAssembler;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.ControllerServices.ConversationService;
import com.real.time.chatapp.DTO.ConversationDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ConversationController {
	private final ConversationRepository conversationRepository;
	private final ConversationModelAssembler conversationAssembler;
	private final UserRepository userRepository;
	private final AuthenticationService service;
	private final ConversationService conversationService;

	/**
	 * Getting all of the apps conversations
	 * 
	 * @return
	 */
	@GetMapping("/conversations")
	public CollectionModel<EntityModel<Conversation>> all() {
		List<EntityModel<Conversation>> entityModels;
		try {
			entityModels = conversationService.getAllConversations().stream().map(conversationAssembler::toModel)
					.collect(Collectors.toList());
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}

		return CollectionModel.of(entityModels, linkTo(methodOn(ConversationController.class).all()).withSelfRel());

	}

	@GetMapping("/userConversations")
	public CollectionModel<EntityModel<Conversation>> getConversationByUser() {
		List<EntityModel<Conversation>> entityModels = conversationService.getAllUserConversations().stream()
				.map(conversationAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels, linkTo(methodOn(ConversationController.class).all()).withSelfRel());

	}

	/**
	 * Fetching a conversation by ID
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/conversations/{id}")
	public EntityModel<Conversation> one(@PathVariable Long id) {
		try {
			return conversationAssembler.toModel(conversationService.getConversationById(id));
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
	}

	/**
	 * Searching for conversations that have occured after a specific date
	 * 
	 * @param date
	 * @return
	 */
	@GetMapping("/search/conversations")
	public CollectionModel<EntityModel<Conversation>> findConversationsByDate(
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {

		List<EntityModel<Conversation>> entityModels = conversationService.searchConversationsByDate(date).stream()
				.map(conversationAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(ConversationController.class).findConversationsByDate(date)).withSelfRel());
	}

	/**
	 * Searching conversation that have a specific User within them
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/search/conversations/{id}")
	public CollectionModel<EntityModel<Conversation>> findConversationsWithUser(@PathVariable Long id) {
		List<EntityModel<Conversation>> entityModels = conversationService.searchConversationsWithUser(id).stream()
				.map(conversationAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(ConversationController.class).findConversationsWithUser(id)).withSelfRel());
	}

	/**
	 * Creating a conversation between two users
	 * 
	 * @param userOneId
	 * @param userTwoId
	 * @return
	 */
	@PostMapping("/conversations/{userId}")
	public ResponseEntity<?> createConversationBetweenUsers(@PathVariable Long userId) {

		EntityModel<Conversation> entityModel = conversationAssembler
				.toModel(conversationService.createConversation(userId));
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	/**
	 * Update a conversation
	 * 
	 * @param conversationId
	 * @param newConversation
	 * @return
	 */
	@PutMapping("/conversation/{conversationId}")
	public ResponseEntity<?> updateConversation(@PathVariable Long conversationId,
			@RequestBody ConversationDTO newConversationDTO) {
		EntityModel<Conversation> entityModel;
		try {
			entityModel = conversationAssembler
					.toModel(conversationService.updateConversation(conversationId, newConversationDTO));
		} catch(UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	/**
	 * Add User to Conversation
	 * 
	 * @param conversationId
	 * @param userId
	 * @return
	 */
	@PutMapping("/conversations/{conversationId}/{userId}")
	public ResponseEntity<?> addUserToConversation(@PathVariable Long conversationId, @PathVariable Long userId) {
		EntityModel<Conversation> entityModel;
		try {
			entityModel = conversationAssembler
					.toModel(conversationService.addUserToConversation(conversationId, userId));
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	/**
	 * Leave a Conversation - Delete conversation if no users remain
	 * 
	 * @param conversationId
	 * @return
	 */
	@DeleteMapping("/conversation/leave/{conversationId}")
	public ResponseEntity<?> leaveConversation(@PathVariable Long conversationId) {
		try {
			conversationService.leaveConversation(conversationId);
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		return ResponseEntity.noContent().build();
	}

	/**
	 * Delete a conversation
	 * 
	 * @param conversationId
	 * @return
	 */
	@DeleteMapping("/conversations/{conversationId}")
	public ResponseEntity<?> deleteConversation(@PathVariable Long conversationId) {
		try {
			conversationService.deleteConversation(conversationId);
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		return ResponseEntity.noContent().build();
	}

}
