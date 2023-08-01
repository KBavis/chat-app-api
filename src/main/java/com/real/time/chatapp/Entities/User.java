package com.real.time.chatapp.Entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User implements UserDetails{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long user_id;
	private String userName;
	private String firstName;
	private String lastName;
	private String password;
	
	//Enumerated Tells Spring that this is an Enum
	@Enumerated(EnumType.STRING)
	private Role role;

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
	
	/**
	 *  Overrding Spring Security Interface Methods
	 */
	
	/**
	 *  We Return A List of This Users Roles (Admin or User)
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public String getUsername() {
		return userName;
	}
	
	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

}
