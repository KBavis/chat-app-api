package com.real.time.chatapp.integration.auth;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

/**
 * @author Kellen Bavis
 * 	Endpoint Access Test Cases 
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class AuthenticationControllerEndpointAccessTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	private AuthenticationTestHelper testHelper;
	
	@Autowired
	private UserRepository repository;
	
	@BeforeEach
	void setUp() {
		testHelper = new AuthenticationTestHelper(mockMvc,repository);
	}
	
	/**
	 * Ensures the Register Endpoint is Accessible 
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_register_isAccessible() throws Exception {
		testHelper.signUp("test", "test")
		.andExpect(status().isOk());
	}
	
	@Test
	@Transactional
	void test_login_isAccessible() throws Exception {
		testHelper.signUp("test", "test");
		testHelper.login("test", "test")
		.andExpect(status().isOk());
	}
	
}
