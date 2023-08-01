package com.real.time.chatapp.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
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
import com.real.time.chatapp.DTO.UserDTO;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class UserControllerEndpointAccessTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;
	
	private AuthenticationTestHelper testHelper;
	
	private String adminPassword;
	
	@BeforeEach
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
	}
	
	
	//********************************************************************
	//----------ACCESS RETURNS FORBIDDEN/UNAUTHORIZED TEST CASES----------
	//********************************************************************
	
	@Test
	@Transactional
	void test_users_returnsForbidden() throws Exception {
		mockMvc.perform(get("/users")).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_userById_returnsForbidden() throws Exception {
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		mockMvc.perform(get("/users/" + user.getUser_id())).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_userByName_returnsForbidden() throws Exception {
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		mockMvc.perform(get("/search/users/name?name=" + user.getFirstName())).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_userByUserName_returnsForbidden() throws Exception {
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		mockMvc.perform(get("/search/users/userName?userName=" + user.getUsername())).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_updateUser_returnsForbidden() throws Exception {
		UserDTO userDTO = new UserDTO();
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		userDTO.setFirstName("test");
		userDTO.setLastName("test");
		userDTO.setPassword("test");
		userDTO.setUsername("test");
		mockMvc.perform(put("/users/" + user.getUser_id())).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_updateUser_returnsUnauthorized() throws Exception {
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		UserDTO userDTO = new UserDTO();
		userDTO.setFirstName("test");
		userDTO.setLastName("test");
		userDTO.setPassword("test");
		userDTO.setUsername("test");
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser2", "password");
		mockMvc.perform(put("/users/" + user.getUser_id()).header("Authorization", "Bearer " + authResponse.getToken()).content(new ObjectMapper().writeValueAsString(userDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}
	
	@Test
	@Transactional
	void test_deleteUser_returnsForbidden() throws Exception {
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		mockMvc.perform(delete("/users/" + user.getUser_id())).andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	void test_deleteUser_returnsUnauthorized() throws Exception {
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser2", "password");
		mockMvc.perform(delete("/users/" + user.getUser_id()).header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isUnauthorized());
	}
	
	//********************************************************
	//----------ACCESS RETURNS OK/CREATED TEST CASES----------
	//********************************************************
	
	@Test
	@Transactional
	void test_users_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(get("/users").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_usersById_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		User user = userRepository.findByUserName("testUser2")
				.orElseThrow(() -> new UserNotFoundException("testUser1"));
		mockMvc.perform(get("/users/" + user.getUser_id()).header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_searchByName_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(get("/search/users/name?name=test test").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_searchByUserName_returnsOk() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(get("/search/users/userName?userName=testUser1").header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_updateUser_returnsOk() throws Exception {
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		UserDTO userDTO = new UserDTO();
		userDTO.setFirstName("test");
		userDTO.setLastName("test");
		userDTO.setPassword("test");
		userDTO.setUsername("test");
		userDTO.setRole(Role.USER);
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(put("/users/" + user.getUser_id()).header("Authorization", "Bearer " + authResponse.getToken()).content(new ObjectMapper().writeValueAsString(userDTO)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
	}
	
	@Test
	@Transactional
	void test_deleteUser_returnsNoContent() throws Exception {
		User user = userRepository.findByUserName("testUser1").orElseThrow(() -> new UserNotFoundException("testUser1"));
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser1", "password");
		mockMvc.perform(delete("/users/" + user.getUser_id()).header("Authorization", "Bearer " + authResponse.getToken())).andExpect(status().isNoContent());	
	}
	
}
