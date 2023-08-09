package com.real.time.chatapp.integration.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.real.time.chatapp.DTO.ConversationDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class CoversationControllerEndpointAccessTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ConversationRepository conversationRepository;

	@Autowired
	private UserRepository userRepository;

	private AuthenticationTestHelper testHelper;

	private String adminPassword;
	

	@BeforeEach
	void setup() {
		testHelper = new AuthenticationTestHelper(mockMvc, userRepository);
		Properties properties = new Properties();
		try (InputStream inputStream = LoadAdmin.class.getClassLoader().getResourceAsStream("application.properties")) {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		adminPassword = properties.getProperty("admin.password");
	}

	
	//************************************************
	//----------ACCESS RETURNS FORBIDDEN TEST CASES----------
	//************************************************
	
	@Test
	void test_conversation_returnsForbidden() throws Exception {
		mockMvc.perform(get("/conversations")).andExpect(status().isForbidden());
	}
	
	@Test
	void test_conversation_returnsUnauthorized() throws Exception {
		testHelper.signUp("test1", "password");
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test1", "password");
		mockMvc.perform(get("/conversations").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_userConversation_returnsForbidden() throws Exception {

		mockMvc.perform(get("/userConversations"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_conversationById_returnsForbidden() throws Exception {
		testHelper.signUp("test1", "password");
		testHelper.signUp("test2", "password");
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test1", "password");

		long conversationId = createMockConversation(authResponse.getToken(), "test2");
		mockMvc.perform(
				get("/conversations/" + conversationId))
				.andExpect(status().isForbidden());
	}
	
	
	@Test
	@Transactional
	void test_conversationById_returnsUnauthorized() throws Exception {
		testHelper.signUp("test1", "password");
		testHelper.signUp("test2", "password");
		testHelper.signUp("test3", "password");
		AuthenticationResponse wrongAuthResponse = testHelper.loginAndReturnToken("test3", "password");
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test1", "password");
		long conversationId = createMockConversation(authResponse.getToken(), "test2");
		mockMvc.perform(
				get("/conversations/" + conversationId)
				.header("Authorization", "Bearer " + wrongAuthResponse.getToken()))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_searchConversationsByDates_returnsForbidden() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");

		createMockConversation(response.getToken(), "testUser2");

		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		mockMvc.perform(get("/search/conversations?date=" + date.format(formatter))).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_searchConversationsByUser_returnsForbidden() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");

		createMockConversation(response.getToken(), "testUser2");

		User user = userRepository.findByUserName("testUser2")
				.orElseThrow(() -> new UserNotFoundException("testUser2"));
		Long userId = user.getUser_id();

		mockMvc.perform(get("/search/conversations/" + userId))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_createConversation_returnsForbidden() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");

		User userTwo = userRepository.findByUserName("testUser2").orElseThrow(() -> new UserNotFoundException("test2"));

		mockMvc.perform(
				post("/conversations/" + userTwo.getUser_id()))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_updateConversation_returnsForbidden() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		ConversationDTO conversationDTO = new ConversationDTO();
		conversationDTO.setConversationStart(new Date());
		conversationDTO.setMessages(new ArrayList<>());
		conversationDTO.setNumUsers(0);
		
		mockMvc.perform(put("/conversation/" + conversationId)
				.content(new ObjectMapper().writeValueAsString(conversationDTO)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_updateConversation_returnsUnauthorized() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		testHelper.signUp("testUser3", "password");
		AuthenticationResponse response2 = testHelper.loginAndReturnToken("testUser3", "password");
		
		ConversationDTO conversationDTO = new ConversationDTO();
		conversationDTO.setConversationStart(new Date());
		conversationDTO.setMessages(new ArrayList<>());
		conversationDTO.setNumUsers(0);
		
		mockMvc.perform(put("/conversation/" + conversationId)
				.header("Authorization", "Bearer " + response2.getToken())
				.content(new ObjectMapper().writeValueAsString(conversationDTO)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_addUserToConversation_returnsForbidden() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		testHelper.signUp("testUser3", "password");
		User user = userRepository.findByUserName("testUser3").orElseThrow(() -> new UserNotFoundException("testUser3"));
		
		
		mockMvc.perform(
				put("/conversations/" + conversationId + "/" +  user.getUser_id()))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_addUserToConversation_returnsUnauthorized() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		testHelper.signUp("testUser3", "password");
		User user = userRepository.findByUserName("testUser3").orElseThrow(() -> new UserNotFoundException("testUser3"));
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser3", "password");
		
		mockMvc.perform(
				put("/conversations/" + conversationId + "/" +  user.getUser_id())
				.header("Authorization", "Bearer " + authResponse.getToken()))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_leaveConversation_returnsForbidden() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		mockMvc.perform(
				delete("/conversation/leave/" + conversationId))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_leaveConversation_returnsUnauthorized() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		testHelper.signUp("testUser3", "password");
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser3", "password");
		mockMvc.perform(
				delete("/conversation/leave/" + conversationId)
				.header("Authorization", "Bearer " + authResponse.getToken()))
				.andExpect(status().isUnauthorized());
	}
	
	
	@Test
	@Transactional
	void test_deleteConversation_returnsUnauthorized() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse responseOne = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(responseOne.getToken(), "testUser2");
		mockMvc.perform(delete("/conversations/" + conversationId).header("Authorization", "Bearer " + responseOne.getToken()))
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_deleteConversation_returnsForbidden() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse responseOne = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(responseOne.getToken(), "testUser2");
		mockMvc.perform(delete("/conversations/" + conversationId))
				.andExpect(status().isForbidden());
	}
	
	
	
	//********************************************************
	//----------ACCESS RETURNS OK/CREATED TEST CASES----------
	//********************************************************
	@Test
	void test_conversation_returnsOk() throws Exception {
		AuthenticationResponse response = testHelper.loginAndReturnToken("AdminUser", adminPassword);
		mockMvc.perform(get("/conversations").header("Authorization", "Bearer " + response.getToken()))
				.andExpect(status().isOk());
	}

	@Test
	@Transactional
	void test_userConversation_returnsOk() throws Exception {
		testHelper.signUp("test", "password");
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test", "password");

		mockMvc.perform(get("/userConversations").header("Authorization", "Bearer " + authResponse.getToken()))
				.andExpect(status().isOk());

	}

	@Test
	@Transactional
	void test_conversationById_returnsOk() throws Exception {
		testHelper.signUp("test1", "password");
		testHelper.signUp("test2", "password");
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test1", "password");

		long conversationId = createMockConversation(authResponse.getToken(), "test2");
		mockMvc.perform(
				get("/conversations/" + conversationId).header("Authorization", "Bearer " + authResponse.getToken()))
				.andExpect(status().isOk());
	}

	@Test
	@Transactional
	void test_searchConversationsByDates_returnsOk() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");

		createMockConversation(response.getToken(), "testUser2");

		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		mockMvc.perform(get("/search/conversations?date=" + date.format(formatter)).header("Authorization",
				"Bearer " + response.getToken())).andExpect(status().isOk());
	}

	@Test
	@Transactional
	void test_searchConversationsByUser_returnsOk() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");

		createMockConversation(response.getToken(), "testUser2");

		User user = userRepository.findByUserName("testUser2")
				.orElseThrow(() -> new UserNotFoundException("testUser2"));
		Long userId = user.getUser_id();

		mockMvc.perform(get("/search/conversations/" + userId).header("Authorization", "Bearer " + response.getToken()))
				.andExpect(status().isOk());
	}

	@Test
	@Transactional
	void test_createConversation_returnsCreated() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");

		User userTwo = userRepository.findByUserName("testUser2").orElseThrow(() -> new UserNotFoundException("test2"));

		mockMvc.perform(
				post("/conversations/" + userTwo.getUser_id()).header("Authorization", "Bearer " + response.getToken()))
				.andExpect(status().isCreated());
	}

	@Test
	@Transactional
	void test_updateConversation_returnsCreated() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		ConversationDTO conversationDTO = new ConversationDTO();
		conversationDTO.setConversationStart(new Date());
		conversationDTO.setMessages(new ArrayList<>());
		conversationDTO.setNumUsers(0);
		
		mockMvc.perform(put("/conversation/" + conversationId)
				.header("Authorization", "Bearer " + response.getToken())
				.content(new ObjectMapper().writeValueAsString(conversationDTO)).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}
	
	@Test
	@Transactional
	void test_addUserToConversation_returnsCreated() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		testHelper.signUp("testUser3", "password");
		User user = userRepository.findByUserName("testUser3").orElseThrow(() -> new UserNotFoundException("testUser3"));
		
		
		mockMvc.perform(
				put("/conversations/" + conversationId + "/" +  user.getUser_id()).header("Authorization", "Bearer " + response.getToken()))
				.andExpect(status().isCreated());
	}
	
	@Test
	@Transactional
	void test_leaveConversation_returnsNoContent() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse response = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(response.getToken(), "testUser2");
		
		mockMvc.perform(
				delete("/conversation/leave/" + conversationId).header("Authorization", "Bearer " + response.getToken()))
				.andExpect(status().isNoContent());
	}
	
	@Test
	@Transactional
	void test_deleteConversation_returnsNoContent() throws Exception {
		testHelper.signUp("testUser1", "password");
		testHelper.signUp("testUser2", "password");
		AuthenticationResponse responseOne = testHelper.loginAndReturnToken("testUser1", "password");
		Long conversationId = createMockConversation(responseOne.getToken(), "testUser2");
		AuthenticationResponse responseTwo = testHelper.loginAndReturnToken("AdminUser", adminPassword);
		mockMvc.perform(delete("/conversations/" + conversationId).header("Authorization", "Bearer " + responseTwo.getToken()))
				.andExpect(status().isNoContent());
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

}
