package com.real.time.chatapp.integration.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.time.chatapp.Auth.AuthenticationRequest;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.RegisterRequest;
import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.DTO.UserDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Repositories.UserRepository;
import com.real.time.chatapp.Util.JsonUtil;

/**
 * 
 * @author Kellen Bavis
 * 
 *         Helper Class for IntegrationControllerTests
 */

public class RestIntegrationTestHelper {
	private final MockMvc mockMvc;
	private final UserRepository userRepository;

	public RestIntegrationTestHelper(MockMvc mockMvc, UserRepository userRepository) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
	}

	protected ResultActions signUp(String user, String password) throws Exception {
		return mockMvc.perform(post("/auth/register")
				.content(new ObjectMapper()
						.writeValueAsString(RegisterRequest.builder().username(user).password(password).build()))
				.contentType(MediaType.APPLICATION_JSON));
	}

	protected ResultActions login(String username, String password) throws Exception {
		return mockMvc.perform(post("/auth/authenticate")
				.content(new ObjectMapper().writeValueAsString(
						AuthenticationRequest.builder().username(username).password(password).build()))
				.contentType(MediaType.APPLICATION_JSON));
	}

	protected AuthenticationResponse loginAndReturnToken(String username, String password) throws Exception {
		String loginReturnValueJson = mockMvc.perform(post("/auth/authenticate")
				.content(new ObjectMapper().writeValueAsString(
						AuthenticationRequest.builder().username(username).password(password).build()))
				.contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();
		AuthenticationResponse authResponse = JsonUtil.fromJson(loginReturnValueJson, AuthenticationResponse.class);
		return authResponse;
	}
	
	protected Conversation addConversation(Long userID, AuthenticationResponse authResponse) throws Exception {
		String createConversationReturnValueJson = mockMvc.perform(post("/conversations/" + userID)
				.header("Authorization", "Bearer " + authResponse.getToken())).andReturn().getResponse().getContentAsString();
		return JsonUtil.fromJson(createConversationReturnValueJson, Conversation.class);
	}
	
	protected Conversation addUserToConversation(Long conversationId, Long userId, AuthenticationResponse authResponse) throws Exception {
		String addUserToConvoReturnValueJson = mockMvc.perform(put("/conversations/" + conversationId + "/" + userId)
				.header("Authorization", "Bearer " + authResponse.getToken())).andReturn().getResponse().getContentAsString();
		
		return JsonUtil.fromJson(addUserToConvoReturnValueJson, Conversation.class);
	}
	
	protected Message sendMessage(Long conversationId, AuthenticationResponse authResponse, String content) throws Exception{
		String sendMessageReturnValueJson = mockMvc.perform(post("/messages/" + conversationId)
				.header("Authorization", "Bearer " + authResponse.getToken())
				.content(new ObjectMapper().writeValueAsString(
						MessageDTO.builder().content(content).build()))
				.contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();
		
		return JsonUtil.fromJson(sendMessageReturnValueJson, Message.class);
	}
	
	protected User updateUserFullName(Long userId, AuthenticationResponse authResponse, String firstName, String lastName) throws Exception {
		String updateUserFullName = mockMvc.perform(put("/users/" + userId)
				.header("Authorization", "Bearer " + authResponse.getToken())
				.content(new ObjectMapper().writeValueAsString(
						UserDTO.builder().firstName(firstName).lastName(lastName).build()))
				.contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();
		
		return JsonUtil.fromJson(updateUserFullName, User.class);
	}


}
