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

import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Exception.MessageNotFoundException;

@RestController
public class MessageController {
	
	private final MessageRepository messageRepository;
	private final MessageModelAssembler messageAssembler;
	
	MessageController(MessageRepository messageRepo, MessageModelAssembler msgAssembler) {
		this.messageRepository = messageRepo;
		this.messageAssembler = msgAssembler;
	}
	
	@GetMapping("/messages")
	CollectionModel<EntityModel<Message>> all(){
		List<EntityModel<Message>> messages = messageRepository.findAll().stream().map(messageAssembler::toModel)
				.collect(Collectors.toList());
		
		return CollectionModel.of(messages,
			linkTo(methodOn(MessageController.class).all()).withSelfRel()
		);
	}
	
	@GetMapping("/messages/{id}")
	EntityModel<Message> one(@PathVariable Long id){
		Message message = messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
		return messageAssembler.toModel(message);
	}
	
	@PostMapping("/messages")
	ResponseEntity<?> newMessage(@RequestBody Message message){
		EntityModel<Message> entityModel = messageAssembler.toModel(messageRepository.save(message));
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
				
	}
	
	@PutMapping("/messages/{id}")
	ResponseEntity<?> updateMessage(@RequestBody Message newMessage, @PathVariable Long id){
		Message updatedMessage = messageRepository.findById(id).map(message -> {
			message.setContent(newMessage.getContent());
			message.setConversation(newMessage.getConversation());
			message.setRecipient(newMessage.getRecipient());
			message.setSendDate(newMessage.getSendDate());
			message.setSender(newMessage.getSender());
			return messageRepository.save(message);
		}).orElseGet(() -> {
			newMessage.setId(id);
			return messageRepository.save(newMessage);
		});
		
		EntityModel<Message> entityModel = messageAssembler.toModel(updatedMessage);
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}
	
	@DeleteMapping("/messages/{id}")
	ResponseEntity<?> deleteMessage(@PathVariable Long id){
		messageRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
