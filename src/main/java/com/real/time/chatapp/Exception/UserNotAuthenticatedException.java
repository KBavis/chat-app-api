package com.real.time.chatapp.Exception;

public class UserNotAuthenticatedException extends RuntimeException{
	public UserNotAuthenticatedException(String userName) {
		super("User not authenticated: " + userName);
	}
}
