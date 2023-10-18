package com.real.time.chatapp.Config;

import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfig.class);
	
	@Bean
	public NewTopic conversationTopic() {
		LOGGER.info("New Kafka Topic Creatged: messages");
		return TopicBuilder.name("messages")
				.build();
	}
	
    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
    }
}
