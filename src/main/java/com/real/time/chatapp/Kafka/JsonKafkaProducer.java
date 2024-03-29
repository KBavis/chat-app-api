package com.real.time.chatapp.Kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.DTO.MessageResponseDTO;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class JsonKafkaProducer {
	
	private static Logger LOGGER = LoggerFactory.getLogger(JsonKafkaProducer.class);
	private final KafkaTemplate<String, MessageDTO> kafkaTemplate;
	
	public void sendMessage(MessageDTO data) {
		String topicName = "messages";
		LOGGER.info(String.format("Topic: "+ topicName +", Message sent -> %s", data.toString()));
		Message<MessageDTO> message = MessageBuilder
				.withPayload(data)
				.setHeader(KafkaHeaders.TOPIC, topicName)
				.setHeader(KafkaHeaders.KEY, data.getMessage_id().toString())
				.build();
		
		kafkaTemplate.send(message);
	}
}
