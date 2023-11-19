package com.real.time.chatapp.Kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.real.time.chatapp.DTO.MessageDTO;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor
public class JsonKafkaConsumer {
	private static Logger LOGGER = LoggerFactory.getLogger(JsonKafkaConsumer.class);
	
	private final SimpMessagingTemplate messagingTemplate;
	
    @KafkaListener(topics = "messages", groupId = "myGroup")
    public void consume(ConsumerRecord<String, MessageDTO> record) {
        LOGGER.info(String.format("Json Message Received -> Key: %s, Value: %s, Topic: %s, Partition: %d, Offset: %d",
                record.key(), record.value(), record.topic(), record.partition(), record.offset()));
        
        //Send Message to WebSocket Clients Associated With The Conversation 
        Long conversationId = record.value().getConversationId();
        MessageDTO message = record.value();
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId.toString(), message);
        
    }
}
