//package com.real.time.chatapp.Kafka;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//import lombok.RequiredArgsConstructor;
//
//@RequiredArgsConstructor
//@Service
//public class KafkaProducer {
//	
//	private final KafkaTemplate<String, String> kafkaTemplate;
//	private static Logger logger = LoggerFactory.getLogger(KafkaProducer.class);
//	
//	public void sendMessage(String message) {
//		logger.info(String.format("Message sent %s", message));
//		kafkaTemplate.send("conversation", message);
//	}
//	
//}
