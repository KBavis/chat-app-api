package com.real.time.chatapp.Entities;


import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

//TODO: Entities cannot reference other entities, so figure out how to configuer Message and Conversation
//
@Entity
@Table(name = "message")
public class Message {
	private @Id @GeneratedValue Long message_id;
	private boolean isRead;
	private Date sendDate; 
	private String content;
	private LocalTime sendTime;
	/**
	 *  Many Messages to One User
	 *  Join Column states our column in Message table will be called 'recipient_id' 
	 */
	@ManyToOne
	@JoinColumn(name = "recipient_user_id", referencedColumnName = "user_id")
	private User recipient;
	
	/**
	 *  Many Messages to One User
	 *  Join Column states our column in Message table will be called 'sender_id' 
	 */
	@ManyToOne
	@JoinColumn(name = "sender_user_id", referencedColumnName ="user_id")
	private User sender;
	
    /**
     *  Many Messages To Many Conversations
     */
	@ManyToOne
	@JoinColumn(name = "conversation_id", referencedColumnName = "conversation_id")
	private Conversation conversation;
	
	public Message() {}
	public Message(boolean isRead, User recipient, User sender, Date sendDate, String content, LocalTime time) {
		this.isRead = isRead;
		this.recipient = recipient;
		this.sender = sender;
		this.sendDate = sendDate;
		this.content = content;
		this.sendTime = time;
	}
	public Long getId() {
		return message_id;
	}
	public void setId(Long id) {
		this.message_id = id;
	}
//	public void setConversation(Conversation conversation) {
//		
//	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	public User getRecipient() {
		return recipient;
	}
	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}
	public User getSender() {
		return sender;
	}
	public void setSender(User sender) {
		this.sender = sender;
	}
	public Date getSendDate() {
		return sendDate;
	}
	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public LocalTime getTime() {
		return sendTime;
	}
	public void setTime(LocalTime time) {
		this.sendTime = time;
	}
	public Conversation getConversation() {
		return conversation;
	}
	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}
	@Override
	public int hashCode() {
		return Objects.hash(content, message_id, isRead, recipient, sendDate, sender, sendTime);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		return Objects.equals(content, other.content) && Objects.equals(message_id, other.message_id) && isRead == other.isRead
				&& Objects.equals(recipient, other.recipient) && Objects.equals(sendDate, other.sendDate)
				&& Objects.equals(sender, other.sender)
				&& Objects.equals(sendTime, other.sendTime);
	}
	@Override
	public String toString() {
		return "Message [id=" + message_id + ", isRead=" + isRead + ", recipient=" + recipient + ", sender=" + sender
				+ ", sendDate=" + sendDate + ", content=" + content + ", sendTime=" + sendTime + "]";
	}
	
}
