package com.real.time.chatapp.Entities;



import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class Message {
	private @Id @GeneratedValue @JsonProperty("message_id") Long message_id;
	private boolean isRead;
	private Date sendDate = new Date(); 
	private String content;
	
	@ManyToMany(mappedBy = "recievedMessages", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private Set<User> recipients;
	
	/**
	 *  Many Messages to One User
	 *  Join Column states our column in Message table will be called 'sender_id' 
	 */
	@ManyToOne
	@JoinColumn(name = "sender_user_id", referencedColumnName ="user_id")
	private User sender;
	
    /**
     *  Many Messages To One Conversations
     */
	@ManyToOne
	@JoinColumn(name = "conversation_id", referencedColumnName = "conversation_id")
	private Conversation conversation;
	
	@PreRemove
	private void preRemove() {
		for(User user: recipients) {
			user.getRecievedMessages().remove(this);
		}
	}
	
	@Override
	public String toString() {
		return "Message [message_id=" + message_id + ", isRead=" + isRead + ", sendDate=" + sendDate + ", content="
				+ content + ", recipients=" + recipients + ", sender=" + sender + ", conversation=" + conversation
				+ "]";
	}

	

	
}
