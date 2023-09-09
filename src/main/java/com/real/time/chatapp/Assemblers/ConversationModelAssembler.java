package com.real.time.chatapp.Assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.real.time.chatapp.Controllers.ConversationController;
import com.real.time.chatapp.DTO.ConversationResponseDTO;
import com.real.time.chatapp.Entities.Conversation;

@Component
public class ConversationModelAssembler implements RepresentationModelAssembler<Conversation, EntityModel<ConversationResponseDTO>> {

    @Override
    public EntityModel<ConversationResponseDTO> toModel(Conversation conversation) {
        ConversationResponseDTO responseDTO = new ConversationResponseDTO();
        responseDTO.setConversation_id(conversation.getConversation_id());
        responseDTO.setConversationStart(conversation.getConversationStart());
        responseDTO.setNumUsers(conversation.getNumUsers());
        responseDTO.setMessages(conversation.getMessages());
        responseDTO.setUsers(new ArrayList<>(conversation.getConversation_users()));

        return EntityModel.of(responseDTO,
                linkTo(methodOn(ConversationController.class).one(conversation.getConversation_id())).withSelfRel(),
                linkTo(methodOn(ConversationController.class).all()).withRel("/conversations")
        );
    }
}
