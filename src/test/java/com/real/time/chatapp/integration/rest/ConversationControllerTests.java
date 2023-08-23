package com.real.time.chatapp.integration.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.LoadAdmin;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.UserRepository;
import com.real.time.chatapp.Util.JsonUtil;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class ConversationControllerTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	private RestIntegrationTestHelper testHelper;
	
	
	private AuthenticationResponse adminAuthResponse;
	
	String adminPassword;
	
	User u1;
	User u2;
	User u3;
	User u4;
	AuthenticationResponse u1_auth;
	AuthenticationResponse u2_auth;
	AuthenticationResponse u3_auth;
	AuthenticationResponse u4_auth;
	Conversation c1;
	Conversation c2;
	Conversation c3;
	
	@BeforeEach 
	void setUp() throws Exception{
		//Initalize Test Helper 
		testHelper = new RestIntegrationTestHelper(mockMvc, userRepository);
		
		//Set Up Admin Authentication 
		Properties properties = new Properties();
		try (InputStream inputStream = LoadAdmin.class.getClassLoader().getResourceAsStream("application.properties")) {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		adminPassword = properties.getProperty("admin.password");
		adminAuthResponse = testHelper.loginAndReturnToken("AdminUser", adminPassword);
		
		//Sign Up Mock Users 
		testHelper.signUp("u1", "password");
		testHelper.signUp("u2", "password");
		testHelper.signUp("u3", "password");
		testHelper.signUp("u4", "password");
		
		//Fetch Signed Up Users 
		u1 = userRepository.findByUserName("u1").orElseThrow(() -> new UserNotFoundException("u1"));
		u2 = userRepository.findByUserName("u2").orElseThrow(() -> new UserNotFoundException("u2"));
		u3 = userRepository.findByUserName("u3").orElseThrow(() -> new UserNotFoundException("u3"));
		u4 = userRepository.findByUserName("u4").orElseThrow(() -> new UserNotFoundException("u4"));
		
		//Set Up User Authentications 
		u1_auth = testHelper.loginAndReturnToken("u1", "password");
		u2_auth = testHelper.loginAndReturnToken("u2", "password");
		u3_auth = testHelper.loginAndReturnToken("u3", "password");
		u4_auth = testHelper.loginAndReturnToken("u4", "password");
		
		/**
		 * Set Up Mock Conversations
		 */
		//Convo 1 Contains All Users
		c1 = testHelper.addConversation(u2.getUser_id(), u1_auth);
		testHelper.addUserToConversation(c1.getConversation_id(), u3.getUser_id(), u1_auth);
		testHelper.addUserToConversation(c1.getConversation_id(), u4.getUser_id(), u1_auth);
		
		//Convo 2 Contains U1 and U2
		c2 = testHelper.addConversation(u2.getUser_id(), u1_auth);
		
		//Convo 3 Contains U3 and U4
		c3 = testHelper.addConversation(u4.getUser_id(), u4_auth);
	}
	
	@Test
	@Transactional
	void test_getAllConversations() throws Exception{
		//Create Conversations
		String responseJson = mockMvc.perform(get("/conversations").header("Authorization", "Bearer " + adminAuthResponse.getToken())).andReturn().getResponse().getContentAsString();
		CollectionModel<EntityModel<Conversation>> conversations = JsonUtil.fromJson(responseJson, CollectionModel<EntityModel>);
		
	}
}
