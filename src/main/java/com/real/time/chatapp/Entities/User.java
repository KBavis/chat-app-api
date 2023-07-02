package com.real.time.chatapp.Entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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
	@OneToMany(mappedBy = "sender", cascade = CascadeType.PERSIST)
	private List<Message> sentMessages;

	// One User Can Be Involved In Many Recieved Messages
	// MappedBy means we are referencing the 'User recipient' field in User Table
	@OneToMany(mappedBy = "recipient", cascade = CascadeType.PERSIST)
	private List<Message> recievedMessages;

	

}
