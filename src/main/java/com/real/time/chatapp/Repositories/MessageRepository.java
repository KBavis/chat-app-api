package com.real.time.chatapp.Repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long>{
	
	@Query("SELECT m FROM Message m WHERE :user MEMBER OF m.recipients")
	List<Message> findMessagesByUser(User user);
	
	@Query("SELECT m FROM Message m WHERE m.content LIKE %:content% AND :user MEMBER OF m.recipients OR :user = m.sender") 
	List<Message> findMessagesByContent(String content, User user);
	
	@Query("SELECT m FROM Message m WHERE m.sendDate >= :date AND :user MEMBER OF m.recipients OR :user = m.sender")
	List<Message> findMessagesByDate(Date date, User user);
	
	@Query("SELECT m FROM Message m WHERE m.isRead = false AND :user MEMBER OF m.recipients")
	List<Message> findMessageByIsRead(User user);
}
