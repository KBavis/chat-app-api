package com.real.time.chatapp.Assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.real.time.chatapp.Controllers.ConversationController;
import com.real.time.chatapp.DTO.ConversationResponseDTO;
import com.real.time.chatapp.DTO.MessageResponseDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;

@Component
public class ConversationModelAssembler
		implements RepresentationModelAssembler<Conversation, EntityModel<ConversationResponseDTO>> {

	@Override
	public EntityModel<ConversationResponseDTO> toModel(Conversation conversation) {
		ConversationResponseDTO responseDTO = new ConversationResponseDTO();
		responseDTO.setConversation_id(conversation.getConversation_id());
		responseDTO.setConversationStart(conversation.getConversationStart());
		responseDTO.setNumUsers(conversation.getNumUsers());
		// TODO: Make necessary updates to test cases for COnversationResponseDTO taking
		// in MessageResponseDTOs instead of Messages
		List<MessageResponseDTO> responseDTOes = new ArrayList<>();
		List<Message> conversationMessages = conversation.getMessages();
		if (conversationMessages != null) {
			for (int i = 0; i < conversationMessages.size(); i++) {
				Message m = conversationMessages.get(i);
				MessageResponseDTO mDTO = MessageResponseDTO.builder().content(m.getContent()).sendDate(m.getSendDate())
						.message_id(m.getMessage_id()).sender(m.getSender()).conversation(m.getConversation()).build();
				responseDTOes.add(mDTO);
			}
		}
		responseDTO.setMessages(responseDTOes);
		// responseDTO.setMessages(conversation.getMessages());
		responseDTO.setUsers(new ArrayList<>(conversation.getConversation_users()));

		return EntityModel.of(responseDTO,
				linkTo(methodOn(ConversationController.class).one(conversation.getConversation_id())).withSelfRel(),
				linkTo(methodOn(ConversationController.class).all()).withRel("/conversations"));
	}
}
