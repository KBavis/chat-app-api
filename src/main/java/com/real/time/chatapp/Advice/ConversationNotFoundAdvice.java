package com.real.time.chatapp.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.real.time.chatapp.Exception.ConversationNotFoundException;

@ControllerAdvice
public class ConversationNotFoundAdvice {
	
	@ResponseBody
	@ExceptionHandler(ConversationNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String conversationNotFoundHandler(ConversationNotFoundException ex) {
		return ex.getMessage();
	}
}
