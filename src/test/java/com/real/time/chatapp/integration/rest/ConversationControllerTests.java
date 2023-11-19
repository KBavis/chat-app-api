package com.real.time.chatapp.integration.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.LoadAdmin;
import com.real.time.chatapp.DTO.ConversationDTO;
import com.real.time.chatapp.DTO.ConversationResponseDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.UserRepository;

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
	private ConversationRepository conversationRepository;

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
	ObjectMapper objectMapper;

	@BeforeEach
	@Transactional
	void setUp() throws Exception {
		// Initalize Test Helper
		testHelper = new RestIntegrationTestHelper(mockMvc, userRepository);
		objectMapper = new ObjectMapper();

		// Set Up Admin Authentication
		Properties properties = new Properties();
		try (InputStream inputStream = LoadAdmin.class.getClassLoader().getResourceAsStream("application.properties")) {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		adminPassword = properties.getProperty("admin.password");
		adminAuthResponse = testHelper.loginAndReturnToken("AdminUser", adminPassword);

		// Sign Up Mock Users
		testHelper.signUp("u1", "password");
		testHelper.signUp("u2", "password");
		testHelper.signUp("u3", "password");
		testHelper.signUp("u4", "password");

		// Fetch Signed Up Users
		u1 = userRepository.findByUserName("u1").orElseThrow(() -> new UserNotFoundException("u1"));
		u2 = userRepository.findByUserName("u2").orElseThrow(() -> new UserNotFoundException("u2"));
		u3 = userRepository.findByUserName("u3").orElseThrow(() -> new UserNotFoundException("u3"));
		u4 = userRepository.findByUserName("u4").orElseThrow(() -> new UserNotFoundException("u4"));

		// Set Up User Authentications
		u1_auth = testHelper.loginAndReturnToken("u1", "password");
		u2_auth = testHelper.loginAndReturnToken("u2", "password");
		u3_auth = testHelper.loginAndReturnToken("u3", "password");
		u4_auth = testHelper.loginAndReturnToken("u4", "password");

		/**
		 * Set Up Mock Conversations
		 */
		// Convo 1 Contains All Users
		c1 = testHelper.addConversation(u2.getUser_id(), u1_auth);
		testHelper.addUserToConversation(c1.getConversation_id(), u3.getUser_id(), u1_auth);
		testHelper.addUserToConversation(c1.getConversation_id(), u4.getUser_id(), u1_auth);

		// Convo 2 Contains U1 and U2
		c2 = testHelper.addConversation(u2.getUser_id(), u1_auth);

		// Convo 3 Contains U3 and U4
		c3 = testHelper.addConversation(u3.getUser_id(), u4_auth);
	}

	@Test
	@Transactional
	void test_getAllConversations() throws Exception {
		// Fetching Converastions
		String responseJson = mockMvc
				.perform(get("/conversations").header("Authorization", "Bearer " + adminAuthResponse.getToken()))
				.andReturn().getResponse().getContentAsString();
		

		//Extracting Coversations From Response
		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode conversationsNode = rootNode.path("_embedded").path("conversationResponseDTOes");
		List<EntityModel<ConversationResponseDTO>> entityModels = objectMapper.readValue(conversationsNode.toString(), new TypeReference<List<EntityModel<ConversationResponseDTO>>>() {});
		List<ConversationResponseDTO> conversations = entityModels.stream()
				.map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());
		
		
		//Assertions
		for(ConversationResponseDTO c: conversations) {
			boolean u1Found = false;
			boolean u2Found = false;
			boolean u3Found = false;
			boolean u4Found = false;
			
			if(c.getConversation_id().equals(c1.getConversation_id())){
				assertTrue(c.getNumUsers() == 4);
				for(User u: c.getUsers()) {
					if(u.getUsername().equals("u1") && u.getUser_id().equals(u1.getUser_id())) {
						u1Found = true;
					} else if(u.getUsername().equals("u2") && u.getUser_id().equals(u2.getUser_id())) {
						u2Found = true;
					} else if(u.getUsername().equals("u3") && u.getUser_id().equals(u3.getUser_id())) {
						u3Found = true;
					} else if(u.getUsername().equals("u4") && u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					}else {
						fail("Unexpected User Found");
					}
				}
				assertTrue(u1Found == true && u2Found == true && u3Found == true && u4Found == true);
			} else if(c.getConversation_id().equals(c2.getConversation_id())) {
				assertTrue(c.getNumUsers() == 2);
				for(User u: c.getUsers()) {
					if(u.getUsername().equals("u1") && u.getUser_id().equals(u1.getUser_id())) {
						u1Found = true;
					} else if(u.getUsername().equals("u2") && u.getUser_id().equals(u2.getUser_id())) {
						u2Found = true;
					}else {
						fail("Unexpected User Found");
					}
				}
				assertTrue(u1Found == true && u2Found == true);
			} else if(c.getConversation_id().equals(c3.getConversation_id())) {
				assertTrue(c.getNumUsers() == 2);
				for(User u: c.getUsers()) {
					if(u.getUsername().equals("u3") && u.getUser_id().equals(u3.getUser_id())) {
						u3Found = true;
					} else if(u.getUsername().equals("u4") && u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					}else {
						fail("Unexpected User Found");
					}
				}
				assertTrue(u3Found == true && u4Found == true);
			} 
		}
	}
		
		@Test
		@Transactional
		void test_getUserConversations() throws Exception{
			// Fetching User Converastions
			String responseJson = mockMvc
					.perform(get("/userConversations").header("Authorization", "Bearer " + u1_auth.getToken()))
					.andReturn().getResponse().getContentAsString();
			
			//Extracting Coversations From Response
			JsonNode rootNode = objectMapper.readTree(responseJson);
			JsonNode conversationsNode = rootNode.path("_embedded").path("conversationResponseDTOes");
			List<EntityModel<ConversationResponseDTO>> entityModels = objectMapper.readValue(conversationsNode.toString(), new TypeReference<List<EntityModel<ConversationResponseDTO>>>() {});
			List<ConversationResponseDTO> conversations = entityModels.stream()
					.map(entityModel -> entityModel.getContent())
					.collect(Collectors.toList());
			
			//Assertions
			assertTrue(conversations.size() == 2);
			for(ConversationResponseDTO c: conversations) {
				boolean u1Found = false;
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				
				if(c.getConversation_id().equals(c1.getConversation_id())) {
					assertTrue(c.getNumUsers() == 4);
					for(User u: c.getUsers()) {
						if(u.getUsername().equals("u1") && u.getUser_id().equals(u1.getUser_id())) {
							u1Found = true;
						} else if(u.getUsername().equals("u2") && u.getUser_id().equals(u2.getUser_id())) {
							u2Found = true;
						} else if(u.getUsername().equals("u3") && u.getUser_id().equals(u3.getUser_id())) {
							u3Found = true;
						} else if(u.getUsername().equals("u4") && u.getUser_id().equals(u4.getUser_id())) {
							u4Found = true;
						}else {
							fail("Unexpected User Found");
						}
					}
					assertTrue(u1Found == true && u2Found == true && u3Found == true && u4Found == true);
				} else if(c.getConversation_id().equals(c2.getConversation_id())) {
					assertTrue(c.getNumUsers() == 2);
					for(User u: c.getUsers()) {
						if(u.getUsername().equals("u1") && u.getUser_id().equals(u1.getUser_id())) {
							u1Found = true;
						} else if(u.getUsername().equals("u2") && u.getUser_id().equals(u2.getUser_id())) {
							u2Found = true;
						}else {
							fail("Unexpected User Found");
						}
					}
					assertTrue(u1Found == true && u2Found == true);
				} else {
					fail("Unexpected conversation found");
				}
			}
		}
		
		@Test
		@Transactional
		void test_getConversationById() throws Exception {
			// Fetching User Converastions
			String responseJson = mockMvc
					.perform(get("/conversations/" + c1.getConversation_id()).header("Authorization", "Bearer " + u1_auth.getToken()))
					.andReturn().getResponse().getContentAsString();
			
			System.out.println("ResponseJson Conversation By ID: " + responseJson);
			
			//Extracting Coversations From Response
			JsonNode rootNode = objectMapper.readTree(responseJson);
			
			EntityModel<ConversationResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(), new TypeReference<EntityModel<ConversationResponseDTO>>() {});
			ConversationResponseDTO conversation = entityModel.getContent();
			
			assertTrue(conversation.getConversation_id().equals(c1.getConversation_id()));
			boolean user1 = false;
			boolean user2 = false;
			boolean user3 = false;
			boolean user4 = false;
			for(User u: conversation.getUsers()) {
				if(u.getUser_id().equals(u1.getUser_id()) && u.getUsername().equals("u1")) {
					user1 = true;
				} else if(u.getUser_id().equals(u2.getUser_id()) && u.getUsername().equals("u2")) {
					user2 = true;
				}else if(u.getUser_id().equals(u3.getUser_id()) && u.getUsername().equals("u3")) {
					user3 = true;
				}else if(u.getUser_id().equals(u4.getUser_id()) && u.getUsername().equals("u4")) {
					user4 = true;
				} else {
					fail("Unexpecte User");
				}
			}
			assertTrue(user1 == true && user2 == true && user3 == true && user4 == true);	
		}
		
		@Test
		@Transactional 
		void test_searchConversation_byDate() throws Exception {
	        // Create a Date object
	        Date date = new Date();
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        String formattedDate = sdf.format(date);
	        
			// Fetching User Converastions
			String responseJson = mockMvc
					.perform(get("/search/conversations?date=" + formattedDate).header("Authorization", "Bearer " + u1_auth.getToken()))
					.andReturn().getResponse().getContentAsString();
			
			//Extracting Coversations From Response
			JsonNode rootNode = objectMapper.readTree(responseJson);
			JsonNode conversationsNode = rootNode.path("_embedded").path("conversationResponseDTOes");
			List<EntityModel<ConversationResponseDTO>> entityModels = objectMapper.readValue(conversationsNode.toString(), new TypeReference<List<EntityModel<ConversationResponseDTO>>>() {});
			List<ConversationResponseDTO> conversations = entityModels.stream()
					.map(entityModel -> entityModel.getContent())
					.collect(Collectors.toList());
			
			//Assertions
			assertTrue(conversations.size() == 2);
			for(ConversationResponseDTO c: conversations) {
				boolean u1Found = false;
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				
				if(c.getConversation_id().equals(c1.getConversation_id())) {
					assertTrue(c.getNumUsers() == 4);
					for(User u: c.getUsers()) {
						if(u.getUsername().equals("u1") && u.getUser_id().equals(u1.getUser_id())){
							u1Found = true;
						} else if(u.getUsername().equals("u2") && u.getUser_id().equals(u2.getUser_id())) {
							u2Found = true;
						} else if(u.getUsername().equals("u3") && u.getUser_id().equals(u3.getUser_id())) {
							u3Found = true;
						} else if(u.getUsername().equals("u4") && u.getUser_id().equals(u4.getUser_id())){
							u4Found = true;
						}else {
							fail("Unexpected User Found");
						}
					}
					assertTrue(u1Found == true && u2Found == true && u3Found == true && u4Found == true);
				} else if(c.getConversation_id().equals(c2.getConversation_id())) {
					assertTrue(c.getNumUsers() == 2);
					for(User u: c.getUsers()) {
						if(u.getUsername().equals("u1") && u.getUser_id().equals(u1.getUser_id())) {
							u1Found = true;
						} else if(u.getUsername().equals("u2") && u.getUser_id().equals(u2.getUser_id())) {
							u2Found = true;
						}else {
							fail("Unexpected User Found");
						}
					}
					assertTrue(u1Found == true && u2Found == true);
				} else {
					fail("Unexpected conversation found");
				}
			}
		}
		
		@Test
		@Transactional
		void test_searchConversation_byUser() throws Exception {
			// Fetching User Converastions
			String responseJson = mockMvc
					.perform(get("/search/conversations/" + u3.getUser_id()).header("Authorization", "Bearer " + u3_auth.getToken()))
					.andReturn().getResponse().getContentAsString();
			
			
			//Extracting Coversations From Response
			JsonNode rootNode = objectMapper.readTree(responseJson);
			JsonNode conversationsNode = rootNode.path("_embedded").path("conversationResponseDTOes");
			List<EntityModel<ConversationResponseDTO>> entityModels = objectMapper.readValue(conversationsNode.toString(), new TypeReference<List<EntityModel<ConversationResponseDTO>>>() {});
			List<ConversationResponseDTO> conversations = entityModels.stream()
					.map(entityModel -> entityModel.getContent())
					.collect(Collectors.toList());
			
			assertTrue(conversations.size() == 2);
			for(ConversationResponseDTO c: conversations) {
				boolean u1Found = false;
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				
				if(c.getConversation_id().equals(c1.getConversation_id())){
					assertTrue(c.getNumUsers() == 4);
					for(User u: c.getUsers()) {
						if(u.getUsername().equals("u1") && u.getUser_id().equals(u1.getUser_id())) {
							u1Found = true;
						} else if(u.getUsername().equals("u2") && u.getUser_id().equals(u2.getUser_id())) {
							u2Found = true;
						} else if(u.getUsername().equals("u3") && u.getUser_id().equals(u3.getUser_id())) {
							u3Found = true;
						} else if(u.getUsername().equals("u4") && u.getUser_id().equals(u4.getUser_id())){
							u4Found = true;
						}else {
							fail("Unexpected User Found");
						}
					}
					assertTrue(u1Found == true && u2Found == true && u3Found == true && u4Found == true);
				} else if(c.getConversation_id().equals(c3.getConversation_id())) {
					assertTrue(c.getNumUsers() == 2);
					for(User u: c.getUsers()) {
						if(u.getUsername().equals("u3") && u.getUser_id().equals(u3.getUser_id())) {
							u3Found = true;
						} else if(u.getUsername().equals("u4") && u.getUser_id().equals(u4.getUser_id())) {
							u4Found = true;
						}else {
							fail("Unexpected User Found");
						}
					}
					assertTrue(u3Found == true && u4Found == true);
				} else {
					fail("Unexpected conversation found");
				}
			}
			
		}
		
		@Test
		@Transactional
		void test_createConversation() throws Exception {
			String createConversationReturnValueJson = mockMvc.perform(post("/conversations/" + u3.getUser_id())
					.header("Authorization", "Bearer " + u1_auth.getToken())).andReturn().getResponse().getContentAsString();
			JsonNode rootNode = objectMapper.readTree(createConversationReturnValueJson);
			EntityModel<ConversationResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(), new TypeReference<EntityModel<ConversationResponseDTO>>() {});
			ConversationResponseDTO conversation = entityModel.getContent();
			boolean user1Found = false;
			boolean user3Found = false;
			for(User u: conversation.getUsers()) {
				if(u.getUser_id().equals(u1.getUser_id()) && u.getUsername().equals("u1")) {
					user1Found = true;
				} else if(u.getUser_id().equals(u3.getUser_id()) && u.getUsername().equals("u3")) {
					user3Found = true;
				} else {
					fail("Unexpected user found");
				}
			}
			
			assertTrue(user1Found == true && user3Found == true);
		}
		
		@Test
		@Transactional
		void test_updateConversation() throws Exception {
			Date date = new Date();
			Message message = Message.builder()
					.content("Hi, this is a test")
					.build();
			List<Message> messages = List.of(message);
			ConversationDTO conversationDTO = ConversationDTO.builder()
					.numUsers(100)
					.messages(messages)
					.build();
			String responseJson = mockMvc
					.perform(put("/conversation/" + c1.getConversation_id()).header("Authorization", "Bearer " + u1_auth.getToken())
							.content(new ObjectMapper().writeValueAsString(conversationDTO)).contentType(MediaType.APPLICATION_JSON))
					.andReturn().getResponse().getContentAsString();
			
			System.out.println("Conversation Update Response: "+ responseJson);
			
			JsonNode rootNode = objectMapper.readTree(responseJson);
			EntityModel<ConversationResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(), new TypeReference<EntityModel<ConversationResponseDTO>>() {});
			ConversationResponseDTO conversation = entityModel.getContent();
			assertTrue(conversation.getNumUsers() == 100);
			assertNotNull(conversation.getMessages());
		}
		
		@Test
		@Transactional
		void test_addUserToConversation() throws Exception {
			String addUserToConvoReturnValueJson = mockMvc.perform(put("/conversations/" + c3.getConversation_id() + "/" + u1.getUser_id())
					.header("Authorization", "Bearer " + u3_auth.getToken())).andReturn().getResponse().getContentAsString();
			
			JsonNode rootNode = objectMapper.readTree(addUserToConvoReturnValueJson);
			EntityModel<ConversationResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(), new TypeReference<EntityModel<ConversationResponseDTO>>() {});
			ConversationResponseDTO conversation = entityModel.getContent();
			
			assertNotNull(conversation);
			assertTrue(conversation.getNumUsers() == 3);
			boolean userAdded = false;
			for(User u: conversation.getUsers()) {
				System.out.println("User In Conversation : " + u.toString());
				if(u.getUser_id().equals(u1.getUser_id()) && u.getUsername().trim().equals(u1.getUsername())) {
					userAdded = true;
				}
			}
			assertTrue(userAdded == true);
		}
		
		@Test
		@Transactional
		void test_leaveConversation() throws Exception {
			String leaveConversationJson = mockMvc.perform(delete("/conversation/leave/" + c3.getConversation_id())
					.header("Authorization", "Bearer " + u3_auth.getToken())).andReturn().getResponse().getContentAsString();
			
			Conversation c1 = conversationRepository.findById(c3.getConversation_id()).orElseThrow(() -> new ConversationNotFoundException(c3.getConversation_id()));
			assertTrue(c1.getNumUsers() == 1);
			boolean leftConversation = true;
			
			for(User u: c1.getConversation_users()) {
				if(u.getUser_id().equals(u3.getUser_id()) || u.getUsername().equals("u3")) {
					leftConversation = false;
				}
			}
			
			assertTrue(leftConversation == true);
		}
		
		@Test
		@Transactional
		void test_deleteConversation() throws Exception {
			String leaveConversationJson = mockMvc.perform(delete("/conversations/" + c3.getConversation_id())
					.header("Authorization", "Bearer " + adminAuthResponse.getToken())).andReturn().getResponse().getContentAsString();
			
			
			System.out.println("Delete Conversation: " + leaveConversationJson);
			
			// Fetching Converastions
			String responseJson = mockMvc
					.perform(get("/conversations").header("Authorization", "Bearer " + adminAuthResponse.getToken()))
					.andReturn().getResponse().getContentAsString();
			

			//Extracting Coversations From Response
			JsonNode rootNode = objectMapper.readTree(responseJson);
			JsonNode conversationsNode = rootNode.path("_embedded").path("conversationResponseDTOes");
			List<EntityModel<ConversationResponseDTO>> entityModels = objectMapper.readValue(conversationsNode.toString(), new TypeReference<List<EntityModel<ConversationResponseDTO>>>() {});
			List<ConversationResponseDTO> conversations = entityModels.stream()
					.map(entityModel -> entityModel.getContent())
					.collect(Collectors.toList());	
			
			for(ConversationResponseDTO conversation: conversations) {
				assertTrue(conversation.getConversation_id() != c3.getConversation_id());
			}
		}
		
	}

