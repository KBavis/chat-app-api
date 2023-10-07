package com.real.time.chatapp.Kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.real.time.chatapp.DTO.MessageDTO;


@Service 
public class JsonKafkaConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonKafkaConsumer.class);
	
    @KafkaListener(topicPattern = "conversation-.*", groupId = "myGroup")
    public void consume(ConsumerRecord<String, MessageDTO> record) {
        LOGGER.info(String.format("Json Message Received -> Key: %s, Value: %s, Topic: %s, Partition: %d, Offset: %d",
                record.key(), record.value(), record.topic(), record.partition(), record.offset()));
    }
}
