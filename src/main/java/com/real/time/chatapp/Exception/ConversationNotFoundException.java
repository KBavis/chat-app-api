package com.real.time.chatapp.Exception;


public class ConversationNotFoundException extends RuntimeException{
	public ConversationNotFoundException(Long id) {
		super("Conversation not found: " + id);
	}
}
