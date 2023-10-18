package com.real.time.chatapp.integration.webSocket;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.real.time.chatapp.DTO.MessageDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTests {
	private String URL;
	
	private CompletableFuture<MessageDTO> completableFuture;
	
    @BeforeEach
    public void setup() {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:8080/ws";
    }
    
    @Test
    @WithMockUser
    @SuppressWarnings("deprecation")
    public void testSendMessage() throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
    	WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
    	stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    	
        
		StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);
		
		 stompSession.subscribe("/topic/conversation/1", new SendMessageStompFrameHandler());
    }
    
    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }
    
    private class SendMessageStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return MessageDTO.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object payload) {
            if (payload instanceof MessageDTO) {
                completableFuture.complete((MessageDTO) payload);
            } else {
                completableFuture.completeExceptionally(new IllegalStateException("Unexpected payload type: " + payload.getClass()));
            }
        }
    }
}

