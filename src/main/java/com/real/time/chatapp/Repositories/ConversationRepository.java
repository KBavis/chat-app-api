package com.real.time.chatapp.Repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation,Long>{
	
	/**
	 * Fetch conversations by date 
	 * @param date
	 * @return
	 */
	@Query("SELECT c FROM Conversation c WHERE c.conversationStart >= :date")
	List<Conversation> findConversationsByDate(Date date);
	
	/**
	 * Fetch conversations by User within it
	 * @param id
	 * @return
	 */
	@Query("SELECT c FROM Conversation c WHERE :user MEMBER OF c.conversation_users")
	List<Conversation> findConversationsByUser(User user);
	
}
