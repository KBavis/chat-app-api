package com.real.time.chatapp.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.real.time.chatapp.Exception.MessageNotFoundException;

@ControllerAdvice
public class MessageNotFoundAdvice {
	
	@ResponseBody
	@ExceptionHandler(MessageNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String messageNotFoundHandler(MessageNotFoundException ex) {
		return ex.getMessage();
	}
}