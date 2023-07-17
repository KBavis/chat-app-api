package com.real.time.chatapp.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.real.time.chatapp.Exception.UserNotInConversationException;

@ControllerAdvice
public class UserNotInConversationAdvice {
	@ResponseBody
	@ExceptionHandler(UserNotInConversationException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String errMsg(UserNotInConversationException ex) {
		return ex.getMessage();
	}
}
