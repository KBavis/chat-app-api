package com.real.time.chatapp.Rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;

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
	CollectionModel<EntityModel<Conversation>> all() {
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
	EntityModel<Conversation> one(@PathVariable Long id) {
		Conversation convo = conversationRepository.findById(id)
				.orElseThrow(() -> new ConversationNotFoundException(id));
		return conversationAssembler.toModel(convo);
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

		userOne.getList_conversations().add(conversation);
		userTwo.getList_conversations().add(conversation);
		EntityModel<Conversation> entityModel = conversationAssembler
				.toModel(conversationRepository.save(conversation));
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
		EntityModel<Conversation> entityModel = conversationAssembler.toModel(conversationRepository.save(conversation));
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);

	}

}
