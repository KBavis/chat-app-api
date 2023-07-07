package com.real.time.chatapp.Rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.real.time.chatapp.Entities.User;

@Repository
interface UserRepository extends JpaRepository<User,Long>{
	
	@Query("SELECT u FROM User u WHERE u.firstName = :first AND u.lastName = :last")
	List<User> searchUsersByName(String first, String last);
	
	
	@Query("SELECT u FROM User u WHERE u.userName = :user")
	List<User> searchUsersByUserName(String user);
}