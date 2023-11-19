package com.real.time.chatapp.integration.rest;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.LoadAdmin;
import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.DTO.MessageResponseDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class MessageControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private UserRepository userRepository;

	private RestIntegrationTestHelper testHelper;

	private ObjectMapper objectMapper;

	private String adminPassword;

	private AuthenticationResponse adminAuthResponse;

	private User u1;
	private User u2;
	private User u3;
	private User u4;
	private AuthenticationResponse u1_auth;
	private AuthenticationResponse u2_auth;
	private AuthenticationResponse u3_auth;
	private AuthenticationResponse u4_auth;
	private Conversation c1;
	private Conversation c2;
	private Conversation c3;
	private Message m1;
	private Message m2;
	private Message m3;

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

		// Send Mock Messages
		m1 = testHelper.sendMessage(c1.getConversation_id(), u1_auth, "Hi u2, u3, and u4. It's u1!");
		m2 = testHelper.sendMessage(c2.getConversation_id(), u2_auth, "Hi u1, it's u2!");
		m3 = testHelper.sendMessage(c3.getConversation_id(), u3_auth, "Hi u4, it's u3!");
	}

	@Test
	@Transactional
	void test_getAllMessages() throws Exception {
		// Fetching All Messages
		String responseJson = mockMvc
				.perform(get("/messages").header("Authorization", "Bearer " + adminAuthResponse.getToken())).andReturn()
				.getResponse().getContentAsString();

		// Extracting Messages From Response
		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode messagesNode = rootNode.path("_embedded").path("messageResponseDTOes");
		List<EntityModel<MessageResponseDTO>> entityModels = objectMapper.readValue(messagesNode.toString(),
				new TypeReference<List<EntityModel<MessageResponseDTO>>>() {
				});

		List<MessageResponseDTO> messages = entityModels.stream().map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());

		// Assertions
		for (MessageResponseDTO m : messages) {
			if (m.getMessage_id().equals(m1.getMessage_id())) {
				assertTrue(m.getConversation().getConversation_id().equals(c1.getConversation_id()));
				assertTrue(m.getContent().equals("Hi u2, u3, and u4. It's u1!"));
				assertTrue(m.getSender().getUser_id().equals(u1.getUser_id()));
				// Ensure Message Has Proper Recipients
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u2.getUser_id())) {
						u2Found = true;
					} else if (u.getUser_id().equals(u3.getUser_id())) {
						u3Found = true;
					} else if (u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u2Found == true && u3Found == true && u4Found == true);
			} else if (m.getMessage_id().equals(m2.getMessage_id())) {
				assertTrue(m.getConversation().getConversation_id().equals(c2.getConversation_id()));
				assertTrue(m.getContent().equals("Hi u1, it's u2!"));
				assertTrue(m.getSender().getUser_id().equals(u2.getUser_id()));
				// Ensure Message Has Proper Recipients
				boolean u1Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u1.getUser_id())) {
						u1Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u1Found == true);

			} else if (m.getMessage_id().equals(m3.getMessage_id())) {
				assertTrue(m.getConversation().getConversation_id().equals(c3.getConversation_id()));
				assertTrue(m.getContent().equals("Hi u4, it's u3!"));
				assertTrue(m.getSender().getUser_id().equals(u3.getUser_id()));
				// Ensure Message Has Proper Recipients
				boolean u4Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u4Found == true);

			} 
		}
	}

	@Test
	@Transactional
	void test_getUserMessages() throws Exception {
		// Fetching User Messages
		String responseJson = mockMvc
				.perform(get("/userMessages").header("Authorization", "Bearer " + u2_auth.getToken())).andReturn()
				.getResponse().getContentAsString();

		// Extracting User Messgaes From Response
		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode messagesNode = rootNode.path("_embedded").path("messageResponseDTOes");
		List<EntityModel<MessageResponseDTO>> entityModels = objectMapper.readValue(messagesNode.toString(),
				new TypeReference<List<EntityModel<MessageResponseDTO>>>() {
				});
		List<MessageResponseDTO> messages = entityModels.stream().map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());

		// Assertions
		assertTrue(messages.size() == 1);
		for (MessageResponseDTO m : messages) {
			if (m.getMessage_id().equals(m1.getMessage_id())) {
				assertTrue(m.getContent().equals("Hi u2, u3, and u4. It's u1!"));
				assertTrue(m.getConversation().getConversation_id().equals(c1.getConversation_id()));
				assertTrue(m.getSender().getUser_id().equals(u1.getUser_id()));
				// Ensure Message Has Proper Recipients
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u2.getUser_id())) {
						u2Found = true;
					} else if (u.getUser_id().equals(u3.getUser_id())) {
						u3Found = true;
					} else if (u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u2Found == true && u3Found == true && u4Found == true);

			} else {
				fail("Unexpected message found");
			}
		}
	}

	@Test
	@Transactional
	void test_getMessage_byID() throws Exception {
		// Fetching Specific Message
		String responseJson = mockMvc
				.perform(get("/messages/" + m2.getMessage_id()).header("Authorization", "Bearer " + u1_auth.getToken()))
				.andReturn().getResponse().getContentAsString();

		// Extracting User Messgaes From Response
		JsonNode rootNode = objectMapper.readTree(responseJson);
		EntityModel<MessageResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(),
				new TypeReference<EntityModel<MessageResponseDTO>>() {
				});
		MessageResponseDTO message = entityModel.getContent();

		// Assertions
		assertTrue(message.getMessage_id().equals(m2.getMessage_id()));
		assertTrue(message.getContent().equals("Hi u1, it's u2!"));
		assertTrue(message.getSender().getUser_id().equals(u2.getUser_id()));
		Set<User> recipients = message.getRecipients();
		assertTrue(recipients.size() == 1);
		assertTrue(new ArrayList<>(recipients).get(0).getUser_id().equals(u1.getUser_id()));
	}

	@Test
	@Transactional
	void test_getMessages_byConversation() throws Exception {
		// Fetching Messages From Conversaiton
		String responseJson = mockMvc.perform(get("/messages/conversations/" + c1.getConversation_id())
				.header("Authorization", "Bearer " + u2_auth.getToken())).andReturn().getResponse()
				.getContentAsString();

		System.out.println("ResponseJSOn MessagesByConversation  " + responseJson);

		// Extract Conversation Mesages
		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode messageNode = rootNode.path("_embedded").path("messageResponseDTOes");
		List<EntityModel<MessageResponseDTO>> entityModels = objectMapper.readValue(messageNode.toString(),
				new TypeReference<List<EntityModel<MessageResponseDTO>>>() {
				});

		List<MessageResponseDTO> messages = entityModels.stream().map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());

		assertTrue(messages.size() == 1);
		for (MessageResponseDTO m : messages) {
			if (m.getMessage_id().equals(m1.getMessage_id())) {
				assertTrue(m.getContent().equals("Hi u2, u3, and u4. It's u1!"));
				assertTrue(m.getConversation().getConversation_id().equals(c1.getConversation_id()));
				assertTrue(m.getSender().getUser_id().equals(u1.getUser_id()));
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u2.getUser_id())) {
						u2Found = true;
					} else if (u.getUser_id().equals(u3.getUser_id())) {
						u3Found = true;
					} else if (u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u2Found == true && u3Found == true && u4Found == true);
			} else {
				fail("Unexpected message found");
			}
		}
	}

	@Test
	@Transactional
	void test_searchMessagesByContent() throws Exception {
		String responseJson = mockMvc.perform(
				get("/search/messages/content?content=Hi").header("Authorization", "Bearer " + u1_auth.getToken()))
				.andReturn().getResponse().getContentAsString();

		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode messageNode = rootNode.path("_embedded").path("messageResponseDTOes");
		List<EntityModel<MessageResponseDTO>> entityModels = objectMapper.readValue(messageNode.toString(),
				new TypeReference<List<EntityModel<MessageResponseDTO>>>() {
				});

		List<MessageResponseDTO> messages = entityModels.stream().map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());

		assertTrue(messages.size() == 2);
		for (MessageResponseDTO m : messages) {
			if (m.getMessage_id().equals(m1.getMessage_id())) {
				assertTrue(m.getContent().equals("Hi u2, u3, and u4. It's u1!"));
				assertTrue(m.getConversation().getConversation_id().equals(c1.getConversation_id()));
				assertTrue(m.getSender().getUser_id().equals(u1.getUser_id()));
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u2.getUser_id())) {
						u2Found = true;
					} else if (u.getUser_id().equals(u3.getUser_id())) {
						u3Found = true;
					} else if (u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u2Found == true && u3Found == true && u4Found == true);
			} else if (m.getMessage_id().equals(m2.getMessage_id())) {
				assertTrue(m.getConversation().getConversation_id().equals(c2.getConversation_id()));
				assertTrue(m.getContent().equals("Hi u1, it's u2!"));
				assertTrue(m.getSender().getUser_id().equals(u2.getUser_id()));
				// Ensure Message Has Proper Recipients
				boolean u1Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u1.getUser_id())) {
						u1Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u1Found == true);
			} else {
				fail("Unexpected message not found");
			}
		}
	}

	@Test
	@Transactional
	void test_getMessagesByDate() throws Exception {
		// Create a Date object
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = sdf.format(date);

		// Search For this Message
		String responseJson = mockMvc.perform(
				get("/search/messages/date?date=2023-07-06").header("Authorization", "Bearer " + u1_auth.getToken()))
				.andReturn().getResponse().getContentAsString();

		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode messageNode = rootNode.path("_embedded").path("messageResponseDTOes");
		List<EntityModel<MessageResponseDTO>> entityModels = objectMapper.readValue(messageNode.toString(),
				new TypeReference<List<EntityModel<MessageResponseDTO>>>() {
				});

		List<MessageResponseDTO> messages = entityModels.stream().map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());

		for (MessageResponseDTO m : messages) {
			if (m.getMessage_id().equals(m1.getMessage_id())) {
				assertTrue(m.getContent().equals("Hi u2, u3, and u4. It's u1!"));
				assertTrue(m.getConversation().getConversation_id().equals(c1.getConversation_id()));
				assertTrue(m.getSender().getUser_id().equals(u1.getUser_id()));
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u2.getUser_id())) {
						u2Found = true;
					} else if (u.getUser_id().equals(u3.getUser_id())) {
						u3Found = true;
					} else if (u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u2Found == true && u3Found == true && u4Found == true);
			} else if (m.getMessage_id().equals(m2.getMessage_id())) {
				assertTrue(m.getConversation().getConversation_id().equals(c2.getConversation_id()));
				assertTrue(m.getContent().equals("Hi u1, it's u2!"));
				assertTrue(m.getSender().getUser_id().equals(u2.getUser_id()));
				// Ensure Message Has Proper Recipients
				boolean u1Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u1.getUser_id())) {
						u1Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u1Found == true);
			} else {
				System.out.println("MessageID: " + m.getMessage_id());
				fail("Unexpected message not found");
			}
		}
	}

	@Test
	@Transactional
	void test_getMessagesByRead() throws Exception {
		// Search For this Message
		String responseJson = mockMvc
				.perform(get("/search/messages/read").header("Authorization", "Bearer " + u1_auth.getToken()))
				.andReturn().getResponse().getContentAsString();

		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode messageNode = rootNode.path("_embedded").path("messageResponseDTOes");
		List<EntityModel<MessageResponseDTO>> entityModels = objectMapper.readValue(messageNode.toString(),
				new TypeReference<List<EntityModel<MessageResponseDTO>>>() {
				});

		List<MessageResponseDTO> messages = entityModels.stream().map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());

		for (MessageResponseDTO m : messages) {
			if (m.getMessage_id().equals(m1.getMessage_id())) {
				assertTrue(m.getContent().equals("Hi u2, u3, and u4. It's u1!"));
				assertTrue(m.getConversation().getConversation_id().equals(c1.getConversation_id()));
				assertTrue(m.getSender().getUser_id().equals(u1.getUser_id()));
				boolean u2Found = false;
				boolean u3Found = false;
				boolean u4Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u2.getUser_id())) {
						u2Found = true;
					} else if (u.getUser_id().equals(u3.getUser_id())) {
						u3Found = true;
					} else if (u.getUser_id().equals(u4.getUser_id())) {
						u4Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u2Found == true && u3Found == true && u4Found == true);
			} else if (m.getMessage_id().equals(m2.getMessage_id())) {
				assertTrue(m.getConversation().getConversation_id().equals(c2.getConversation_id()));
				assertTrue(m.getContent().equals("Hi u1, it's u2!"));
				assertTrue(m.getSender().getUser_id().equals(u2.getUser_id()));
				// Ensure Message Has Proper Recipients
				boolean u1Found = false;
				for (User u : m.getRecipients()) {
					if (u.getUser_id().equals(u1.getUser_id())) {
						u1Found = true;
					} else {
						fail("Unexpected recipient found");
					}
				}
				assertTrue(u1Found == true);
			} else {
				fail("Unexpected message not found");
			}
		}
	}

	@Test
	@Transactional
	void test_sendMessage() throws Exception {
		String sendMessageReturnValueJson = mockMvc.perform(
				post("/messages/" + c1.getConversation_id()).header("Authorization", "Bearer " + u2_auth.getToken())
						.content(new ObjectMapper()
								.writeValueAsString(MessageDTO.builder().content("Hi guys! It's u2!").build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		// Extract Message Response
		JsonNode rootNode = objectMapper.readTree(sendMessageReturnValueJson);
		EntityModel<MessageResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(),
				new TypeReference<EntityModel<MessageResponseDTO>>() {
				});
		MessageResponseDTO message = entityModel.getContent();
		assertTrue(message.getContent().equals("Hi guys! It's u2!"));
		assertTrue(message.getSender().getUser_id().equals(u2.getUser_id()));
		assertTrue(message.getRecipients().size() == 3);
		boolean u1_Found = false;
		boolean u4_Found = false;
		boolean u3_Found = false;
		for (User u : message.getRecipients()) {
			if (u.getUser_id().equals(u1.getUser_id())) {
				u1_Found = true;
			} else if (u.getUser_id().equals(u4.getUser_id())) {
				u4_Found = true;
			} else if (u.getUser_id().equals(u3.getUser_id())) {
				u3_Found = true;
			} else {
				fail("Unexpected user found");
			}
		}
		assertTrue(u1_Found == true && u4_Found == true && u3_Found == true);
	}

	@Test
	@Transactional
	void test_updateMessage() throws Exception {
		// Create Update MessageDTO
		MessageDTO msgDTO = MessageDTO.builder().content("Updated message!").build();
		// Search For this Message
		String responseJson = mockMvc
				.perform(put("/messages/" + m1.getMessage_id()).header("Authorization", "Bearer " + u1_auth.getToken())
						.content(new ObjectMapper().writeValueAsString(msgDTO)).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse().getContentAsString();

		// Extract Message Response
		JsonNode rootNode = objectMapper.readTree(responseJson);
		EntityModel<MessageResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(),
				new TypeReference<EntityModel<MessageResponseDTO>>() {
				});
		MessageResponseDTO message = entityModel.getContent();

		// Assert
		assertTrue(message.getContent().equals("Updated message!"));
	}
	
	@Test
	@Transactional
	void test_deleteMessage() throws Exception {
		mockMvc.perform(delete("/messages/" + m1.getMessage_id()).header("Authorization", "Bearer " + u1_auth.getToken()));
		
		//Attempt to Fetch Deleted Message
		String responseJson = mockMvc
				.perform(get("/messages/" + m1.getMessage_id()).header("Authorization", "Bearer " + u1_auth.getToken()))
				.andReturn().getResponse().getContentAsString();
		
		assertTrue(responseJson.equals("Message not found: " + m1.getMessage_id()));
	}
}
