package com.real.time.chatapp.DTO;

import java.util.List;
import java.util.Set;

import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.Role;

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
public class UserResponseDTO {
	private Long user_id;
	private String userName;
	private String firstName;
	private String lastName;
	private String password;
	private Role role;
	private Set<Conversation> list_conversations;
	private List<Message> sentMessges;
	private Set<Message> recievedMessages;
}
