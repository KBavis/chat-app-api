package com.real.time.chatapp.DTO;

import java.util.Date;
import java.util.List;

import com.real.time.chatapp.Entities.Message;

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
public class ConversationDTO {
	private Long id;
	private int numUsers;
	private Date conversationStart;
	private List<Message> messages;
}
