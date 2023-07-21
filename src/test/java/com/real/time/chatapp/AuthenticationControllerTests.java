package com.real.time.chatapp;


import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.time.chatapp.Auth.AuthenticationRequest;
import com.real.time.chatapp.Auth.RegisterRequest;
import com.real.time.chatapp.Config.JwtService;
import com.real.time.chatapp.ControllerServices.AuthenticationService;
import com.real.time.chatapp.Controllers.AuthenticationController;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.CoreMatchers;

public class AuthenticationControllerTests {
	
	private MockMvc mockMvc;
	private ObjectMapper mapper = new ObjectMapper();
	private User user;
	private UserRepository userRepository;
	private  MessageRepository messageRepository;
	private JwtService jwtService = new JwtService();
	private PasswordEncoder passwordEncoder;
	private AuthenticationManager authenticationManager;
	private AuthenticationService authenticationService;
	private AuthenticationRequest authenticationRequest;
	private RegisterRequest registerRequest;
	

	
	@BeforeEach
	void setup() {
		 userRepository = mock(UserRepository.class);
		 passwordEncoder = new BCryptPasswordEncoder();
		 authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtService, authenticationManager, messageRepository);
		 mockMvc = MockMvcBuilders
				 .standaloneSetup(new AuthenticationController(authenticationService))
				 .build();
		 
		 user = User.builder().build();
		
		 registerRequest = RegisterRequest.builder()
				 .username("testUser")
				 .password("testPasssword")
				 .firstname("Test")
				 .lastname("User")
				 .build();
		 
	}
	
	@Test
	void test_register_returnsOk() throws Exception {
		mockMvc.perform(post("/auth/register")
				.content(mapper.writeValueAsString(registerRequest))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
	
	@Test
	void test_register_returnsJwt() throws Exception{
		String returnedJwtToken = mockMvc.perform(post("/auth/register")
				.content(mapper.writeValueAsString(registerRequest))
				.contentType(MediaType.APPLICATION_JSON))
				.andReturn()
				.getResponse()
				.getContentAsString();
		
//		assertThat(returnedJwtToken.length(), CoreMatchers.is(256));
		Assertions.assertTrue(!returnedJwtToken.isEmpty(), "JWT Token should be returned");
	}
}
