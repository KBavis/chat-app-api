//package com.real.time.chatapp.Config;
//
//import java.util.Map;
//
//import org.apache.kafka.clients.admin.NewTopic;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.TopicBuilder;
//
//TODO: Removed this class as it is causing AdminClient connection issues with docker config (also, docker handles the topic creation)
//
//@Configuration
//public class KafkaConfig {
//	
//	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfig.class);
//	
//	@Bean
//	public NewTopic conversationTopic() {
//		LOGGER.info("New Kafka Topic Creatged: messages");
//		return TopicBuilder.name("messages")
//				.build();
//	}
//	
//    @Bean
//    public AdminClient adminClient() {
//    	//changed from localhost:9092 to kafka-container:9092 following utilization of docker-compose
//        return AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092"));
//    }
//}
