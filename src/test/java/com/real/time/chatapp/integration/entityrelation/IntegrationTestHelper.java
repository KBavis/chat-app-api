package com.real.time.chatapp.integration.entityrelation;

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
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Repositories.UserRepository;
import com.real.time.chatapp.Util.JsonUtil;

/**
 * 
 * @author Kellen Bavis
 * 
 *         Helper Class for AuthenticationControllerTests
 */

public class IntegrationTestHelper {
	private final MockMvc mockMvc;
	private final UserRepository userRepository;

	public IntegrationTestHelper(MockMvc mockMvc, UserRepository userRepository) {
		this.mockMvc = mockMvc;
		this.userRepository = userRepository;
	}

	public ResultActions signUp(String user, String password) throws Exception {
		return mockMvc.perform(post("/auth/register")
				.content(new ObjectMapper()
						.writeValueAsString(RegisterRequest.builder().username(user).password(password).build()))
				.contentType(MediaType.APPLICATION_JSON));
	}

	public ResultActions login(String username, String password) throws Exception {
		return mockMvc.perform(post("/auth/authenticate")
				.content(new ObjectMapper().writeValueAsString(
						AuthenticationRequest.builder().username(username).password(password).build()))
				.contentType(MediaType.APPLICATION_JSON));
	}

	public AuthenticationResponse loginAndReturnToken(String username, String password) throws Exception {
		String loginReturnValueJson = mockMvc.perform(post("/auth/authenticate")
				.content(new ObjectMapper().writeValueAsString(
						AuthenticationRequest.builder().username(username).password(password).build()))
				.contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();
		AuthenticationResponse authResponse = JsonUtil.fromJson(loginReturnValueJson, AuthenticationResponse.class);
		return authResponse;
	}

	public String sendMessage(MessageDTO message, Conversation conversation, AuthenticationResponse authResponse)
			throws Exception {
		return mockMvc.perform(post("/messages/" + conversation.getConversation_id())
				.header("Authorization", "Bearer " + authResponse.getToken())
				.content(new ObjectMapper().writeValueAsString(message)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();
	}

	public Long sendMessageAndReturnId(MessageDTO message, Long conversationId,
			AuthenticationResponse authResponse) throws Exception {
		String messageResponse = mockMvc.perform(post("/messages/" + conversationId)
				.header("Authorization", "Bearer " + authResponse.getToken())
				.content(new ObjectMapper().writeValueAsString(message)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		long messageId = new ObjectMapper().readTree(messageResponse).get("message_id").asLong();
		return messageId;
	}

	public String createConversation(AuthenticationResponse authResponse, User user) throws Exception {
		return mockMvc.perform(post("/conversations/" + user.getUser_id()).header("Authorization",
				"Bearer " + authResponse.getToken())).andReturn().getResponse().getContentAsString();
	}
	
	public Long createConversationAndReturnId(AuthenticationResponse authResponse, User user) throws Exception {
		String conversationResponse = mockMvc.perform(post("/conversations/" + user.getUser_id()).header("Authorization",
				"Bearer " + authResponse.getToken())).andReturn().getResponse().getContentAsString();
		
		long conversationId = new ObjectMapper().readTree(conversationResponse).get("conversation_id").asLong();
		return conversationId;
	}
	
	public String addUserToConversation(Long conversationId, User user, AuthenticationResponse authResponse) throws Exception{
		return mockMvc.perform(put("/conversations/" + conversationId + "/" + user.getUser_id())
				.header("Authorization", "Bearer " + authResponse.getToken())).andReturn().getResponse()
				.getContentAsString();
	}

}
