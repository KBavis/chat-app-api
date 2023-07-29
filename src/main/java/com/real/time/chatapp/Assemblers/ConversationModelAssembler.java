package com.real.time.chatapp.Assemblers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.real.time.chatapp.Controllers.ConversationController;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;

@Component
public class ConversationModelAssembler implements RepresentationModelAssembler<Conversation,EntityModel<Conversation>>{
	
	@Override
	public EntityModel<Conversation> toModel(Conversation conversation){
		return EntityModel.of(conversation, 
			linkTo(methodOn(ConversationController.class).one(conversation.getConversation_id())).withSelfRel(),
			linkTo(methodOn(ConversationController.class).all()).withRel("/conversations")
		);
	}
}
