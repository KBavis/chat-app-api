package com.real.time.chatapp.Config;

import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
	
//	@Bean
//	public NewTopic conversationTopic(String conversationId) {
//		return TopicBuilder.name("conversation-" + conversationId)
//				.build();
//	}
	
    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
    }
}
