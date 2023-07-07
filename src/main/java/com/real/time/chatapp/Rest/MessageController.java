package com.real.time.chatapp.Rest;

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

import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.MessageNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Exception.UserNotInConversationException;

@RestController
public class MessageController {

	// TODO: Add logic in MessageService and Not in the MessageController

	private final MessageRepository messageRepository;
	private final MessageModelAssembler messageAssembler;
	private final UserRepository userRepository;
	private final ConversationRepository conversationRepository;

	MessageController(MessageRepository messageRepo, MessageModelAssembler msgAssembler, UserRepository userRepository,
			ConversationRepository conversationRepository) {
		this.messageRepository = messageRepo;
		this.messageAssembler = msgAssembler;
		this.conversationRepository = conversationRepository;
		this.userRepository = userRepository;
	}

	/**
	 * Get all messages
	 * 
	 * @return
	 */
	@GetMapping("/messages")
	CollectionModel<EntityModel<Message>> all() {
		List<EntityModel<Message>> messages = messageRepository.findAll().stream().map(messageAssembler::toModel)
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
	EntityModel<Message> one(@PathVariable Long id) {
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
		List<EntityModel<Message>> entityModels = messageRepository.findMessagesByContent(content).stream()
				.map(messageAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(MessageController.class).searchMessagesByContent(content)).withSelfRel());
	}
	
	/**
	 * Search messages by send date
	 * @param date
	 * @return
	 */
	@GetMapping("/search/messages/date")
	CollectionModel<EntityModel<Message>> searchMessagesByDateSent(
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
		List<EntityModel<Message>> entityModels = messageRepository.findMessagesByDate(date).stream()
				.map(messageAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(MessageController.class).searchMessagesByDateSent(date)).withSelfRel());
	}

	/**
	 * Search messages for messages that have not been read
	 * @return
	 */
	@GetMapping("/search/messages/read")
	CollectionModel<EntityModel<Message>> searchMessagesByIsRead() {
		List<EntityModel<Message>> entityModels = messageRepository.findMessageByIsRead().stream()
				.map(messageAssembler::toModel).collect(Collectors.toList());
		
		return CollectionModel.of(entityModels,
				linkTo(methodOn(MessageController.class).searchMessagesByIsRead()).withSelfRel());
	}

	/**
	 * Add a new messages TODO: Update this function to create a new message by
	 * sending it to a conversation - All Users within this Conversations will
	 * Recieve this Message - Endpoin: /messages/{conversationId}/{senderId} -->
	 * where senderId, is the userID of the sende
	 * 
	 * @param message
	 * @return
	 */
	/**
	 * TODO Maybe make this so we don't need to specify a userIdOne and userIdTwo -
	 * instead, it could simply be the conversationId, and maybe the senderId - that
	 * way, we could set the the messageSender as the stated senderId - and we could
	 * change the messageRecipient to a List<User> - whatever users that are in this
	 * conversation that are not the sender would be set as a message recipient -
	 * this means we would have to update our policies on cascading/removing
	 */

	@PostMapping("/messages/{conversationId}/{userIdOne}/{userIdTwo}")
	ResponseEntity<?> newMessage(@RequestBody Message message, @PathVariable Long userIdOne,
			@PathVariable Long userIdTwo, @PathVariable Long conversationId) {
		User sender = userRepository.findById(userIdOne).orElseThrow(() -> new UserNotFoundException(userIdOne));
		User reciever = userRepository.findById(userIdTwo).orElseThrow(() -> new UserNotFoundException(userIdTwo));

		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		if (!conversation.getConversation_users().contains(sender)) {
			throw new UserNotInConversationException(userIdOne, conversationId);
		}
		if (!conversation.getConversation_users().contains(reciever)) {
			throw new UserNotInConversationException(userIdTwo, conversationId);
		}

		message.setRecipient(reciever);
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

		// Add Recieved Messages to the List of Recievers Recieved Messages
		List<Message> recievedMessages = reciever.getRecievedMessages();
		recievedMessages.add(message);
		reciever.setRecievedMessages(recievedMessages);

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
			message.setRecipient(newMessage.getRecipient());
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
		messageRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
