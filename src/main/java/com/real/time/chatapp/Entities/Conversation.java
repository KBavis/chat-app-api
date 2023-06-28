package com.real.time.chatapp.Entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "conversation")
public class Conversation {
	// TODO: Enable search capabilities to find conversations (based on who your
	// talking to)
	private @Id @GeneratedValue Long conversation_id;
	private int numUsers;
	
	/**
	 *  Many Conversations To Many Users
	 */
	@ManyToMany(mappedBy="list_conversations", cascade = CascadeType.MERGE)
	private List<User> conversation_users = new ArrayList<>();
	
	/**
	 * One Conversation To Many Messages
	 */
	@OneToMany(mappedBy = "conversation")
	private List<Message> messages;
	
	private Date conversationStart;
	

	public Conversation() {
		this.conversation_users = new ArrayList<>();
		this.messages = new ArrayList<>();
	}

	public Conversation(int numUsers, Date conversationStart) {
		this.numUsers = numUsers;
		this.conversationStart = conversationStart;
		this.conversation_users = new ArrayList<>();
		this.messages = new ArrayList<>();
	}
	
	public List<User> getUsers(){
		return conversation_users;
	}
	
	public void addUser(User user) {
		conversation_users.add(user);
	}
	public List<Message> getMessages(){
		return messages;
	}
	public void addMessage(Message message) {
		messages.add(message);
	}
	public List<Message> getUserMessages(User user){
		return messages.stream()
				.filter(message -> message.getSender().equals(user))
				.collect(Collectors.toList());
	}
	public Long getId() {
		return conversation_id;
	}

	public void setId(Long id) {
		this.conversation_id = id;
	}

	public int getNumUsers() {
		return numUsers;
	}

	public void setNumUsers(int numUsers) {
		this.numUsers = numUsers;
	}

	public Date getConversationStart() {
		return conversationStart;
	}

	public void setConversationStart(Date conversationStart) {
		this.conversationStart = conversationStart;
	}

	@Override
	public int hashCode() {
		return Objects.hash(conversation_id, conversationStart, numUsers, conversation_users, messages);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Conversation other = (Conversation) obj;
		return Objects.equals(conversation_id, other.conversation_id) && Objects.equals(conversationStart, other.conversationStart) &&
				Objects.equals(numUsers, other.numUsers) && Objects.equals(conversation_users, other.conversation_users) 
				&& Objects.equals(messages, other.messages);
	}

	@Override
	public String toString() {
		return "Conversation [conversation_id=" + conversation_id + ", numUsers=" + numUsers + ", conversation_users="
				+ conversation_users + ", messages=" + messages + ", conversationStart=" + conversationStart + "]";
	}


}
