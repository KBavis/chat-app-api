package com.real.time.chatapp.Assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.real.time.chatapp.Controllers.MessageController;
import com.real.time.chatapp.DTO.MessageResponseDTO;
import com.real.time.chatapp.Entities.Message;

@Component
public class MessageModelAssembler implements RepresentationModelAssembler<Message,EntityModel<MessageResponseDTO>>{
	
	@Override
	public EntityModel<MessageResponseDTO> toModel(Message message){
		MessageResponseDTO responseDTO = new MessageResponseDTO();
		responseDTO.setContent(message.getContent());
		responseDTO.setConversation(message.getConversation());
		responseDTO.setMessage_id(message.getMessage_id());
		responseDTO.setRead(message.isRead());
		responseDTO.setSender(message.getSender());
		responseDTO.setRecipients(message.getRecipients());
		responseDTO.setSendDate(message.getSendDate());
		return EntityModel.of(responseDTO,
			linkTo(methodOn(MessageController.class).one(message.getMessage_id())).withSelfRel(),
			linkTo(methodOn(MessageController.class).all()).withRel("/messages")
		); 
	}	
}
