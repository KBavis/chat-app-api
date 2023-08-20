package com.real.time.chatapp.unitteests.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.real.time.chatapp.ControllerServices.ConversationService;
import com.real.time.chatapp.DTO.ConversationDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.UserRepository;

@SpringBootTest
public class ConversationServiceTests {
	
	@Mock
	private ConversationRepository conversationRepository;
	
	@Mock 
	private UserRepository userRepository;
	
	@InjectMocks
	private ConversationService conversationService;
	
	List<Conversation> mockConversation;
	Conversation c1;
	Conversation c2;
	Conversation c3;
	
	@BeforeEach
	void setUp() {
		c1 = Conversation.builder()
				.conversation_id(1L)
				.conversationStart(new Date())
				.conversation_users(new HashSet<User>())
				.build();
		
		c2 = Conversation.builder()
				.conversation_id(2L)
				.conversationStart(new Date())
				.conversation_users(new HashSet<User>())
				.build();
		
		c3 = Conversation.builder()
				.conversation_id(3L)
				.conversationStart(new Date())
				.conversation_users(new HashSet<User>())
				.build();
		
		mockConversation = List.of(c1, c2, c3);
	}
	@Test
	@WithMockUser("TestUser")
	void test_getAllConversations_asUser_isUnauthorized() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.build();
				
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act
		UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
			conversationService.getAllConversations();
		});
		
		//Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");
		
		//Verify
		verify(userRepository, times(2)).findByUserName("TestUser");
	}
	
	@Test
	@WithMockUser("AdminUser")
	void test_getAllConversations_asAdmin_isSuccesful() {
		//Mock
		User adminUser = User.builder()
				.user_id(1L)
				.firstName("Admin")
				.lastName("User")
				.userName("AdminUser")
				.password("Password")
				.role(Role.ADMIN)
				.build();
		when(userRepository.findByUserName("AdminUser")).thenReturn(Optional.of(adminUser));
		when(conversationRepository.findAll()).thenReturn(mockConversation);
		
		
		//Act
		List<Conversation> allConversations = conversationService.getAllConversations();
		
		//Assert
		assertNotNull(allConversations);
		assertEquals(allConversations.size(), 3);
		assertEquals(allConversations.get(0), c1);
		assertEquals(allConversations.get(1), c2);
		assertEquals(allConversations.get(2), c3);
		
		//Verify
		verify(userRepository, times(1)).findByUserName("AdminUser");
		verify(conversationRepository, times(1)).findAll();
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_getAllUserConversations_isSuccesful() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.build();
		when(conversationRepository.findConversationsByUser(testUser)).thenReturn(mockConversation);
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act
		List<Conversation> allConversations = conversationService.getAllUserConversations();
		
		//Assert
		assertNotNull(allConversations);
		assertEquals(allConversations.size(), 3);
		assertEquals(allConversations.get(0), c1);
		assertEquals(allConversations.get(1), c2);
		assertEquals(allConversations.get(2), c3);
		
		//Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).findConversationsByUser(testUser);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_getConversationByID_isSuccesful() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Ensure TestUser is in C1
		Set<User> c1Users = c1.getConversation_users();
		c1Users.add(testUser);
		c1.setConversation_users(c1Users);
		
		//Act
		Conversation convo = conversationService.getConversationById(1L);
		
		//Assert
		assertNotNull(convo);
		assertEquals(convo.getConversation_id(), 1L);
		
		//Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_getConversationByID_isUnauthorized() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act
		UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
			conversationService.getConversationById(1L);
		});
		
		//Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");
		
		//Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_getConversationByID_isNotFound() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.empty());
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act
		ConversationNotFoundException exception = assertThrows(ConversationNotFoundException.class, () -> {
			conversationService.getConversationById(1L);
		});
		
		//Assert
		assertNotNull(exception);
		assertEquals(exception.getLocalizedMessage(), "Conversation not found: " + 1L);
		
		//Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	
	@Test
	@WithMockUser("TestUser")
	void test_searchConversationByDate_isSuccesful() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.build();
		Date date = new Date();
		when(conversationRepository.findConversationsByDate(date, testUser)).thenReturn(mockConversation);
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act
		List<Conversation> conversations = conversationService.searchConversationsByDate(date);
		
		//Assert
		assertNotNull(conversations);
		assertEquals(conversations.size(), 3);
		assertEquals(conversations.get(0), c1);
		assertEquals(conversations.get(1), c2);
		assertEquals(conversations.get(2), c3);
		
		//Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).findConversationsByDate(date, testUser);
	}
	
	@Test
	@WithMockUser("AuthUser")
	void test_searchConversationWithUser_isSuccesful() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.build();
		User loggedInUser = User.builder()
				.user_id(2L)
				.firstName("Logged")
				.lastName("In")
				.userName("LoggedInUser")
				.password("Password")
				.role(Role.USER)
				.build();
		
		when(conversationRepository.findConversationsByUserAndAuthUser(loggedInUser, testUser)).thenReturn(mockConversation);
		when(userRepository.findByUserName("AuthUser")).thenReturn(Optional.of(loggedInUser));
		when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		
		//Act
		List<Conversation> conversations = conversationService.searchConversationsWithUser(1L);
		
		//Assert
		assertNotNull(conversations);
		assertEquals(conversations.size(), 3);
		assertEquals(conversations.get(0), c1);
		assertEquals(conversations.get(1), c2);
		assertEquals(conversations.get(2), c3);
		
		//Verify
		verify(userRepository, times(1)).findByUserName("AuthUser");
		verify(conversationRepository, times(1)).findConversationsByUserAndAuthUser(loggedInUser, testUser);
	}
	
	@Test
	@WithMockUser("AuthUser")
	void test_createConversation_isSuccesful() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		
		User loggedInUser = User.builder()
				.user_id(2L)
				.firstName("Logged")
				.lastName("In")
				.userName("LoggedInUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		
		when(userRepository.findByUserName("AuthUser")).thenReturn(Optional.of(loggedInUser));
		when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		//Act
		Conversation convo = conversationService.createConversation(1L);
		
		//Assert
		assertNotNull(convo);
		assertTrue(convo.getConversation_users().contains(testUser));
		assertTrue(convo.getConversation_users().contains(loggedInUser));
		
		//Verify
		verify(userRepository, times(1)).findByUserName("AuthUser");
		verify(userRepository, times(1)).findById(1L);
		verify(conversationRepository, times(1)).save(any(Conversation.class));
	}
	
	@Test
	@WithMockUser("AuthUser")
	void test_createConversation_isNotFound() {
		//Mock
		User loggedInUser = User.builder()
				.user_id(2L)
				.firstName("Logged")
				.lastName("In")
				.userName("LoggedInUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		
		when(userRepository.findByUserName("AuthUser")).thenReturn(Optional.of(loggedInUser));
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		
		//Act
		UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> {
			conversationService.createConversation(1L);
		});
		
		//Assert
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "Could not find user " + 1L);
		
		//Verify
		verify(userRepository, times(1)).findByUserName("AuthUser");
		verify(userRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("AuthUser")
	void test_updateConversation_isNotFound() {
		//Mock
		User loggedInUser = User.builder()
				.user_id(2L)
				.firstName("Logged")
				.lastName("In")
				.userName("LoggedInUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.empty());
		
		
		//Act
		ConversationNotFoundException ex = assertThrows(ConversationNotFoundException.class, () -> {
			conversationService.updateConversation(1L, new ConversationDTO());
		});
		
		//Assert
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "Conversation not found: " + 1L);
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_updateConversation_isUnauthorized() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			conversationService.updateConversation(1L, new ConversationDTO());
		});
		
		//Assert
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser");
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_updateConversation_isSuccesful() {
		//Mock
		List<Message> messages = new ArrayList<>();
		Date date = new Date();
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		ConversationDTO dto = ConversationDTO.builder()
				.numUsers(2)
				.conversationStart(date)
				.messages(messages)
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		
		//Ensure C1 Contains Test User
		Set<User> c1Users = c1.getConversation_users();
		c1Users.add(testUser);
		c1.setConversation_users(c1Users);		
		
		//Act
		Conversation updatedConversation = conversationService.updateConversation(1L, dto);
		
		//Assert
		assertNotNull(updatedConversation);
		assertEquals(updatedConversation.getNumUsers(), 2);
		assertEquals(updatedConversation.getConversationStart(), date);
		assertEquals(updatedConversation.getMessages(), messages);
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).save(any(Conversation.class));
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_addUserToConversation_isConversationNotFound() {
		//Mock
		when(conversationRepository.findById(1L)).thenReturn(Optional.empty());
		
		//Act and Assert
		ConversationNotFoundException ex = assertThrows(ConversationNotFoundException.class, () -> {
			conversationService.addUserToConversation(1L, 1L);
		});
		
		//Assert
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "Conversation not found: " + 1L);
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_addUserToConversation_isUserNotFound() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		
		//Ensure C1 Contains Test User
		Set<User> c1Users = c1.getConversation_users();
		c1Users.add(testUser);
		c1.setConversation_users(c1Users);	
		
		//Act and Assert
		UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> {
			conversationService.addUserToConversation(1L, 1L);
		});
		
		//Assert
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "Could not find user " + 1L);
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(userRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_addUserToConversation_isUnauthorized() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act and Assert
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			conversationService.addUserToConversation(1L, 1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser");
	}
	
	@Test
	@WithMockUser("LoggedInUser")
	void test_addUserToConversation_isSuccesful() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		
		//Mock
		User loggedInUser = User.builder()
				.user_id(2L)
				.firstName("Logged")
				.lastName("In")
				.userName("LoggedInUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("LoggedInUser")).thenReturn(Optional.of(loggedInUser));
		when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		
		//Ensure C1 Contains Logged In User
		Set<User> c1Users = c1.getConversation_users();
		c1Users.add(loggedInUser);
		c1.setConversation_users(c1Users);	
		
		//Act
		Conversation convo = conversationService.addUserToConversation(1L, 1L);
		
		//Assert
		assertNotNull(convo);
		assertTrue(convo.getConversation_users().contains(loggedInUser));
		assertTrue(convo.getConversation_users().contains(testUser));
		
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("LoggedInUser");
		verify(conversationRepository, times(1)).save(any(Conversation.class));
		verify(userRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_leaveConversation_isConversationNotFound() {
		//Mock
		when(conversationRepository.findById(1L)).thenReturn(Optional.empty());
		
		//Act and Assert
		ConversationNotFoundException ex = assertThrows(ConversationNotFoundException.class, () -> {
			conversationService.addUserToConversation(1L, 1L);
		});
		
		//Assert
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "Conversation not found: " + 1L);
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_leaveConversation_isUnauthorized() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act and Assert
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			conversationService.leaveConversation(1L);
		});
		
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser");
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_leaveConversation_isSuccesful_notDeleted() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		//Ensure C1 Contains Test User And There Are Two User 
		Set<User> c1Users = c1.getConversation_users();
		c1Users.add(testUser);
		c1.setConversation_users(c1Users);	
		c1.setNumUsers(2);
		
		//Act 
		conversationService.leaveConversation(1L);
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).save(any(Conversation.class));
	}
	
	
	@Test
	@WithMockUser("TestUser")
	void test_leaveConversation_isSuccesful_isDeleted() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		//Ensure C1 Contains Test User And There Are Two User 
		Set<User> c1Users = c1.getConversation_users();
		c1Users.add(testUser);
		c1.setConversation_users(c1Users);	
		c1.setNumUsers(1);
		
		//Act 
		conversationService.leaveConversation(1L);
		
		//Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).deleteById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_deleteConversation_isUnauthorized() {
		//Mock
		User testUser = User.builder()
				.user_id(1L)
				.firstName("Test")
				.lastName("User")
				.userName("TestUser")
				.password("Password")
				.role(Role.USER)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act and Assert
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			conversationService.deleteConversation(1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");
		
		//Verify
		verify(userRepository, times(2)).findByUserName("TestUser");
	}
	
	@Test
	@WithMockUser("AdminUser")
	void test_deleteConversation_isSuccesful() {
		//Mock
		User adminUser = User.builder()
				.user_id(1L)
				.firstName("Admin")
				.lastName("User")
				.userName("AdminUser")
				.password("Password")
				.role(Role.ADMIN)
				.list_conversations(new HashSet<Conversation>())
				.build();
		when(userRepository.findByUserName("AdminUser")).thenReturn(Optional.of(adminUser));
		
		//Act
		conversationService.deleteConversation(1L);
		
		//Verify
		verify(userRepository, times(1)).findByUserName("AdminUser");
		verify(conversationRepository,times(1)).deleteById(1L);
	}
}
