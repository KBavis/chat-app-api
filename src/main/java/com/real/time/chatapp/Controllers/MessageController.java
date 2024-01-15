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
import com.real.time.chatapp.ControllerServices.MessageService;
import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.DTO.MessageResponseDTO;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MessageController {
	/**
	 * \ TODO: Consider adding funcitonality to a MessageService class to decluter
	 * funcitonality (good practice)
	 */
	private final MessageModelAssembler messageAssembler;
	private final MessageService messageService;

	/**
	 * Get all messages
	 * 
	 * @return
	 */
	@GetMapping("/messages")
	public CollectionModel<EntityModel<MessageResponseDTO>> all() {
		List<EntityModel<MessageResponseDTO>> messages;
		try {
			messages = messageService.getAllMessages().stream().map(messageAssembler::toModel)
					.collect(Collectors.toList());
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}

		return CollectionModel.of(messages, linkTo(methodOn(MessageController.class).all()).withSelfRel());
	}

	/**
	 * Get all messages for authenticated user
	 * 
	 * @param userId
	 * @return
	 */
	@GetMapping("/userMessages")
	public CollectionModel<EntityModel<MessageResponseDTO>> getUserMessages() {

		List<EntityModel<MessageResponseDTO>> messages = messageService.getAllUserMessages().stream()
				.map(messageAssembler::toModel).collect(Collectors.toList());
		return CollectionModel.of(messages, linkTo(methodOn(MessageController.class).all()).withSelfRel());
	}

	/**
	 * Get a specific message
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/messages/{id}")
	public EntityModel<MessageResponseDTO> one(@PathVariable Long id) {
		Message msg;
		try {
			msg = messageService.getMessageById(id);
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		return messageAssembler.toModel(msg);
	}

	/**
	 * Get messages of a specific conversation
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/messages/conversations/{id}")
	public CollectionModel<EntityModel<MessageResponseDTO>> getConversationMessages(@PathVariable Long id) {
		List<EntityModel<MessageResponseDTO>> entityModels;
		try {
			entityModels = messageService.getConversationMessages(id).stream().map(messageAssembler::toModel)
					.collect(Collectors.toList());
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}

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
	public CollectionModel<EntityModel<MessageResponseDTO>> searchMessagesByContent(@RequestParam("content") String content) {
		List<EntityModel<MessageResponseDTO>> entityModels = messageService.searchMessagesByContent(content).stream()
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
	public CollectionModel<EntityModel<MessageResponseDTO>> searchMessagesByDateSent(
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
		List<EntityModel<MessageResponseDTO>> entityModels = messageService.searchMessagesByDate(date).stream()
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
	public CollectionModel<EntityModel<MessageResponseDTO>> searchMessagesByIsRead() {
		List<EntityModel<MessageResponseDTO>> entityModels = messageService.searchMessagesByRead().stream()
				.map(messageAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(entityModels,
				linkTo(methodOn(MessageController.class).searchMessagesByIsRead()).withSelfRel());
	}
	

	/**
	 * Get most recent message of a conversation
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/messages/recent/{id}")
	public EntityModel<MessageResponseDTO> recent(@PathVariable Long id) {
		Message msg;
		try {
			msg = messageService.getMostRecentMessage(id);
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		return messageAssembler.toModel(msg);
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	@PostMapping("/messages/{conversationId}")
	public ResponseEntity<?> newMessage(@RequestBody MessageDTO messageDTO, @PathVariable Long conversationId) {
		EntityModel<MessageResponseDTO> entityModel;
		try {
			entityModel = messageAssembler.toModel(messageService.createMessage(messageDTO, conversationId));
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
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
	public ResponseEntity<?> updateMessage(@RequestBody MessageDTO newMessage, @PathVariable Long id) {
		EntityModel<MessageResponseDTO> entityModel;
		try {
			entityModel = messageAssembler.toModel(messageService.updateMessage(newMessage, id));
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("/messages/{id}")
	public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
		try {
			messageService.deleteMessage(id);
		} catch (UnauthorizedException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
		}
		return ResponseEntity.noContent().build();
	}
}
