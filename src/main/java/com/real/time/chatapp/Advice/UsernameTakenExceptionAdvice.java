package com.real.time.chatapp.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.real.time.chatapp.Exception.UsernameTakenException;

@ControllerAdvice
public class UsernameTakenExceptionAdvice {
	@ResponseBody
	@ExceptionHandler(UsernameTakenException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String conversationNotFoundHandler(UsernameTakenException ex) {
		return ex.getMessage();
	}
}
