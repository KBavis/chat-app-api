package com.real.time.chatapp.Entities;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
	// TODO: Enable Search Enpoints Using JpaRepository Methods
	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long conversation_id;
	private int numUsers;
	private Date conversationStart = new Date();
	
	/**
	 * Many Conversations To Many Users
	 */
	@ManyToMany(mappedBy = "list_conversations", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private Set<User> conversation_users = new HashSet<>(); 

	/**
	 * One Conversation To Many Messages
	 */
	@OneToMany(mappedBy = "conversation")
	private List<Message> messages;


}
