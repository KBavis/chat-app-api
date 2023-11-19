package com.real.time.chatapp.integration.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
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

@SpringBootTest
@AutoConfigureMockMvc
public class KafkaConsumeProducerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private AdminClient adminClient;

	@Autowired
	private JsonKafkaProducer kafkaProducer;

	@Autowired
	private JsonKafkaConsumer kafkaConsumer;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@MockBean
	private KafkaTemplate<String, MessageDTO> kafkaTemplate;

	@MockBean
	private SimpMessagingTemplate messagingTempalte;

	@Value("${spring.kafka.template.default-topic}")
	private String defaultTopic;

	@Captor
	private ArgumentCaptor<Message<MessageDTO>> messageCaptor;

	RestIntegrationTestHelper testHelper;

	@BeforeEach
	void setUp() throws Exception {
		testHelper = new RestIntegrationTestHelper(mockMvc, userRepository);
	}

	/**
	 * Ensures That The Default Topic Is Created
	 */
	@Test
	public void testTopicCreationOnStartup() throws InterruptedException, ExecutionException {
		boolean topicExists = adminClient.listTopics().names().get().contains(defaultTopic);
		assertTrue(topicExists, "Topic Should Be Created On Startup By Configuration");
	}

	/**
	 * Ensures That The Kafka Producer Is Correctly Sending The Message To The Topic
	 */
	@Test
	public void testMessageSendingToTopic() {
		// Mock The KafkaProducer to use the KafkaTemplate
		MessageDTO messageDTO = MessageDTO.builder().content("Testing The Message!").conversationId(13L).id(1L).build();
		kafkaProducer.sendMessage(messageDTO);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Message<MessageDTO>> messageCaptor = ArgumentCaptor.forClass(Message.class);
		verify(kafkaTemplate, times(1)).send(any(Message.class));
		verify(kafkaTemplate).send(messageCaptor.capture());

		Message<MessageDTO> capturedMessage = messageCaptor.getValue();
		assertEquals("messages", capturedMessage.getHeaders().get(KafkaHeaders.TOPIC));
		assertEquals(messageDTO, capturedMessage.getPayload());
	}

	/**
	 * Ensures The Consumer Sends The Consumed Message Correctly
	 */
	@Test
	public void testConsumerSendsToWebSocket() {
		// Create ConsumerRecord For Testing
		MessageDTO message = MessageDTO.builder().content("Test").id(1L).conversationId(2L).build();
		ConsumerRecord<String, MessageDTO> record = new ConsumerRecord<>("messages", 0, 0, "key", message);

		// Consume Record
		kafkaConsumer.consume(record);

		// Assertions
		verify(messagingTemplate, times(1)).convertAndSend(
				"/topic/conversation/" + message.getConversationId(), message);
	}

	@Test
	@DirtiesContext
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
				.conversationId(conversation.getConversation_id()).id(1L).build();
		String jsonMessageDTO = new ObjectMapper().writeValueAsString(messageDTO);

		// Call Endpoint
		mockMvc.perform(post("/messages/{conversationId}", conversation.getConversation_id())
				.header("Authorization", "Bearer " + authResponse.getToken()).contentType(MediaType.APPLICATION_JSON)
				.content(jsonMessageDTO)).andExpect(status().isCreated());

		// Ensure Kafka Producer Sent Message
//		ArgumentCaptor<MessageDTO> messageCaptor = ArgumentCaptor.forClass(MessageDTO.class);
//		verify(mockKafkaProducer, times(1)).sendMessage(messageCaptor.capture()); 
//		verify(mockKafkaProducer, times(1)).sendMessage(any(MessageDTO.class)); 
		
	}

}
