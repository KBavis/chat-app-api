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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long user_id;
	private String userName;
	private String firstName;
	private String lastName;
	private String password;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_conversations", joinColumns = {
			@JoinColumn(name = "user_id", referencedColumnName = "user_id") }, inverseJoinColumns = {
					@JoinColumn(name = "conversation_id", referencedColumnName = "conversation_id") })
	private Set<Conversation> list_conversations = new HashSet<>();

	@OneToMany(mappedBy = "sender")
	private List<Message> sentMessages;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_recieved_messages", joinColumns = {
			@JoinColumn(name = "user_id", referencedColumnName = "user_id") }, inverseJoinColumns = {
					@JoinColumn(name = "message_id", referencedColumnName = "message_id") })
	private Set<Message> recievedMessages = new HashSet<>();

	/**
	 * Used as a Callback to remove this user from any conversations upon deletion
	 */
	@PreRemove
	private void preRemove() {
		// Delete all message sent by this user
		for (Conversation convo : list_conversations) {
			convo.getConversation_users().remove(this);
		}

	}

}
