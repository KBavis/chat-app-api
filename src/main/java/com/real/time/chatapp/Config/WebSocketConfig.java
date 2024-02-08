package com.real.time.chatapp.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
	
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    //TODO: Change This From Local Host to Our EC2 Instance
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect").setAllowedOrigins(
        		"http://localhost:3000", 
        		"http://ec2-3-90-160-1.compute-1.amazonaws.com:3000",
        		"http://ec2-3-90-160-1.compute-1.amazonaws.com" 
        		).withSockJS(); //added ec2 instance (for reverse proxy and not)
    }
}
