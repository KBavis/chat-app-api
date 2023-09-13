package com.real.time.chatapp.integration.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.LoadAdmin;
import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class MessageControllerEndpointAccessTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ConversationRepository conversationRepository;

	private AuthenticationTestHelper testHelper;

	private String adminPassword;
	
	private Long messageId;
	
	private Long conversationId;
	
	private MessageDTO messageDTO;
	
	@BeforeEach
	@Transactional
	void setup() throws Exception{
		testHelper = new AuthenticationTestHelper(mockMvc, userRepository);
		Properties properties = new Properties();
		try (InputStream inputStream = LoadAdmin.class.getClassLoader().getResourceAsStream("application.properties")) {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		adminPassword = properties.getProperty("admin.password");
		//Testing 
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		testHelper.signUp("testUser3", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		conversationId = createMockConversation(response.getToken(), "testUser2");
		messageDTO = new MessageDTO(); 
		messageDTO.setContent("Hi testUser2");
		messageId = createMockMessage(response.getToken(), conversationId, messageDTO);
	}
	
	
	//********************************************************************
	//----------ACCESS RETURNS FORBIDDEN/UNAUTHORIZED TEST CASES----------
	//********************************************************************
	@Test
	@Transactional
	void test_messages_returnsForbidden() throws Exception {
		mockMvc.perform(get("/messages")).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_messages_returnsUnauthorized() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser3", "password");
		mockMvc.perform(get("/messages").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_userMessages_returnsForbidden() throws Exception {
		mockMvc.perform(get("/userMessages")).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_messageById_returnsForbidden() throws Exception {
		mockMvc.perform(get("/messages/" + messageId)).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_messageByConversation_returnsForbidden() throws Exception {
		mockMvc.perform(get("/messages/conversations/" + conversationId)).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_messageByConversation_returnsUnauthorized() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser3", "password");
		mockMvc.perform(get("/messages/conversations/" + conversationId).header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_searchMessagesByContent_returnsForbidden() throws Exception {
		mockMvc.perform(get("/search/messages/content?=Hi")).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_searchMessagesByDate_returnsForbidden() throws Exception {
		mockMvc.perform(get("/search/messages/date?=" + new Date())).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_searchMessagesByRead_returnsForbidden() throws Exception {
		mockMvc.perform(get("/search/messages/read")).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_createNewMessage_returnsForbidden() throws Exception {
		mockMvc.perform(post("/messages/" + conversationId).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_createNewMessage_returnsUnauthorized() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser3", "password");
		mockMvc.perform(post("/messages/" + conversationId).header("Authorization", "Bearer " + authResponse.getToken()).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_updateMessage_returnsForbidden() throws Exception {
		mockMvc.perform(put("/messages/" + messageId).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_updateMessage_returnsUnathorized() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser2", "password");
		mockMvc.perform(put("/messages/" + messageId).header("Authorization", "Bearer " + authResponse.getToken()).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_deleteMessage_returnsForbidden() throws Exception {
		mockMvc.perform(delete("/messages/" + messageId).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_deleteMessage_returnsUnauthorized() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser2", "password");
		mockMvc.perform(delete("/messages/" + messageId).header("Authorization", "Bearer " + authResponse.getToken()).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}
	
	
	//********************************************************
	//----------ACCESS RETURNS OK/CREATED TEST CASES----------
	//********************************************************
	@Test
	@Transactional
	void test_messages_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("AdminUser", adminPassword);
		mockMvc.perform(get("/messages").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_userMessages_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(get("/userMessages").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_messageById_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(get("/messages/" + messageId).header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_messageByConversation_returnOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(get("/messages/conversations/" + conversationId).header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_searchMessagesByContent_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(get("/search/messages/content?content=Hi").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_searchMessagesByDate_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		mockMvc.perform(get("/search/messages/date?date=" + date.format(formatter)).header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_searchMessagesByRead_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(get("/search/messages/read").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_createNewMessage_returnsCreated() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(post("/messages/" + conversationId).header("Authorization", "Bearer " + authResponse.getToken()).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
	}
	
	@Test
	@Transactional
	void test_updateMessage_returnsCreated() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(put("/messages/" + messageId).header("Authorization", "Bearer " + authResponse.getToken()).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
	}
	
	@Test
	@Transactional
	void test_deleteMessage_returnsNoContent() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(delete("/messages/" + messageId).header("Authorization", "Bearer " + authResponse.getToken()).content(new ObjectMapper().writeValueAsString(messageDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
	}	
	
	/**
	 * Helper Method to Create Mock Conversation
	 */
	protected Long createMockConversation(String token, String username) throws Exception {

		User userTwo = userRepository.findByUserName(username).orElseThrow(() -> new UserNotFoundException("test2"));

		mockMvc.perform(post("/conversations/" + userTwo.getUser_id()).header("Authorization", "Bearer " + token));

		List<Conversation> conversations = conversationRepository.findConversationsByUser(userTwo);
		if (conversations == null)
			throw new RuntimeException("Conversation not found");
		Conversation createdConversation = conversations.get(0);
		return createdConversation.getConversation_id();
	}
	
	/*
	 *  Helper Method to Create Mock Message
	 */
	protected Long createMockMessage(String token, Long conversationId, MessageDTO message) throws Exception{
		mockMvc.perform(post("/messages/" + conversationId).content(new ObjectMapper().writeValueAsString(message)).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON));
		Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ConversationNotFoundException(conversationId));
		List<Message> messages = conversation.getMessages();
		return messages.get(0).getMessage_id();
	}
}
