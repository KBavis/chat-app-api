package com.real.time.chatapp.Exception;

public class MessageNotFoundException extends RuntimeException {
	public MessageNotFoundException(Long id) {
		super("Message not found: " + id);
	}
}
