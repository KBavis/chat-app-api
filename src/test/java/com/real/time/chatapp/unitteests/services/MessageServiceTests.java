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

import com.real.time.chatapp.ControllerServices.MessageService;
import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.MessageNotFoundException;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
public class MessageServiceTests {
	@Mock
	private MessageRepository messageRepository;

	@Mock
	private ConversationRepository conversationRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private MessageService messageService;

	Message m1;
	Message m2;
	Message m3;
	Conversation c1;
	List<Message> mockMessages;
	Set<User> mockUsers;

	@BeforeEach
	void setUp() {
		m1 = Message.builder().content("First Message").conversation(c1).isRead(false).sendDate(new Date())
				.recipients(new HashSet<User>()).sender(new User()).message_id(1L).build();
		m2 = Message.builder().content("Second Message").conversation(c1).isRead(false).sendDate(new Date())
				.recipients(new HashSet<User>()).sender(new User()).message_id(2L).build();
		m3 = Message.builder().content("Third Message").conversation(c1).isRead(false).sendDate(new Date())
				.recipients(new HashSet<User>()).sender(new User()).message_id(3L).build();
		

		mockMessages = List.of(m1, m2, m3);
		
		c1 = Conversation.builder().conversation_id(1L).messages(mockMessages).numUsers(2)
				.conversation_users(new HashSet<User>()).conversationStart(new Date()).build();
	}

	@Test
	@WithMockUser("TestUser")
	void test_getAllMessages_isUnauthorized() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));

		// Act and Assert
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			messageService.getAllMessages();
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");

		// Validate
		verify(userRepository, times(2)).findByUserName("TestUser");
	}

	@Test
	@WithMockUser("AdminUser")
	void test_getAllMessages_isSuccesful() {
		// Mock
		User adminUser = User.builder().user_id(1L).firstName("Admin").lastName("User").userName("AdminUser")
				.password("Password").role(Role.ADMIN).list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("AdminUser")).thenReturn(Optional.of(adminUser));
		when(messageRepository.findAll()).thenReturn(mockMessages);

		// Act
		List<Message> messages = messageService.getAllMessages();

		// Assert
		assertNotNull(messages);
		assertEquals(messages.get(0), m1);
		assertEquals(messages.get(1), m2);
		assertEquals(messages.get(2), m3);

		// Verify
		verify(userRepository, times(1)).findByUserName("AdminUser");
		verify(messageRepository, times(1)).findAll();
	}

	@Test
	@WithMockUser("TestUser")
	void test_getAllUserMessages_isSuccesful() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(messageRepository.findMessagesByUser(testUser)).thenReturn(mockMessages);

		// Act
		List<Message> messages = messageService.getAllUserMessages();

		// Assert
		assertNotNull(messages);
		assertEquals(messages.get(0), m1);
		assertEquals(messages.get(1), m2);
		assertEquals(messages.get(2), m3);

		// Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(messageRepository, times(1)).findMessagesByUser(testUser);
	}

	@Test
	@WithMockUser("TestUser")
	void test_getMessageById_isMessageNotFoundException() {
		// Mock
		when(messageRepository.findById(1L)).thenReturn(Optional.empty());

		// Act and Assert
		MessageNotFoundException ex = assertThrows(MessageNotFoundException.class, () -> {
			messageService.getMessageById(1L);
		});

		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "Message not found: " + 1L);
	}

	@Test
	@WithMockUser("TestUser")
	void test_getMessageById_isUnauthorized() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(messageRepository.findById(1L)).thenReturn(Optional.of(m1));

		// Act and Assert
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			messageService.getMessageById(1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");

		// Validate
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(messageRepository, times(1)).findById(1L);
	}

	@Test
	@WithMockUser("TestUser")
	void test_getMessageById_isSuccesful_whenSentOrReceieved() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(messageRepository.findById(1L)).thenReturn(Optional.of(m1));
		when(messageRepository.findById(2L)).thenReturn(Optional.of(m2));

		// Ensure m1 was sent by user, and m2 recieved by user
		List<Message> sentMessages = testUser.getSentMessages();
		sentMessages.add(m1);
		testUser.setSentMessages(sentMessages);
		Set<Message> recievedMessages = testUser.getRecievedMessages();
		recievedMessages.add(m2);
		testUser.setRecievedMessages(recievedMessages);

		// Act
		Message message1 = messageService.getMessageById(1L);
		Message message2 = messageService.getMessageById(2L);

		// Assert
		assertNotNull(m1);
		assertNotNull(m2);
		assertEquals(m1.getMessage_id(), 1L);
		assertEquals(m2.getMessage_id(), 2L);

		// Validate
		verify(userRepository, times(2)).findByUserName("TestUser");
		verify(messageRepository, times(1)).findById(1L);
		verify(messageRepository, times(1)).findById(2L);
	}

	@Test
	@WithMockUser("TestUser")
	void test_getConversationMessages_isConversationNotFound() {
		// Mock
		when(conversationRepository.findById(1L)).thenReturn(Optional.empty());

		// Act and Assert
		ConversationNotFoundException ex = assertThrows(ConversationNotFoundException.class, () -> {
			messageService.getConversationMessages(1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "Conversation not found: " + 1L);

		// Verify
		verify(conversationRepository, times(1)).findById(1L);
	}

	@Test
	@WithMockUser("TestUser")
	void test_getConversationMessages_isUnauthorized() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));

		// Act and Assert
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			messageService.getConversationMessages(1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");

		// Verify
		verify(conversationRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("TestUser");
	}

	@Test
	@WithMockUser("TestUser")
	void test_getConversationMessages_isSuccesful() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Ensure Conversation Contains Test User
		Set<User> convoUsers = c1.getConversation_users();
		convoUsers.add(testUser);
		c1.setConversation_users(convoUsers);

		//Act
		List<Message> messages = messageService.getConversationMessages(1L);
		
		// Assert
		assertNotNull(messages);
		assertEquals(messages.get(0), m1);
		assertEquals(messages.get(1), m2);
		assertEquals(messages.get(2), m3);

		// Verify
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_searchMessagesByContent_isSuccesful() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(messageRepository.findMessagesByContent("Content", testUser)).thenReturn(mockMessages);
		
		//Act
		List<Message> messages = messageService.searchMessagesByContent("Content");
		
		// Assert
		assertNotNull(messages);
		assertEquals(messages.get(0), m1);
		assertEquals(messages.get(1), m2);
		assertEquals(messages.get(2), m3);

		// Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(messageRepository, times(1)).findMessagesByContent("Content", testUser);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_searchMessagesByDate_isSuccesful() {
		// Mock
		Date date = new Date();
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(messageRepository.findMessagesByDate(date, testUser)).thenReturn(mockMessages);
		
		//Act
		List<Message> messages = messageService.searchMessagesByDate(date);
		
		// Assert
		assertNotNull(messages);
		assertEquals(messages.get(0), m1);
		assertEquals(messages.get(1), m2);
		assertEquals(messages.get(2), m3);

		// Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(messageRepository, times(1)).findMessagesByDate(date, testUser);
	}
	
	@Test
	@WithMockUser("TestUser")
	void test_searchMessagesByRead_isSuccesful() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(messageRepository.findMessageByIsRead(testUser)).thenReturn(mockMessages);
		
		//Act
		List<Message> messages = messageService.searchMessagesByRead();
		
		// Assert
		assertNotNull(messages);
		assertEquals(messages.get(0), m1);
		assertEquals(messages.get(1), m2);
		assertEquals(messages.get(2), m3);

		// Verify
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(messageRepository, times(1)).findMessageByIsRead(testUser);
	}
	
	@Test
	@WithMockUser("TestUser")
	@Transactional
	void test_createMessage_isConversationNotFoundException() {
		//Mock
		when(conversationRepository.findById(1L)).thenReturn(Optional.empty());
		
		// Act and Assert
		ConversationNotFoundException ex = assertThrows(ConversationNotFoundException.class, () -> {
			messageService.getConversationMessages(1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "Conversation not found: " + 1L);

		// Verify
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("TestUser")
	@Transactional
	void test_createMessage_isUnauthorized() {
		// Mock
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(conversationRepository.findById(1L)).thenReturn(Optional.of(c1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			messageService.createMessage(new MessageDTO(), 1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");

		// Validate
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(conversationRepository, times(1)).findById(1L);
	}
	
	@Test
	@WithMockUser("u1")
	@Transactional
	void test_createMesage_isSuccesful() {
		// Mock Users
		User u1 = User.builder()
				.user_id(1L)
				.userName("u1")
				.password("test")
				.sentMessages(new ArrayList<>())
				.recievedMessages(new HashSet<>())
				.build();
		User u2 = User.builder()
				.user_id(1L)
				.userName("u2")
				.password("test")
				.sentMessages(new ArrayList<>())
				.recievedMessages(new HashSet<>())
				.build();
		Set<User> mockUsers = new HashSet<>();
		mockUsers.add(u1);
		mockUsers.add(u2);
		//Mock Conversation
		Conversation c2 = Conversation.builder()
				.conversation_id(2L)
				.messages(new ArrayList<>())
				.conversation_users(mockUsers)
				.numUsers(2)
				.build();
		
		when(userRepository.findByUserName("u1")).thenReturn(Optional.of(u1));
		when(conversationRepository.findById(2L)).thenReturn(Optional.of(c2));
		when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		//MessageDTO
		MessageDTO dto = MessageDTO.builder()
				.content("Content")
				.build();
		
		//Act
		Message createdMessage = messageService.createMessage(dto, 2L);
		
		//Assert
		assertNotNull(createdMessage);
		assertEquals(createdMessage.getContent(), "Content");
		assertEquals(createdMessage.getSender(), u1);
		assertEquals(createdMessage.getRecipients().size(), 1);
		assertTrue(createdMessage.getRecipients().contains(u2));
		assertTrue(c2.getMessages().contains(createdMessage));
		assertTrue(u1.getSentMessages().contains(createdMessage));
		assertTrue(u2.getRecievedMessages().contains(createdMessage));
		
		//Verify 
		verify(conversationRepository, times(1)).findById(2L);
		verify(userRepository, times(1)).findByUserName("u1");
		verify(messageRepository, times(1)).save(any(Message.class));
	}
	
	@Test
	@WithMockUser("TestUser")
	@Transactional
	void test_updateMessage_isUnauthorized() {
		// Mock
		User u1 = User.builder()
				.user_id(1L)
				.userName("u1")
				.password("test")
				.sentMessages(new ArrayList<>())
				.recievedMessages(new HashSet<>())
				.build();
		Message msg1 = Message.builder()
				.content("Testing")
				.sender(u1)
				.build();
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(messageRepository.findById(1L)).thenReturn(Optional.of(msg1));
		
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			messageService.updateMessage(new MessageDTO(), 1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");

		// Validate
		verify(userRepository, times(1)).findByUserName("TestUser");
		verify(messageRepository, times(1)).findById(1L);
	}
	@Test
	@WithMockUser("TestUser")
	@Transactional
	void test_updateMessage_isSuccesful() {
		//Mock
		Date date = new Date();
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		Message msg1 = Message.builder()
				.content("Testing")
				.sender(testUser)
				.build();
		MessageDTO messageDTO = MessageDTO.builder()
				.content("Updated Content")
				.sendDate(date)
				.build();
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		when(messageRepository.findById(1L)).thenReturn(Optional.of(msg1));
		when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
		
		//Act
		Message updatedMesage = messageService.updateMessage(messageDTO, 1L);
		
		//Assert
		assertNotNull(updatedMesage);
		assertEquals(updatedMesage.getContent(), "Updated Content");
		assertEquals(updatedMesage.getSendDate(), date);
		
		// Validate
		verify(messageRepository, times(2)).findById(1L);
		verify(messageRepository, times(1)).save(any(Message.class));
	}
	
	@Test
	@WithMockUser("TestUser")
	@Transactional
	void test_deleteMessage_isUnauthorized() {
		//Mocking
		User u1 = User.builder()
				.user_id(1L)
				.userName("u1")
				.password("test")
				.sentMessages(new ArrayList<>())
				.recievedMessages(new HashSet<>())
				.build();
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		Message m1 = Message.builder()
				.message_id(1L)
				.sender(u1)
				.build();
		
		when(messageRepository.findById(1L)).thenReturn(Optional.of(m1));
		when(userRepository.findByUserName("TestUser")).thenReturn(Optional.of(testUser));
		
		//Act and Assert
		UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> {
			messageService.deleteMessage(1L);
		});
		assertNotNull(ex);
		assertEquals(ex.getLocalizedMessage(), "TestUser is unauthorized to perform this action.");
		
		verify(messageRepository, times(1)).findById(1L);
		verify(userRepository, times(2)).findByUserName("TestUser");
	}
	
	@Test
	@WithMockUser("TestUser")
	@Transactional
	void test_deleteMessage_isSuccesful_asUser() {
		//Mocking
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		Message m1 = Message.builder()
				.message_id(1L)
				.sender(testUser)
				.build();
		when(messageRepository.findById(1L)).thenReturn(Optional.of(m1));
		
		//Act
		messageService.deleteMessage(1L);
		
		//Verify
		verify(messageRepository, times(1)).findById(1L);
	}
	
	
	@Test
	@WithMockUser("AdminUser")
	@Transactional
	void test_deleteMessage_isSuccesful_asAdmin() {
		//Mocking
		User testUser = User.builder().user_id(1L).firstName("Test").lastName("User").userName("TestUser")
				.password("Password").role(Role.USER).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		User adminUser = User.builder().user_id(1L).firstName("Admin").lastName("User").userName("AdminUser")
				.password("Password").role(Role.ADMIN).sentMessages(new ArrayList<>()).recievedMessages(new HashSet<>())
				.list_conversations(new HashSet<Conversation>()).build();
		Message m1 = Message.builder()
				.message_id(1L)
				.sender(testUser)
				.build();
		when(messageRepository.findById(1L)).thenReturn(Optional.of(m1));
		when(userRepository.findByUserName("AdminUser")).thenReturn(Optional.of(adminUser));
		
		//Act
		messageService.deleteMessage(1L);
		
		//Verify
		verify(messageRepository, times(1)).findById(1L);
		verify(userRepository, times(1)).findByUserName("AdminUser");
	}
}
