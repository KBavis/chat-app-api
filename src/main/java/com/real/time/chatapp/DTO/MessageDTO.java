package com.real.time.chatapp.DTO;


import java.util.Date;
import java.util.Set;

import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
	private boolean isRead;
	private Date sendDate;
	private String content;
}
