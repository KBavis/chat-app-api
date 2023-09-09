package com.real.time.chatapp.DTO;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.real.time.chatapp.Entities.Message;
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
public class ConversationResponseDTO {
    private Long conversation_id;
    private Date conversationStart;
    private int numUsers;
    private List<Message> messages;
    private List<User> users;
}
