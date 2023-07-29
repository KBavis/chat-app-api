package com.real.time.chatapp.Exception;

public class UserNotFoundException extends RuntimeException{
	public UserNotFoundException(Long id){
		super("Could not find user " + id);
	}
	
	public UserNotFoundException(String username) {
		super("Could not find user " + username);
	}
}
