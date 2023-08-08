package com.real.time.chatapp.Exception;

import com.real.time.chatapp.Auth.RegisterRequest;

public class BadRegisterRequestException extends RuntimeException {
	public BadRegisterRequestException(RegisterRequest registerRequest) {
		super("Bad Register Request: username[" + registerRequest.getUsername() + "], password["
				+ registerRequest.getPassword() + "]");
	}
}
