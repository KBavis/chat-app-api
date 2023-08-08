package com.real.time.chatapp.integration.auth;

import static org.hamcrest.CoreMatchers.notNullValue;
//import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UsernameTakenException;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
public class AuthenticationControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository repository;

	private AuthenticationTestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper = new AuthenticationTestHelper(mockMvc, repository);
	}

	/**
	 * Test Register Correctly Adds user
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_register_addsUser() throws Exception {
		testHelper.signUp("testUser", "test");

		Optional<User> optionalUser = repository.findByUserName("testUser");
		assertTrue(optionalUser.isPresent(), "User should be present in the database");
		User user = optionalUser.get();

		assertEquals("testUser", user.getUsername());
		assertTrue(passwordEncoder.matches("test", user.getPassword()), "Passwords should match");
	}

	/**
	 * Test Register Returns OK Status
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_register_isSuccesful() throws Exception {
		testHelper.signUp("testUser", "test").andExpect(status().isOk());
	}

	/**
	 * Test Registering a User with Same Username Returns UsernameTakenException
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_register_returnsUsernameTakenException() throws Exception {
		testHelper.signUp("testUser", "test");
		try {
			testHelper.signUp("testUser", "test");
		} catch (ServletException e) {
			assertTrue((e.getCause()) instanceof UsernameTakenException);
		}
	}

	/**
	 * Test Register Returns JWT Token
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_register_returnsJWT() throws Exception {
		testHelper.signUp("testUser", "test").andExpect(jsonPath("$.token").value(notNullValue()));
	}

	/**
	 * Test Authenticate Returns OK Status
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_authenticate_isSuccesful() throws Exception {
		testHelper.signUp("testUser", "test");

		testHelper.login("testUser", "test").andExpect(status().isOk());
	}

	/**
	 * Test Authenticate Returns 403
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_authenticate_wrongPassword_returns403() throws Exception {
		testHelper.signUp("testUser", "test");

		testHelper.login("testUser", "wrongPassword").andExpect(status().isForbidden());
	}

	/**
	 * Test Authenticate Returns 403
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_authenticate_wrongUser_returns403() throws Exception {
		testHelper.signUp("testUser", "test");

		testHelper.login("wrongUser", "test").andExpect(status().isForbidden());
	}
	
	/**
	 * Test Authenticate Returns JWT
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_authenticate_returnsJWT() throws Exception {
		testHelper.signUp("testUser", "test");
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("testUser", "test");
		assertNotNull(authResponse.getToken(), "Token should not be null");
	}

}
