package com.real.time.chatapp.Entities;



import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//TODO: Entities cannot reference other entities, so figure out how to configuer Message and Conversation
//
@Entity
@Table(name = "message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
	private @Id @GeneratedValue Long message_id;
	private boolean isRead;
	private Date sendDate = new Date(); 
	private String content;
	/**
	 *  Many Messages to One User
	 *  Join Column states our column in Message table will be called 'recipient_id' 
	 */
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "recipient_user_id", referencedColumnName = "user_id")
	private User recipient;
	
	/**
	 *  Many Messages to One User
	 *  Join Column states our column in Message table will be called 'sender_id' 
	 */
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "sender_user_id", referencedColumnName ="user_id")
	private User sender;
	
    /**
     *  Many Messages To One Conversations
     */
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "conversation_id", referencedColumnName = "conversation_id")
	private Conversation conversation;
	

	
}
