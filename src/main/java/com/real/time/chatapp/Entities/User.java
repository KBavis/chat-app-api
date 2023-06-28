package com.real.time.chatapp.Entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * 
 * @author bavis
 * 
 *         User Entity for Login Verification
 */

@Entity
@Table(name = "user_table")
public class User {
	// TODO: Enable search capabilities of a user

	private @Id @GeneratedValue Long user_id;
	private String userName;
	private String firstName;
	private String lastName;
	private String password;

	/**
	 * Many Users Can Be Involved In Many Conversations **NOTE**: IN a ManyToMany
	 * relationship, the owner of the relationship should specify a join table in
	 * this join table, you have join columns (the primary key of the owner
	 * relation) and you ahve inverseJoinColumns (the primary key of the other
	 * relation):: The Non-Owner Relation should indicate that it is mapped by 
	 * the ManyToMany field in the Owner Relation
	 * 
	 */

	
	@ManyToMany
	@JoinTable(name = "user_conversations",
	joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "conversation_id"))
	private List<Conversation> list_conversations;

//	One User Can Be Inolved In Many Sent Messages
//	MappedBy means we are referencing the 'User sender' field in User table
	@OneToMany(mappedBy = "sender", cascade = CascadeType.MERGE)
	private List<Message> sentMessages;

	// One User Can Be Involved In Many Recieved Messages
	// MappedBy means we are referencing the 'User recipient' field in User Table
	@OneToMany(mappedBy = "recipient", cascade = CascadeType.MERGE)
	private List<Message> recievedMessages;

	// TODO: Data persists, so delete records in database that we arent using after
	// testing
	public User() {
		this.list_conversations = new ArrayList<>();
		this.sentMessages = new ArrayList<>();
		this.recievedMessages = new ArrayList<>();
	}

	public User(String userName, String firstName, String lastName, String password) {
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.list_conversations = new ArrayList<>();
		this.sentMessages = new ArrayList<>();
		this.recievedMessages = new ArrayList<>();
	}

	public Long getId() {
		return user_id;
	}

	public void setId(Long id) {
		this.user_id = id;
	}

	public String getUserName() {
		return userName;
	}

	public List<Conversation> getConversations() {
		return list_conversations;
	}
	public void addConversation(Conversation conversation) {
		list_conversations.add(conversation);
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSentMessage(Message message) {
		this.sentMessages.add(message);
	}

	public List<Message> getSentMessages() {
		return sentMessages;
	}
	
	public void setSentMessages(List<Message> sentMessages) {
		this.sentMessages = sentMessages;
	}
	
	public void setRecievedMessages(List<Message> recievedMessges) {
		this.recievedMessages = recievedMessages;
	}
	
	public void setRecievedMessage(Message message) {
		this.recievedMessages.add(message);
	}

	public List<Message> getRecievedMessages() {
		return recievedMessages;
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstName, user_id, lastName, password, userName, sentMessages, recievedMessages, list_conversations);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(firstName, other.firstName) && Objects.equals(user_id, other.user_id)
				&& Objects.equals(lastName, other.lastName) && Objects.equals(password, other.password)
				&& Objects.equals(userName, other.userName)
				&& Objects.equals(recievedMessages, other.recievedMessages)
				&& Objects.equals(sentMessages, other.sentMessages)
				&& Objects.equals(list_conversations, other.list_conversations);
	}

	@Override
	public String toString() {
		return "User [user_id=" + user_id + ", userName=" + userName + ", firstName=" + firstName + ", lastName="
				+ lastName + ", password=" + password + ", recievedMessages=" + recievedMessages + ", sentMessages=" +
				sentMessages + ", list_conversations=" + list_conversations + "]";
	}

}
