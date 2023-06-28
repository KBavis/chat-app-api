package com.real.time.chatapp.Rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.real.time.chatapp.Entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long>{
	
}
