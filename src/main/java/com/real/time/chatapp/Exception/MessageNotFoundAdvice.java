package com.real.time.chatapp.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class MessageNotFoundAdvice {
	@ResponseBody
	@ExceptionHandler(MessageNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String userNotFoundHandler(MessageNotFoundException ex) {
		return ex.getMessage();
	}
}
