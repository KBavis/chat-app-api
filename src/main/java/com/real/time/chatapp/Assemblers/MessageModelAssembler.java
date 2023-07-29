package com.real.time.chatapp.Assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.real.time.chatapp.Controllers.MessageController;
import com.real.time.chatapp.Entities.Message;

@Component
public class MessageModelAssembler implements RepresentationModelAssembler<Message,EntityModel<Message>>{
	
	@Override
	public EntityModel<Message> toModel(Message message){
		return EntityModel.of(message,
			linkTo(methodOn(MessageController.class).one(message.getMessage_id())).withSelfRel(),
			linkTo(methodOn(MessageController.class).all()).withRel("/messages")
		); 
	}	
}