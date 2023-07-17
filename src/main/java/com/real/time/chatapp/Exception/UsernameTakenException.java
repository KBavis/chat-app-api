package com.real.time.chatapp.Exception;


public class UsernameTakenException extends RuntimeException {
	public UsernameTakenException(String username) {
		super("Username " + username + " already exists.");
	}
}
