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
import com.real.time.chatapp.DTO.ConversationDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
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
	/**
	 * Getting all of the apps conversations
	 * 
	 * @return
	 */
	@GetMapping("/conversations")
	public CollectionModel<EntityModel<Conversation>> all() {
		if(!service.validateAdmin()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		List<EntityModel<Conversation>> entityModels = conversationRepository.findAll().stream()
				.map(conversationAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels, linkTo(methodOn(ConversationController.class).all()).withSelfRel());

	}
	
	@GetMapping("/userConversations")
	public CollectionModel<EntityModel<Conversation>> getConversationByUser() {
		User user = service.getUser();
		List<EntityModel<Conversation>> entityModels = conversationRepository.findConversationsByUser(user).stream()
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
		User user = service.getUser();
		Conversation convo = conversationRepository.findById(id)
				.orElseThrow(() -> new ConversationNotFoundException(id));
		if(!convo.getConversation_users().contains(user)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
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
		User user = service.getUser();
		
		List<EntityModel<Conversation>> entityModels = conversationRepository.findConversationsByDate(date, user).stream()
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
		User authUser = service.getUser();
		List<EntityModel<Conversation>> entityModels = conversationRepository.findConversationsByUserAndAuthUser(user,authUser).stream()
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
	ResponseEntity<?> createConversationBetweenUsers(@PathVariable Long userId) {
		User userOne = service.getUser();
		User userTwo = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		
		Conversation conversation = new Conversation();
		if(conversation.getConversation_users() == null) conversation.setConversation_users(new HashSet<>());
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
	ResponseEntity<?> updateConversation(@PathVariable Long conversationId, @RequestBody ConversationDTO newConversationDTO) {
	    Conversation existingConversation = conversationRepository.findById(conversationId)
	            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

	    // Copy relevant fields from ConversationDTO to existingConversation
	    existingConversation.setNumUsers(newConversationDTO.getNumUsers());
	    existingConversation.setConversationStart(newConversationDTO.getConversationStart());
	    existingConversation.setMessages(newConversationDTO.getMessages());

	    Conversation updatedConversation = conversationRepository.save(existingConversation);

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
		Conversation convo = conversationRepository.findById(conversationId).orElseThrow(() -> new ConversationNotFoundException(conversationId));
		User currentUser = service.getUser();
		if(!convo.getConversation_users().contains(currentUser)){
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		conversation.getConversation_users().add(user);
		conversation.setNumUsers(conversation.getNumUsers() + 1);
		user.getList_conversations().add(conversation);
		EntityModel<Conversation> entityModel = conversationAssembler
				.toModel(conversationRepository.save(conversation));
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}
	
	/**
	 * Leave a Conversation
	 *  	- Delete conversation if no users remain
	 * @param conversationId
	 * @return
	 */
	@DeleteMapping("/conversation/leave/{conversationId}")
	ResponseEntity<?> leaveConversation(@PathVariable Long conversationId) {
		Conversation convo = conversationRepository.findById(conversationId).orElseThrow(() -> new ConversationNotFoundException(conversationId));
		User user = service.getUser();
		if(!convo.getConversation_users().contains(user)){
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		Set<Conversation> userConversation = user.getList_conversations();
		Set<User> conversationUsers = convo.getConversation_users();
		userConversation.remove(convo);
		conversationUsers.remove(user);
		convo.setConversation_users(conversationUsers);
		user.setList_conversations(userConversation);
		
		if(userConversation.size() == 0) {
			conversationRepository.deleteById(conversationId);
		} else {
			convo.setNumUsers(convo.getNumUsers() - 1);
			conversationRepository.save(convo);
		}
		userRepository.save(user);
		
		return ResponseEntity.noContent().build();
	}
	

	/**
	 * Delete a conversation
	 * 
	 * @param conversationId
	 * @return
	 */
	@DeleteMapping("/conversations/{conversationId}")
	ResponseEntity<?> deleteConversation(@PathVariable Long conversationId) {
		if(!service.validateAdmin()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		conversationRepository.deleteById(conversationId);
		return ResponseEntity.noContent().build();
	}

}
