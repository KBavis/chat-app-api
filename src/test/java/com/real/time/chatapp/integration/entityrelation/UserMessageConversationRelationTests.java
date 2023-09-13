package com.real.time.chatapp.integration.entityrelation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.MessageNotFoundException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Integration Tests for User, Conversation, and Message Relations
 * 
 * @author bavis
 *
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserMessageConversationRelationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ConversationRepository conversationRepository;

	@Autowired
	private MessageRepository messageRepository;

	private ObjectMapper mapper = new ObjectMapper();
	private IntegrationTestHelper testHelper;

	@BeforeEach
	@Transactional
	void setUp() throws Exception {
		testHelper = new IntegrationTestHelper(mockMvc, userRepository);
		testHelper.signUp("test1", "password");
		testHelper.signUp("test2", "password");
		testHelper.signUp("test3", "password");
		testHelper.signUp("test4", "password");
	}

	/**
	 * Integration Test for Creating a Conversation and Sending a Message with Two
	 * Users in Conversation
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_createConversation_singleMessage_twoUsers() throws Exception {

		// Create Conversation Between User 'test1' & 'test2'
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test1", "password");
		User u1 = userRepository.findByUserName("test1").orElseThrow(() -> new UserNotFoundException("test1"));
		User u2 = userRepository.findByUserName("test2").orElseThrow(() -> new UserNotFoundException("test2"));
		Long conversationId = testHelper.createConversationAndReturnId(authResponse, u2);
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		HashSet<User> conversationUsers = new HashSet<>(conversation.getConversation_users());
		
		//Ensure The Appropiate Users Are in the Conversation
		assertThat(conversationUsers, allOf(hasItem(u1), hasItem(u2)));
		assertThat(u1.getList_conversations(), hasItem(conversation));
		assertThat(u2.getList_conversations(), hasItem(conversation));

		//Send Message From User 'test1' to User 'test2' 
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setContent("Hi test2");
		Long messageId = testHelper.sendMessageAndReturnId(messageDTO, conversationId, authResponse);
		Message message = messageRepository.findById(messageId)
				.orElseThrow(() -> new MessageNotFoundException(messageId));
		
		//Ensure The Conversation Has The Message 
		assertThat(conversation.getMessages(), hasItem(message));
		
		//Ensure The Message Has The Appropiate Sender, Recipient, and Conversation
		assertEquals(message.getSender(), u1);
		assertEquals(message.getConversation(), conversation);
		assertThat(message.getRecipients(), hasItem(u2));
		
		//Ensure 'test1' Has Sent the Message 
		assertThat(u1.getSentMessages(), hasItem(message));
		
		//Esnure 'test2' Has Recieved The Message
		assertThat(u2.getRecievedMessages(), hasItem(message));
		
		//Ensure The Message Content Is Correct
		assertEquals(message.getContent(), "Hi test2");
	}

	/**
	 * Integration Test for Creating a Conversation and Sending a Message with
	 * Multiple Users In Conversation
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_createConversation_singleMessage_multipleUsers() throws Exception {
		// Create Conversation Between test1 & test2
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test1", "password");
		User u1 = userRepository.findByUserName("test1").orElseThrow(() -> new UserNotFoundException("test1"));
		User u2 = userRepository.findByUserName("test2").orElseThrow(() -> new UserNotFoundException("test2"));
		User u3 = userRepository.findByUserName("test3").orElseThrow(() -> new UserNotFoundException("test3"));

		Long conversationId = testHelper.createConversationAndReturnId(authResponse, u2);
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		HashSet<User> conversationUsers = new HashSet<>(conversation.getConversation_users());
		
		//Ensure The Conversation and Users Have Been Linked Properly
		assertThat(conversationUsers, allOf(hasItem(u1), hasItem(u2)));
		assertThat(u1.getList_conversations(), hasItem(conversation));
		assertThat(u2.getList_conversations(), hasItem(conversation));

		//Send Message With Two Users In Conversation
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setContent("Hi test2");
		Long messageId = testHelper.sendMessageAndReturnId(messageDTO, conversationId, authResponse);

		// Add User To Conversation
		testHelper.addUserToConversation(conversationId, u3, authResponse);

		// Re-extract updated Conversation
		conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		conversationUsers = new HashSet<>(conversation.getConversation_users());

		// Ensure User Added Properly To Conversation
		assertThat(conversationUsers, allOf(hasItem(u1), hasItem(u2), hasItem(u3)));
		assertThat(u3.getList_conversations(), hasItem(conversation));

		// Authenticate User 3
		AuthenticationResponse authResponse2 = testHelper.loginAndReturnToken("test3", "password");

		// Send Message With Three Users In Conversation
		MessageDTO messageDTO2 = new MessageDTO();
		messageDTO2.setContent("Hi test1 and test2");
		Long messageIdTwo = testHelper.sendMessageAndReturnId(messageDTO2, conversationId, authResponse2);

		// Extract Two Sent Messages
		Message msg1 = messageRepository.findById(messageId).orElseThrow(() -> new MessageNotFoundException(messageId));
		Message msg2 = messageRepository.findById(messageIdTwo)
				.orElseThrow(() -> new MessageNotFoundException(messageIdTwo));

		// Ensure That Msg1 And Msg2 Are Within This Conversation
		assertEquals(msg1.getConversation(), conversation);
		assertEquals(msg2.getConversation(), conversation);
		assertThat(conversation.getMessages(), hasItem(msg1));
		assertThat(conversation.getMessages(), hasItem(msg2));

		// Ensure Msg1 only between test1 and test2
		assertThat(msg1.getRecipients(), hasItem(u2));
		assertThat(msg1.getRecipients(), not(hasItem(u3)));
		assertEquals(msg1.getSender(), u1);
		assertThat(u2.getRecievedMessages(), hasItem(msg1));
		assertThat(u1.getSentMessages(), hasItem(msg1));
		assertThat(u3.getRecievedMessages(), not(hasItem(msg1)));

		// Ensure Msg2 Was Recieved By All Users
		assertThat(msg2.getRecipients(), allOf(hasItem(u1), hasItem(u2)));
		assertEquals(msg2.getSender(), u3);
		assertThat(u1.getRecievedMessages(), hasItem(msg2));
		assertThat(u2.getRecievedMessages(), hasItem(msg2));
		assertThat(u3.getSentMessages(), hasItem(msg2));
		
		//Ensure The Content Of Each Messsage Is Correct
		assertEquals(msg1.getContent(), "Hi test2");
		assertEquals(msg2.getContent(), "Hi test1 and test2");
	}
	
	
	/**
	 * Integration Test for Creating a Conversation with Two Users and Sending Multiple Messages
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_createConversation_multipleMessages_twoUsers() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test1", "password");
		User u1 = userRepository.findByUserName("test1").orElseThrow(() -> new UserNotFoundException("test1"));
		User u2 = userRepository.findByUserName("test2").orElseThrow(() -> new UserNotFoundException("test2"));
		
		// Create Conversation Between test1 & test2
		Long conversationId = testHelper.createConversationAndReturnId(authResponse, u2);
		
		// Create Muiltiple Messages
		MessageDTO messageDTO1 = new MessageDTO();
		messageDTO1.setContent("Hi test2! This is test1");

		MessageDTO messageDTO2 = new MessageDTO();
		messageDTO2.setContent("Hi test1! This is test2");
		
		// Authorize Each User
		AuthenticationResponse authResponse1 = testHelper.loginAndReturnToken("test1", "password");
		AuthenticationResponse authResponse2 = testHelper.loginAndReturnToken("test2", "password");
		
		// Send Each Message
		Long msg1ID = testHelper.sendMessageAndReturnId(messageDTO1, conversationId, authResponse1);
		Long msg2ID = testHelper.sendMessageAndReturnId(messageDTO2, conversationId, authResponse2);
		
		// Ensure Conversation has been Updated with Sent Messages
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		List<Message> conversationMessages = conversation.getMessages();
		Message msg1 = messageRepository.findById(msg1ID).orElseThrow(() -> new MessageNotFoundException(msg1ID));
		Message msg2 = messageRepository.findById(msg2ID).orElseThrow(() -> new MessageNotFoundException(msg2ID));
		assertThat(conversationMessages, allOf(hasItem(msg1), hasItem(msg2)));
		
		//Ensure Each User is in the Conversation
		assertThat(conversation.getConversation_users(), allOf(hasItem(u1), hasItem(u2)));
		
		// Ensure Each Message Has Been Sent in Appropiate Conversation
		assertEquals(msg1.getConversation(), conversation);
		assertEquals(msg2.getConversation(), conversation);
		
		// Ensure Each Message Has Proper Recipients
		assertThat(msg1.getRecipients(), hasItem(u2));
		assertThat(msg2.getRecipients(), hasItem(u1));
		
		// Ensure Each Message Has Proper Sender
		assertEquals(msg1.getSender(), u1);
		assertEquals(msg2.getSender(), u2);
		
		//Ensure Each User Has Recieved Their Proper Messages
		assertThat(u1.getRecievedMessages(), hasItem(msg2));
		assertThat(u2.getRecievedMessages(), hasItem(msg1));
		
		// Ensure Each User Has Their Proper Sent Messages
		assertThat(u1.getSentMessages(), hasItem(msg1));
		assertThat(u2.getSentMessages(), hasItem(msg2));
		
		//Ensure The Content Of Each Message Is Correct
		assertEquals(msg1.getContent(), "Hi test2! This is test1");
		assertEquals(msg2.getContent(), "Hi test1! This is test2");

	}
	/**
	 * Integration Test for Creating a Conversation with Multiple Users and Sending Multiple Messages
	 * 
	 * @throws Exception
	 */
	@Test
	@Transactional
	void test_createConversation_multipleMessages_multipleUsers() throws Exception {
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("test1", "password");
		User u1 = userRepository.findByUserName("test1").orElseThrow(() -> new UserNotFoundException("test1"));
		User u2 = userRepository.findByUserName("test2").orElseThrow(() -> new UserNotFoundException("test2"));
		User u3 = userRepository.findByUserName("test3").orElseThrow(() -> new UserNotFoundException("test3"));
		User u4 = userRepository.findByUserName("test4").orElseThrow(() -> new UserNotFoundException("test4"));

		// Create Conversation Between test1 & test2
		Long conversationId = testHelper.createConversationAndReturnId(authResponse, u2);

		// Add User 'test3' and 'test4' to Conversation
		testHelper.addUserToConversation(conversationId, u3, authResponse);
		testHelper.addUserToConversation(conversationId, u4, authResponse);

		// Create Muiltiple Messages
		MessageDTO messageDTO1 = new MessageDTO();
		messageDTO1.setContent("Hi test1, test2, and test3! This is test4");

		MessageDTO messageDTO2 = new MessageDTO();
		messageDTO2.setContent("Hi test1, test2, and test4! This is test3");

		MessageDTO messageDTO3 = new MessageDTO();
		messageDTO3.setContent("Hi test1, test3, and test4! This is test2");

		MessageDTO messageDTO4 = new MessageDTO();
		messageDTO4.setContent("Hi test2, test3, and test4! This is test1");

		// Authorize Each User
		AuthenticationResponse authResponse1 = testHelper.loginAndReturnToken("test1", "password");
		AuthenticationResponse authResponse2 = testHelper.loginAndReturnToken("test2", "password");
		AuthenticationResponse authResponse3 = testHelper.loginAndReturnToken("test3", "password");
		AuthenticationResponse authResponse4 = testHelper.loginAndReturnToken("test4", "password");

		// Send Each Message
		Long msg1ID = testHelper.sendMessageAndReturnId(messageDTO1, conversationId, authResponse4);
		Long msg2ID = testHelper.sendMessageAndReturnId(messageDTO2, conversationId, authResponse3);
		Long msg3ID = testHelper.sendMessageAndReturnId(messageDTO3, conversationId, authResponse2);
		Long msg4ID = testHelper.sendMessageAndReturnId(messageDTO4, conversationId, authResponse1);

		// Ensure Conversation has been Updated with Sent Messages
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ConversationNotFoundException(conversationId));
		List<Message> conversationMessages = conversation.getMessages();
		Message msg1 = messageRepository.findById(msg1ID).orElseThrow(() -> new MessageNotFoundException(msg1ID));
		Message msg2 = messageRepository.findById(msg2ID).orElseThrow(() -> new MessageNotFoundException(msg2ID));
		Message msg3 = messageRepository.findById(msg3ID).orElseThrow(() -> new MessageNotFoundException(msg3ID));
		Message msg4 = messageRepository.findById(msg4ID).orElseThrow(() -> new MessageNotFoundException(msg4ID));
		assertThat(conversationMessages, allOf(hasItem(msg1), hasItem(msg2), hasItem(msg3), hasItem(msg4)));

		// Ensure Each User Is In Conversation
		assertThat(conversation.getConversation_users(), allOf(hasItem(u1), hasItem(u2), hasItem(u3), hasItem(u4)));

		// Ensure Each Message Has Been Sent in Appropiate Conversation
		assertEquals(msg1.getConversation(), conversation);
		assertEquals(msg2.getConversation(), conversation);
		assertEquals(msg3.getConversation(), conversation);
		assertEquals(msg4.getConversation(), conversation);

		// Ensure Each Message Has Proper Recipients
		assertThat(msg1.getRecipients(), allOf(hasItem(u1), hasItem(u2), hasItem(u3)));
		assertThat(msg2.getRecipients(), allOf(hasItem(u1), hasItem(u2), hasItem(u4)));
		assertThat(msg3.getRecipients(), allOf(hasItem(u1), hasItem(u3), hasItem(u4)));
		assertThat(msg4.getRecipients(), allOf(hasItem(u2), hasItem(u3), hasItem(u4)));

		// Ensure Each Message Has Proper Sender
		assertEquals(msg1.getSender(), u4);
		assertEquals(msg2.getSender(), u3);
		assertEquals(msg3.getSender(), u2);
		assertEquals(msg4.getSender(), u1);

		//Ensure Each User Has Recieved Their Proper Messages
		assertThat(u1.getRecievedMessages(), allOf(hasItem(msg1), hasItem(msg2), hasItem(msg3)));
		assertThat(u2.getRecievedMessages(), allOf(hasItem(msg1), hasItem(msg2), hasItem(msg4)));
		assertThat(u3.getRecievedMessages(), allOf(hasItem(msg1), hasItem(msg3), hasItem(msg4)));
		assertThat(u4.getRecievedMessages(), allOf(hasItem(msg2), hasItem(msg3), hasItem(msg4)));

		// Ensure Each User Has Their Proper Sent Messages
		assertThat(u1.getSentMessages(), hasItem(msg4));
		assertThat(u2.getSentMessages(), hasItem(msg3));
		assertThat(u3.getSentMessages(), hasItem(msg2));
		assertThat(u4.getSentMessages(), hasItem(msg1));
		
		//Ensure Each Message Content Is Correct
		assertEquals(msg1.getContent(), "Hi test1, test2, and test3! This is test4");
		assertEquals(msg2.getContent(), "Hi test1, test2, and test4! This is test3");
		assertEquals(msg3.getContent(), "Hi test1, test3, and test4! This is test2");
		assertEquals(msg4.getContent(), "Hi test2, test3, and test4! This is test1");

	}

}
