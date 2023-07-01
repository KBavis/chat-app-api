package com.real.time.chatapp.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ConversationNotFoundAdvice {
	
	@ResponseBody
	@ExceptionHandler(ConversationNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String conversationNotFoundHandler(ConversationNotFoundException ex) {
		return ex.getMessage();
	}
}
