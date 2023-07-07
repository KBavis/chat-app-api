package com.real.time.chatapp.Entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author bavis
 * 
 *         User Entity for Login Verification
 */

@Entity
@Table(name = "user_table")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long user_id;
	private String userName;
	private String firstName;
	private String lastName;
	private String password;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_conversations",
	joinColumns = {
			@JoinColumn(name = "user_id", referencedColumnName = "user_id")
	},
	inverseJoinColumns = {
			@JoinColumn(name = "conversation_id", referencedColumnName = "conversation_id")
	})
	private Set<Conversation> list_conversations = new HashSet<>();

//	One User Can Be Inolved In Many Sent Messages
//	MappedBy means we are referencing the 'User sender' field in User table
//	CascadeType.REMOVE to remove any messages sent by this user
	@OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE , orphanRemoval = true)
	private List<Message> sentMessages;

	// One User Can Be Involved In Many Recieved Messages
	// MappedBy means we are referencing the 'User recipient' field in User Table
	@OneToMany(mappedBy = "recipient", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Message> recievedMessages;
	

	@PreRemove
	private void preRemove() {
		//Delete all message sent by this user
		for(Conversation convo: list_conversations) {
			convo.getConversation_users().remove(this);
		}
	}

	

}
