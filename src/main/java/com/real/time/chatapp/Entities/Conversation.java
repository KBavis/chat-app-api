package com.real.time.chatapp.Entities;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversation {
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
	 * CascadeType.REMOVE indicates that when a Converesation is dleeted, all related Messages associated with that Conversation will be removed 
	 */
	@OneToMany(mappedBy = "conversation", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Message> messages;

	/**
	 *  Used to specify callback method that is executed before Conversation is removed 
	 *  Manually remove the Conversation from the assoicated Users
	 */
	@PreRemove
	private void preRemove() {
		for(User user: conversation_users) {
			user.getList_conversations().remove(this);
		}
	}

}
