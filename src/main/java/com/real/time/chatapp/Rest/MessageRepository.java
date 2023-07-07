package com.real.time.chatapp.Rest;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.real.time.chatapp.Entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long>{
	
	@Query("SELECT m FROM Message m WHERE m.content LIKE %:content%") 
	List<Message> findMessagesByContent(String content);
	
	@Query("SELECT m FROM Message m WHERE m.sendDate >= :date")
	List<Message> findMessagesByDate(Date date);
	
	@Query("SELECT m FROM Message m WHERE m.isRead = false")
	List<Message> findMessageByIsRead();
}
