package com.real.time.chatapp.Controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.real.time.chatapp.Assemblers.ConversationModelAssembler;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.UserRepository;

@RestController
public class ConversationController {
	private ConversationRepository conversationRepository;
	private ConversationModelAssembler conversationAssembler;
	private UserRepository userRepository;

	public ConversationController(ConversationRepository conversationRepository,
			ConversationModelAssembler conversationAssembler, UserRepository userRepository) {
		this.conversationAssembler = conversationAssembler;
		this.conversationRepository = conversationRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Getting all of the apps conversations
	 * 
	 * @return
	 */
	@GetMapping("/conversations")
	public CollectionModel<EntityModel<Conversation>> all() {
		List<EntityModel<Conversation>> entityModels = conversationRepository.findAll().stream()
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
		Conversation convo = conversationRepository.findById(id)
				.orElseThrow(() -> new ConversationNotFoundException(id));
		return conversationAssembler.toModel(convo);
	}

	/**
	 * Searching for conversations that have occured after a specific date
	 * 
	 * @param date
	 * @return
	 */
	@GetMapping("/search/conversations")
	CollectionModel<EntityModel<Conversation>> findConversationsByDate(
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
		List<EntityModel<Conversation>> entityModels = conversationRepository.findConversationsByDate(date).stream()
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
	CollectionModel<EntityModel<Conversation>> findConversationsWithUser(@PathVariable Long id) {
		User user = userRepository.findById(id).orElse(null);
		
		List<EntityModel<Conversation>> entityModels = conversationRepository.findConversationsByUser(user).stream()
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
	@PostMapping("/conversations/{userOneId}/{userTwoId}")
	ResponseEntity<?> createConversationBetweenUsers(@PathVariable Long userOneId, @PathVariable Long userTwoId) {
		User userOne = userRepository.findById(userOneId).orElseThrow(() -> new UserNotFoundException(userOneId));
		User userTwo = userRepository.findById(userTwoId).orElseThrow(() -> new UserNotFoundException(userTwoId));

		Conversation conversation = new Conversation();
		conversation.getConversation_users().add(userOne);
		conversation.getConversation_users().add(userTwo);
		conversation.setNumUsers(2);

		userOne.getList_conversations().add(conversation);
		userTwo.getList_conversations().add(conversation);
		EntityModel<Conversation> entityModel = conversationAssembler
				.toModel(conversationRepository.save(conversation));
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
	ResponseEntity<?> updateConversation(@PathVariable Long conversationId, @RequestBody Conversation newConversation) {
		Conversation updatedConversation = conversationRepository.findById(conversationId).map(conversation -> {
			conversation.setConversation_id(newConversation.getConversation_id());
			conversation.setConversation_users(newConversation.getConversation_users());
			conversation.setConversationStart(newConversation.getConversationStart());
			conversation.setMessages(newConversation.getMessages());
			conversation.setNumUsers(newConversation.getNumUsers());
			return conversationRepository.save(conversation);
		}).orElseGet(() -> {
			newConversation.setConversation_id(conversationId);
			return conversationRepository.save(newConversation);
		});

		EntityModel<Conversation> entityModel = conversationAssembler.toModel(updatedConversation);
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
	ResponseEntity<?> addUserToConversation(@PathVariable Long conversationId, @PathVariable Long userId) {

		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		conversation.getConversation_users().add(user);
		user.getList_conversations().add(conversation);
		EntityModel<Conversation> entityModel = conversationAssembler
				.toModel(conversationRepository.save(conversation));
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);

	}

	/**
	 * Delete a conversation
	 * 
	 * @param conversationId
	 * @return
	 */
	@DeleteMapping("/conversations/{conversationId}")
	ResponseEntity<?> deleteConversation(@PathVariable Long conversationId) {
		conversationRepository.deleteById(conversationId);
		return ResponseEntity.noContent().build();
	}

}
