package com.real.time.chatapp.Assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.real.time.chatapp.Controllers.UserController;
import com.real.time.chatapp.DTO.UserResponseDTO;
import com.real.time.chatapp.Entities.User;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<UserResponseDTO>>{
	
	@Override
	public EntityModel<UserResponseDTO> toModel(User user){
		UserResponseDTO responseDTO = new UserResponseDTO();
		responseDTO.setUser_id(user.getUser_id());
		responseDTO.setUserName(user.getUsername());
		responseDTO.setFirstName(user.getFirstName());
		responseDTO.setLastName(user.getLastName());
		responseDTO.setPassword(user.getPassword());
		responseDTO.setRole(user.getRole());
		responseDTO.setList_conversations(user.getList_conversations());
		responseDTO.setSentMessges(user.getSentMessages());
		responseDTO.setRecievedMessages(user.getRecievedMessages());
		return EntityModel.of(responseDTO,
			linkTo(methodOn(UserController.class).one(user.getUser_id())).withSelfRel(),
			linkTo(methodOn(UserController.class).all()).withRel("/users"));
	}
}

