package com.real.time.chatapp.unitteests.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

import com.real.time.chatapp.ControllerServices.UserService;
import com.real.time.chatapp.DTO.UserDTO;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

@SpringBootTest
@ContextConfiguration
@WithMockUser(username = "TestUser1")
public class UserServiceTests {

	@Mock
	UserRepository userRepository;

	@Mock
	MessageRepository messageRepository;

	@InjectMocks
	UserService userService;
	
	@Test
	void test_getAllUsers_isSuccesful() {
		// Mock User Repository Find All()
		User u1 = User.builder().firstName("TestUser1").lastName("User1").userName("TestUser1").password("Test")
				.build();
		User u2 = User.builder().firstName("TestUser2").lastName("User2").userName("TestUser2").password("Test")
				.build();
		List<User> users = List.of(u1, u2);
		when(userRepository.findAll()).thenReturn(users);

		// Act
		List<User> allUsers = userService.getAllUsers();

		// Assert
		assertNotNull(allUsers);
		assertEquals(allUsers.size(), 2);
		assertEquals(allUsers.get(0), u1);
		assertEquals(allUsers.get(1), u2);
	}

	@Test
	void test_getUsersById_isSuccesful() {
		// Mock
		User u1 = User.builder().firstName("TestUser1").lastName("User1").userName("TestUser1").password("Test")
				.build();
		when(userRepository.findById(1L)).thenReturn(Optional.of(u1));

		// Act
		User user = userService.getUserById(1L);

		// Assert
		assertNotNull(user);
		assertEquals(u1, user);
	}

	@Test
	void test_searchUsersByName_isSuccesful() {
		// Mock
		User u1 = User.builder().firstName("TestUser1").lastName("User1").userName("TestUser1").password("Test")
				.build();
		List<User> users = List.of(u1);
		when(userRepository.searchUsersByName("TestUser1", "User1")).thenReturn(users);

		// Act
		List<User> foundUsers = userService.searchUserByName("TestUser1 User1");

		// Assert
		assertNotNull(foundUsers);
		assertEquals(1, foundUsers.size());
		assertEquals(foundUsers.get(0), u1);
	}

	@Test
	void test_searchUserByUsername_isSuccesful() {
		// Mock
		User u1 = User.builder().firstName("TestUser1").lastName("User1").userName("TestUser1").password("Test")
				.build();
		List<User> users = List.of(u1);
		when(userRepository.searchUsersByUserName("TestUser1")).thenReturn(users);

		// Act
		List<User> foundUsers = userService.searchUserByUsername("TestUser1");

		// Assert
		assertNotNull(foundUsers);
		assertEquals(1, foundUsers.size());
		assertEquals(foundUsers.get(0), u1);
	}

    @Test
    public void test_updateUser_isSuccessful() {
        // Arrange
        User existingUser = User.builder()
                .user_id(1L)
                .firstName("TestUser1")
                .lastName("User1")
                .userName("TestUser1")
                .password("Test")
                .role(Role.USER)
                .build();

        UserDTO userDTO = UserDTO.builder()
                .username("NewTestUser1")
                .lastName("NewUser1")
                .firstName("NewTestUser1")
                .password("NewTest")
                .role(Role.USER)
                .build();

        // Stub the userRepository findById method
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        
        // Mock the userService
        UserService userServiceSpy = spy(userService);
        doReturn(existingUser).when(userServiceSpy).updateUser(eq(1L), any(UserDTO.class));

        // Act
        User updatedUser = userServiceSpy.updateUser(1L, userDTO);

        // Assert
        assertNotNull(updatedUser);

        // Verify that userRepository.findById was called once with the argument 1L
        verify(userRepository, times(1)).findById(1L);

        // Verify that userRepository.save was called once with any User object
        verify(userRepository, times(1)).save(any(User.class));
    }

	@Test
	void test_updateUser_isUnauthorized() {
		// Mock
	}

	@Test
	void test_deleteUser_isSuccesful() {

	}

	@Test
	void test_deleteUser_isUnauthorized() {

	}

}
