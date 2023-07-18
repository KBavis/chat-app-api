package com.real.time.chatapp.Controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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

import com.real.time.chatapp.Assemblers.MessageModelAssembler;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.MessageNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Exception.UserNotInConversationException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MessageController {
	/**
	 * \ TODO: Consider adding funcitonality to a MessageService class to decluter
	 * funcitonality (good practice)
	 */
	private final MessageRepository messageRepository;
	private final MessageModelAssembler messageAssembler;
	private final UserRepository userRepository;
	private final ConversationRepository conversationRepository;
	private final AuthenticationService service;
	/**
	 * Get all messages
	 * 
	 * @return
	 */
	@GetMapping("/messages")
	public CollectionModel<EntityModel<Message>> all() {
		if(!service.validateAdmin()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		List<EntityModel<Message>> messages = messageRepository.findAll().stream().map(messageAssembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(messages, linkTo(methodOn(MessageController.class).all()).withSelfRel());
	}

	/**
	 *  Get all messages for authenticated user 
	 *  
	 * @param userId
	 * @return
	 */
	@GetMapping("/userMessages")
	public CollectionModel<EntityModel<Message>> getUserMessages() {
		User user = service.getUser();
		List<EntityModel<Message>> messages = messageRepository.findMessagesByUser(user).stream().map(messageAssembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(messages, linkTo(methodOn(MessageController.class).all()).withSelfRel());
	}


	/**
	 * Get a specific message
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/messages/{id}")
	public EntityModel<Message> one(@PathVariable Long id) {
		Message msg = messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
		return messageAssembler.toModel(msg);
	}

	/**
	 * Get messages of a specific conversation
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/messages/conversations/{id}")
	CollectionModel<EntityModel<Message>> getConversationMessages(@PathVariable Long id) {
		Conversation conversation = conversationRepository.findById(id)
				.orElseThrow(() -> new ConversationNotFoundException(id));
		if(!service.validateUserConversation(conversation)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		List<Message> messages = conversation.getMessages();
		List<EntityModel<Message>> entityModels = messages.stream().map(messageAssembler::toModel)
				.collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(MessageController.class).getConversationMessages(id)).withSelfRel());
	}

	/**
	 * Search messages by the content of a message
	 * 
	 * @param content
	 * @return
	 */
	@GetMapping("/search/messages/content")
	CollectionModel<EntityModel<Message>> searchMessagesByContent(@RequestParam("content") String content) {
		User user = service.getUser();
		List<EntityModel<Message>> entityModels = messageRepository.findMessagesByContent(content,user).stream()
				.map(messageAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(MessageController.class).searchMessagesByContent(content)).withSelfRel());
	}

	/**
	 * Search messages by send date
	 * 
	 * @param date
	 * @return
	 */
	@GetMapping("/search/messages/date")
	CollectionModel<EntityModel<Message>> searchMessagesByDateSent(
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
		User user = service.getUser();
		List<EntityModel<Message>> entityModels = messageRepository.findMessagesByDate(date,user).stream()
				.map(messageAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(MessageController.class).searchMessagesByDateSent(date)).withSelfRel());
	}

	/**
	 * Search messages for messages that have not been read
	 * 
	 * @return
	 */
	@GetMapping("/search/messages/read")
	CollectionModel<EntityModel<Message>> searchMessagesByIsRead() {
		User user = service.getUser();
		List<EntityModel<Message>> entityModels = messageRepository.findMessageByIsRead(user).stream()
				.map(messageAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(MessageController.class).searchMessagesByIsRead()).withSelfRel());
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	@PostMapping("/messages/{conversationId}")
	ResponseEntity<?> newMessage(@RequestBody Message message,
			@PathVariable Long conversationId) {
		User sender = service.getUser();
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		if (!conversation.getConversation_users().contains(sender)) {
			throw new UserNotInConversationException(sender.getUser_id(), conversationId);
		}
		
		//Set Recipients of Message As All Conversation Users Other Than Sender 
		Set<User> conversationUsers = conversation.getConversation_users(); 
		Set<User> recipients = new HashSet<>();
		for(User recipient: conversationUsers) {
			if(recipient != sender) {
				recipients.add(recipient);
			}
		}
		message.setRecipients(recipients);
		message.setSender(sender);
		message.setConversation(conversation);

		// Add Messages to List of Messages For The Current Conversation
		List<Message> conversationMessages = conversation.getMessages();
		conversationMessages.add(message);
		conversation.setMessages(conversationMessages);

		// Add Sent Messages to the List of Sender's Sent Messages
		List<Message> sentMessages = sender.getSentMessages();
		sentMessages.add(message);
		sender.setSentMessages(sentMessages);

		// Add Recieved Messages to the List of Each Recievers Recieved Messages
		for(User recipient: recipients) {
			Set<Message> recievedMessages = recipient.getRecievedMessages();
			recievedMessages.add(message);
			recipient.setRecievedMessages(recievedMessages);
		}

		EntityModel<Message> entityModel = messageAssembler.toModel(messageRepository.save(message));
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);

	}

	/**
	 * Update a message
	 * 
	 * @param newMessage
	 * @param id
	 * @return
	 */
	@PutMapping("/messages/{id}")
	ResponseEntity<?> updateMessage(@RequestBody Message newMessage, @PathVariable Long id) {
		Message updatedMessage = messageRepository.findById(id).map(message -> {
			message.setContent(newMessage.getContent());
			message.setConversation(newMessage.getConversation());
			message.setRecipients(newMessage.getRecipients());
			message.setSendDate(newMessage.getSendDate());
			message.setSender(newMessage.getSender());
			return messageRepository.save(message);
		}).orElseGet(() -> {
			newMessage.setMessage_id(id);
			return messageRepository.save(newMessage);
		});

		EntityModel<Message> entityModel = messageAssembler.toModel(updatedMessage);
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	/**
	 * Delete a message TODO: Should a user that sent the message be the only one
	 * allowd to delete the message??
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("/messages/{id}")
	ResponseEntity<?> deleteMessage(@PathVariable Long id) {
		if(!service.validateMessage(id)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to perform this action.");
		}
		messageRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
