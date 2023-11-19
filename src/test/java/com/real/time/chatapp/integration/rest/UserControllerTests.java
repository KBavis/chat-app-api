package com.real.time.chatapp.integration.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Auth.LoadAdmin;
import com.real.time.chatapp.DTO.UserDTO;
import com.real.time.chatapp.DTO.UserResponseDTO;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class UserControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private UserRepository userRepository;
	
	private RestIntegrationTestHelper testHelper;
	
	private ObjectMapper objectMapper;
	
	private User u1;
	private User u2;
	private User u3;
	private User u4;
	private User adminUser;
	private AuthenticationResponse u1_auth;
	private AuthenticationResponse u2_auth;
	private AuthenticationResponse u3_auth;
	private AuthenticationResponse u4_auth;
	
	@BeforeEach
	@Transactional
	void setUp() throws Exception {
		// Initalize Test Helper
		testHelper = new RestIntegrationTestHelper(mockMvc, userRepository);
		objectMapper = new ObjectMapper();
		
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
		adminUser = userRepository.findByUserName("AdminUser").orElseThrow(() -> new UserNotFoundException("AdminUser"));

		// Set Up User Authentications
		u1_auth = testHelper.loginAndReturnToken("u1", "password");
		u2_auth = testHelper.loginAndReturnToken("u2", "password");
		u3_auth = testHelper.loginAndReturnToken("u3", "password");
		u4_auth = testHelper.loginAndReturnToken("u4", "password");
		
		//Update Users Names
		testHelper.updateUserFullName(u1.getUser_id(), u1_auth, "User", "One");
		testHelper.updateUserFullName(u2.getUser_id(), u2_auth, "User", "Two");
		testHelper.updateUserFullName(u3.getUser_id(), u3_auth, "User", "Three");
		testHelper.updateUserFullName(u4.getUser_id(), u4_auth, "User", "Four");
	}
	
	@Test
	@Transactional
	void test_getAllUsers() throws Exception {
		// Fetching All Messages
		String responseJson = mockMvc
				.perform(get("/users").header("Authorization", "Bearer " + u1_auth.getToken())).andReturn()
				.getResponse().getContentAsString();
		
		
		//Extracting Users From Response
		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode usersNode = rootNode.path("_embedded").path("userResponseDTOes");
		List<EntityModel<UserResponseDTO>> entityModels = objectMapper.readValue(
				usersNode.toString(), new TypeReference<List<EntityModel<UserResponseDTO>>>() {});
		List<UserResponseDTO> users = entityModels.stream()
				.map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());
		
		//5 Users Due to Admin User
		//TODO: Remove ability for Users to fetch Admin User 
		boolean u1Found = false;
		boolean u2Found = false;
		boolean u3Found = false;
		boolean u4Found = false;
		boolean adminFound = true;
		for(UserResponseDTO u: users) {
			if(u.getUser_id().equals(u1.getUser_id())) {
				u1Found = true;
				assertTrue(u.getUserName().equals("u1"));
				assertTrue(u.getRole() == Role.USER);
				assertTrue(u.getFirstName().equals("User"));
				assertTrue(u.getLastName().equals("One"));
				assertTrue(u.getList_conversations().isEmpty());
				assertTrue(u.getSentMessges() == null);
				assertTrue(u.getRecievedMessages().isEmpty());
				assertNotNull(u.getPassword());
			} else if(u.getUser_id().equals(u2.getUser_id())) {
				u2Found = true;
				assertTrue(u.getUserName().equals("u2"));
				assertTrue(u.getRole() == Role.USER);
				assertTrue(u.getFirstName().equals("User"));
				assertTrue(u.getLastName().equals("Two"));
				assertTrue(u.getList_conversations().isEmpty());
				assertTrue(u.getSentMessges() == null);
				assertTrue(u.getRecievedMessages().isEmpty());
				assertNotNull(u.getPassword());
			} else if(u.getUser_id().equals(u3.getUser_id())) {
				u3Found = true;
				assertTrue(u.getUserName().equals("u3"));
				assertTrue(u.getRole() == Role.USER);
				assertTrue(u.getFirstName().equals("User"));
				assertTrue(u.getLastName().equals("Three"));
				assertTrue(u.getList_conversations().isEmpty());
				assertTrue(u.getSentMessges() == null);
				assertTrue(u.getRecievedMessages().isEmpty());
				assertNotNull(u.getPassword());
				
			} else if(u.getUser_id().equals(u4.getUser_id())) {
				u4Found = true;
				assertTrue(u.getUserName().equals("u4"));
				assertTrue(u.getRole() == Role.USER);
				assertTrue(u.getFirstName().equals("User"));
				assertTrue(u.getLastName().equals("Four"));
				assertTrue(u.getList_conversations().isEmpty());
				assertTrue(u.getSentMessges() == null);
				assertTrue(u.getRecievedMessages().isEmpty());
				assertNotNull(u.getPassword());
				
			} else if(u.getUser_id().equals(adminUser.getUser_id())){
				adminFound = true;
				assertTrue(u.getUserName().equals("AdminUser"));
				assertTrue(u.getRole() == Role.ADMIN);
				assertTrue(u.getFirstName().equals("Admin"));
				assertTrue(u.getLastName().equals("User"));
				assertTrue(u.getList_conversations().size() == 0);
				assertTrue(u.getSentMessges().size() == 0);
				assertTrue(u.getRecievedMessages().size() == 0);
				assertNotNull(u.getPassword());
			} 
		}
		assertTrue(u1Found == true && u2Found == true && u3Found == true 
				&& u4Found == true);
	}
	
	@Test
	@Transactional
	void test_getUserById() throws Exception {
		//Fetching User By ID 
		String responseJson = mockMvc
				.perform(get("/users/" + u2.getUser_id()).header("Authorization", "Bearer " + u1_auth.getToken())).andReturn()
				.getResponse().getContentAsString();
		
		
		//Parsing Response 
		JsonNode rootNode = objectMapper.readTree(responseJson);
		EntityModel<UserResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(),
				new TypeReference<EntityModel<UserResponseDTO>>() {
				});
		UserResponseDTO user = entityModel.getContent();
		
		//Assertions
		assertTrue(user.getUser_id().equals(u2.getUser_id()));
		assertTrue(user.getFirstName().equals("User"));
		assertTrue(user.getLastName().equals("Two"));
		assertTrue(user.getList_conversations().size() == 0);
		assertTrue(user.getRecievedMessages().size() == 0);
		assertTrue(user.getSentMessges() == null);
		assertTrue(user.getRole() == Role.USER);
		assertTrue(user.getPassword() != null);
		assertTrue(user.getUserName().equals("u2"));
	}
	
	@Test
	@Transactional
	void test_searchUserByName() throws Exception { 
		String responseJson = mockMvc
				.perform(get("/search/users/name?name=" + u2.getFirstName() + " " + u2.getLastName()).header("Authorization", "Bearer " + u1_auth.getToken())).andReturn()
				.getResponse().getContentAsString();
		
		//Pasing Response 
		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode userNode = rootNode.path("_embedded").path("userResponseDTOes");
		List<EntityModel<UserResponseDTO>> entityModels = objectMapper.readValue(userNode.toString(), new TypeReference<List<EntityModel<UserResponseDTO>>>() {});
		List<UserResponseDTO> users = entityModels.stream()
				.map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());
		
		//Assertions
		assertTrue(users.size() == 1);
		boolean user2Found = false;
		for(UserResponseDTO user: users) {
			if(user.getUser_id().equals(u2.getUser_id())) {
				user2Found = true;
				assertTrue(user.getFirstName().equals("User"));
				assertTrue(user.getLastName().equals("Two"));
				assertTrue(user.getPassword() != null);
				assertTrue(user.getList_conversations().size() == 0);
				assertTrue(user.getRecievedMessages().size() == 0);
				assertTrue(user.getSentMessges() == null);
				assertTrue(user.getRole() == Role.USER);
				assertTrue(user.getUserName().equals("u2"));
			} else {
				fail("Unexpected User Found");
			}
		}
		assertTrue(user2Found == true);
		
	}
	
	@Test
	@Transactional
	void test_searchUsersByUsername() throws Exception {
		String responseJson = mockMvc
				.perform(get("/search/users/userName?userName=" + u2.getUsername()).header("Authorization", "Bearer " + u1_auth.getToken())).andReturn()
				.getResponse().getContentAsString();
		
		//Pasing Response 
		JsonNode rootNode = objectMapper.readTree(responseJson);
		JsonNode userNode = rootNode.path("_embedded").path("userResponseDTOes");
		List<EntityModel<UserResponseDTO>> entityModels = objectMapper.readValue(userNode.toString(), new TypeReference<List<EntityModel<UserResponseDTO>>>() {});
		List<UserResponseDTO> users = entityModels.stream()
				.map(entityModel -> entityModel.getContent())
				.collect(Collectors.toList());
		
		
		//Assertions
		assertTrue(users.size() == 1);
		boolean user2Found = false;
		for(UserResponseDTO user: users) {
			if(user.getUser_id().equals(u2.getUser_id())) {
				user2Found = true;
				assertTrue(user.getFirstName().equals("User"));
				assertTrue(user.getLastName().equals("Two"));
				assertTrue(user.getPassword() != null);
				assertTrue(user.getList_conversations().size() == 0);
				assertTrue(user.getRecievedMessages().size() == 0);
				assertTrue(user.getSentMessges() == null);
				assertTrue(user.getRole() == Role.USER);
				assertTrue(user.getUserName().equals("u2"));
			} else {
				fail("Unexpected User Found");
			}
		}
		assertTrue(user2Found == true);
	}
	
	@Test
	@Transactional
	void test_updateUser() throws Exception {
		String responseJson= mockMvc.perform(put("/users/" + u2.getUser_id())
				.header("Authorization", "Bearer " + u2_auth.getToken())
				.content(new ObjectMapper().writeValueAsString(
						UserDTO.builder().username("UpdatedUserName").firstName("Updated").lastName("Name").role(Role.ADMIN).build()))
				.contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getContentAsString();
		
		
		//Parsing Response 
		JsonNode rootNode = objectMapper.readTree(responseJson);
		EntityModel<UserResponseDTO> entityModel = objectMapper.readValue(rootNode.toString(),
				new TypeReference<EntityModel<UserResponseDTO>>() {
				});
		UserResponseDTO user = entityModel.getContent();
		
		//Assertions
		assertTrue(user.getUser_id().equals(u2.getUser_id()));
		assertTrue(user.getFirstName().equals("Updated"));
		assertTrue(user.getLastName().equals("Name"));
		assertTrue(user.getList_conversations().size() == 0);
		assertTrue(user.getRecievedMessages().size() == 0);
		assertTrue(user.getSentMessges() == null);
		assertTrue(user.getRole() == Role.ADMIN);
		assertTrue(user.getPassword() != null);
		assertTrue(user.getUserName().equals("UpdatedUserName"));
		
	}
	
	@Test
	@Transactional
	void test_deleteUser_asUser() throws Exception {
		//Act
		String responseJson = mockMvc.perform(delete("/users/" + u2.getUser_id()).header("Authorization", "Bearer " + u2_auth.getToken()))
				.andReturn().getResponse().getContentAsString();
		
		//Assert
		assertTrue(responseJson.isEmpty());
		String fetchUser = mockMvc.perform(get("/users/" + u2.getUser_id()).header("Authroization", "Bearer " + u1_auth.getToken())).andReturn()
				.getResponse().getContentAsString();
		assertTrue(fetchUser.isEmpty());
	}
	
	@Test
	@Transactional
	void test_deleteUser_asAdmin() throws Exception {
		// Set Up Admin Authentication
		Properties properties = new Properties();
		try (InputStream inputStream = LoadAdmin.class.getClassLoader().getResourceAsStream("application.properties")) {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String adminPassword = properties.getProperty("admin.password");
		AuthenticationResponse adminAuthResponse = testHelper.loginAndReturnToken("AdminUser", adminPassword);
		
		//Act
		String responseJson = mockMvc.perform(delete("/users/" + u2.getUser_id()).header("Authorization", "Bearer " + adminAuthResponse.getToken()))
				.andReturn().getResponse().getContentAsString();
		
		//Assert
		assertTrue(responseJson.isEmpty());
		String fetchUser = mockMvc.perform(get("/users/" + u2.getUser_id()).header("Authroization", "Bearer " + u1_auth.getToken())).andReturn()
				.getResponse().getContentAsString();
		assertTrue(fetchUser.isEmpty());
	}

}