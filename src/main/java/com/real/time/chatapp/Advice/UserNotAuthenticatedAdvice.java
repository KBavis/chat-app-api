package com.real.time.chatapp.Advice;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.real.time.chatapp.Exception.UserNotAuthenticatedException;

@ControllerAdvice
public class UserNotAuthenticatedAdvice {
	@ResponseBody
	@ExceptionHandler(UserNotAuthenticatedException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	String userNotFoundHandler(UserNotAuthenticatedException ex) {
		return ex.getMessage();
	}
}

