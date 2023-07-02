package com.real.time.chatapp.Rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation,Long>{
	
//	boolean existsByIdAndUsersContaining(Long conversationId, User user);
}
