package com.real.time.chatapp.integration.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.real.time.chatapp.Auth.AuthenticationResponse;
import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Kafka.JsonKafkaConsumer;
import com.real.time.chatapp.Kafka.JsonKafkaProducer;
import com.real.time.chatapp.Repositories.UserRepository;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext
public class KafkaImplementationTest {
	RestIntegrationTestHelper testHelper;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	UserRepository userRepository;

	@Autowired
	JsonKafkaConsumer kafkaConsumer;

	@MockBean
	private SimpMessagingTemplate messagingTemplate;

	@MockBean
	private JsonKafkaProducer mockKafkaProducer;

	@BeforeEach
	void setUp() throws Exception {
		testHelper = new RestIntegrationTestHelper(mockMvc, userRepository);
	}

	@Test
	public void testProducerAndConsumer() throws Exception {
		// Signup Users
		testHelper.signUp("TestUser", "Password");
		testHelper.signUp("TestUser2", "Password1");

		// Fetch User
		User user = userRepository.findByUserName("TestUser2").orElseThrow();

		// Authenticate User
		AuthenticationResponse authResponse = testHelper.loginAndReturnToken("TestUser", "Password");

		// Create Conversation
		Conversation conversation = testHelper.addConversation(user.getUser_id(), authResponse);

		// Create Message
		MessageDTO messageDTO = MessageDTO.builder().content("Testing The Message!")
				.conversationId(conversation.getConversation_id()).build();
		String jsonMessageDTO = new ObjectMapper().writeValueAsString(messageDTO);

		// Call Endpoint
		mockMvc.perform(post("/messages/{conversationId}", conversation.getConversation_id())
				.header("Authorization", "Bearer " + authResponse.getToken()).contentType(MediaType.APPLICATION_JSON)
				.content(jsonMessageDTO)).andExpect(status().isCreated());

		// Ensure Kafka Producer Sent Message
		ArgumentCaptor<MessageDTO> messageCaptor = ArgumentCaptor.forClass(MessageDTO.class);
		verify(mockKafkaProducer, times(1)).sendMessage(messageCaptor.capture());
		verify(mockKafkaProducer, times(1)).sendMessage(any(MessageDTO.class));
		MessageDTO messageSent = messageCaptor.getValue();
		assertTrue(messageSent.getConversationId().equals(conversation.getConversation_id()));
		assertTrue(messageSent.getContent().equals("Testing The Message!"));

		// Ensure Consumer Gets Message
		// Step 3: Simulate Kafka consumer
		kafkaConsumer.consume(new ConsumerRecord<>("messages", 0, 0, "key", messageDTO));

		// Verify that the messagingTemplate.convertAndSendToUser was called with the
		// correct arguments
		verify(messagingTemplate, times(1)).convertAndSendToUser(conversation.getConversation_id().toString(),
				"/topic/conversation/" + conversation.getConversation_id(), messageDTO);

	}
}
