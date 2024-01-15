package com.real.time.chatapp.unitteests.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.real.time.chatapp.ControllerServices.UserService;
import com.real.time.chatapp.DTO.UserDTO;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
public class UserServiceTests {

	@Mock
	UserRepository userRepository;

	@Mock
	MessageRepository messageRepository;

	@InjectMocks
	UserService userService;
	
//	@Test
//	void test_getAllUsers_isSuccesful() {
//		// Mock User Repository Find All()
//		User u1 = User.builder().firstName("TestUser1").lastName("User1").userName("TestUser1").password("Test")
//				.build();
//		User u2 = User.builder().firstName("TestUser2").lastName("User2").userName("TestUser2").password("Test")
//				.build();
//		List<User> users = List.of(u1, u2);
//		when(userRepository.findAll()).thenReturn(users);
//
//		// Act
//		List<User> allUsers = userService.getAllUsers();
//
//		// Assert
//		assertNotNull(allUsers);
//		assertEquals(allUsers.size(), 2);
//		assertEquals(allUsers.get(0), u1);
//		assertEquals(allUsers.get(1), u2);
//		
//		//Ensure Stubbed Methods Are Called
//		verify(userRepository, times(1)).findAll();
//	}

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
		
		//Ensure Stubbed Method Is Called
		verify(userRepository, times(1)).findById(1L);
	}
	
	@Test
	void test_getUsersById_returnsUserNotFoundException() {
		//Mock
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		
		//Act
		UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
			userService.getUserById(1L);
		});
		
		//Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "Could not find user " + 1L);
		
		//Ensure Stubbed Method Is Called
		verify(userRepository, times(1)).findById(1L);
		
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
		
		//Ensure Stubbed Method Is Called
		verify(userRepository, times(1)).searchUsersByName("TestUser1", "User1");
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
		
		//Ensure Stubbed Methods Are Called
		verify(userRepository, times(1)).searchUsersByUserName("TestUser1");
	}

    @Test
    @Transactional
    @WithMockUser(username = "TestUser1")
    public void test_updateUser_asUser_isSuccessful() {
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUserName("TestUser1")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        
        //Act
        User updatedUser = userService.updateUser(1L, userDTO);
        
        //Assert
        assertNotNull(updatedUser);
        assertEquals(updatedUser.getFirstName(), userDTO.getFirstName());
        assertEquals(updatedUser.getUsername(), userDTO.getUsername());
        assertEquals(updatedUser.getLastName(), userDTO.getLastName());
        assertEquals(updatedUser.getPassword(), userDTO.getPassword());
        assertEquals(updatedUser.getRole(), userDTO.getRole());
        
        //Ensure Stubbed Methods Are Called
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findByUserName("TestUser1");
    }
    
    @Test
    @Transactional
    @WithMockUser(username = "AdminUser")
    public void test_updateUser_asAdmin_isSuccessful() {
        // Arrange
        User adminUser = User.builder()
                .user_id(1L)
                .firstName("Admin")
                .lastName("User")
                .userName("AdminUser")
                .password("Test")
                .role(Role.ADMIN)
                .build();
        
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

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUserName("AdminUser")).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        
        //Act
        User updatedUser = userService.updateUser(1L, userDTO);
        
        //Assert
        assertNotNull(updatedUser);
        assertEquals(updatedUser.getFirstName(), userDTO.getFirstName());
        assertEquals(updatedUser.getUsername(), userDTO.getUsername());
        assertEquals(updatedUser.getLastName(), userDTO.getLastName());
        assertEquals(updatedUser.getPassword(), userDTO.getPassword());
        assertEquals(updatedUser.getRole(), userDTO.getRole());
        
        //Ensure Stubbed Methods Are Called
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findByUserName("AdminUser");
    }
    
	@Test
	@WithMockUser(username = "TestUser2")
	@Transactional
	void test_updateUser_isUnauthorized() {
		// Mock
        User existingUser = User.builder()
                .user_id(1L)
                .firstName("TestUser1")
                .lastName("User1")
                .userName("TestUser1")
                .password("Test")
                .role(Role.USER)
                .build();
        
        User authUser = User.builder()
                .user_id(1L)
                .firstName("TestUser2")
                .lastName("User2")
                .userName("TestUser2")
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
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUserName("TestUser2")).thenReturn(Optional.of(authUser));
        
        //Act
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
        	userService.updateUser(1L, userDTO);
        });
        
        //Assert
        assertNotNull(exception);
        assertEquals(exception.getLocalizedMessage(), "TestUser1 is unauthorized to perform this action.");
        
        //Ensure Stubbed Method Is Called
        verify(userRepository, times(1)).findById(1L); 
        verify(userRepository, times(1)).findByUserName("TestUser2");
	}
	
	@Test
	@Transactional
	void test_updateUser_returnsUserNotFoundException() {
        UserDTO userDTO = UserDTO.builder()
                .username("NewTestUser1")
                .lastName("NewUser1")
                .firstName("NewTestUser1")
                .password("NewTest")
                .role(Role.USER)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
        	userService.updateUser(1L, userDTO);
        });
        
        assertNotNull(exception);
        assertEquals(exception.getLocalizedMessage(), "Could not find user " + 1L);
        
        //Verify The Stubbed Method Is Called
        verify(userRepository, times(1)).findById(1L);
	}
	

	@Test
	@WithMockUser("TestUser1")
	@Transactional
	void test_deleteUser_asUser_isSuccesful() {
		// Mock
        User existingUser = User.builder()
                .user_id(1L)
                .firstName("TestUser1")
                .lastName("User1")
                .userName("TestUser1")
                .password("Test")
                .role(Role.USER)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUserName("TestUser1")).thenReturn(Optional.of(existingUser));
        
        //Act
        userService.deleteUser(1L);
        
        //Assert
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUserName("TestUser1");
	}
	
	@Test
	@WithMockUser("AdminUser")
	@Transactional
	void test_deleteUser_asAdmin_isSuccesful() {
		//Mock
		User adminUser = User.builder()
                .user_id(1L)
                .firstName("Admin")
                .lastName("User")
                .userName("AdminUser")
                .password("Test")
                .role(Role.ADMIN)
                .build();
		
        User existingUser = User.builder()
                .user_id(1L)
                .firstName("TestUser1")
                .lastName("User1")
                .userName("TestUser1")
                .password("Test")
                .role(Role.USER)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUserName("AdminUser")).thenReturn(Optional.of(adminUser));
        
        //Act
        userService.deleteUser(1L);
        
        //Assert
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUserName("AdminUser");
	}
	
	@Test
	@Transactional
	void test_deleteUser_returnsUserNotFoundException() {
		//Mock
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		
		//Act
		UserNotFoundException exception = assertThrows(UserNotFoundException.class,() -> {
			userService.deleteUser(1L);
		});
		
		//Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "Could not find user " + 1L);
	}
	

	@Test
	@WithMockUser(username = "TestUser2")
	@Transactional
	void test_deleteUser_isUnauthorized() {
		// Mock
        User existingUser = User.builder()
                .user_id(1L)
                .firstName("TestUser1")
                .lastName("User1")
                .userName("TestUser1")
                .password("Test")
                .role(Role.USER)
                .build();
        
		// Mock
        User authUser = User.builder()
                .user_id(1L)
                .firstName("TestUser2")
                .lastName("User2")
                .userName("TestUser2")
                .password("Test")
                .role(Role.USER)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUserName("TestUser2")).thenReturn(Optional.of(authUser));
        
		//Act
		UnauthorizedException exception = assertThrows(UnauthorizedException.class,() -> {
			userService.deleteUser(1L);
		});
		
		//Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "TestUser1 is unauthorized to perform this action.");
		verify(userRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser2");
	}

}
