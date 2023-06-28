package com.real.time.chatapp;

public class UserNotFoundException extends RuntimeException{
	UserNotFoundException(Long id){
		super("Could not find user " + id);
	}
}
