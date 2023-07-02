package com.real.time.chatapp.Exception;

public class UserNotInConversationException extends RuntimeException{
	public UserNotInConversationException(Long userId, Long conversationId) {
		super("User " + userId + " not found in Conversation " + conversationId);
	}
}
