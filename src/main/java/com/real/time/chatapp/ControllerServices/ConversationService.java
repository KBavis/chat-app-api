package com.real.time.chatapp.ControllerServices;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.real.time.chatapp.Assemblers.ConversationModelAssembler;
import com.real.time.chatapp.DTO.ConversationDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService {

	private final ConversationRepository conversationRepository;
	private final UserRepository userRepository;
	
	/**
	 * Fetch All Conversations
	 * 
	 * @return
	 */
	public List<Conversation> getAllConversations(){
		if(!validateAdmin()) {
			throw new UnauthorizedException(getUser());
		}
		return conversationRepository.findAll();
	}
	
	/**
	 * Fetch All User Conversations
	 * 
	 * @return
	 */
	public List<Conversation> getAllUserConversations(){
		User user = getUser();
		return conversationRepository.findConversationsByUser(user);
	}
	
	/**
	 * Fetching A Conversation By ID
	 * 
	 * @param id
	 * @return
	 */
	public Conversation getConversationById(Long id) {
		User user = getUser();
		Conversation convo = conversationRepository.findById(id).orElseThrow(() -> new ConversationNotFoundException(id));
		if(!convo.getConversation_users().contains(user)) {
			throw new UnauthorizedException(user);
		}
		return convo;
	}
	
	/**
	 * Fetching Conversations By Date
	 * 
	 * @param date
	 * @return
	 */
	public List<Conversation> searchConversationsByDate(Date date){
		User user = getUser();
		return conversationRepository.findConversationsByDate(date, user);
	}
	
	/**
	 * Fetching Conversations With A Specific User
	 * 
	 * @param id
	 * @return
	 */
	public List<Conversation> searchConversationsWithUser(Long id){
		User user = userRepository.findById(id).orElse(null);
		User authenticatedUser = getUser();
		
		return conversationRepository.findConversationsByUserAndAuthUser(authenticatedUser, user);
	}
	
	/**
	 * Create Conversation Between Two Users
	 * 
	 * @param id
	 * @return
	 */
	public Conversation createConversation(Long id) {
		User userOne = getUser();
		User userTwo = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		
		Conversation conversation = new Conversation();
		if(conversation.getConversation_users() == null) conversation.setConversation_users(new HashSet<>());
		
		//Adding Users to Conversation
		conversation.getConversation_users().add(userOne);
		conversation.getConversation_users().add(userTwo);
		conversation.setNumUsers(2);
		
		//Adding This As A Conversation Each User is In
		userOne.getList_conversations().add(conversation);
		userTwo.getList_conversations().add(conversation);
		
		return conversationRepository.save(conversation);
	}
	
	/**
	 * Update A Conversation
	 * 
	 * @param id
	 * @param conversationDTO
	 * @return
	 */
	public Conversation updateConversation(Long id, ConversationDTO conversationDTO) {
		Conversation existingConversation = conversationRepository.findById(id).orElseThrow(() -> new ConversationNotFoundException(id));
		
		User user = getUser();
		if(!existingConversation.getConversation_users().contains(user)){
			throw new UnauthorizedException(user);
		}
		
		existingConversation.setNumUsers(conversationDTO.getNumUsers());
		existingConversation.setConversationStart(conversationDTO.getConversationStart());
		existingConversation.setMessages(conversationDTO.getMessages());
		
		return conversationRepository.save(existingConversation);
	}
 	
	/**
	 * Add User to Conversation
	 * 
	 * @param conversationId
	 * @param userID
	 * @return
	 */
	public Conversation addUserToConversation(Long conversationId, Long userID) {
		Conversation convo = conversationRepository.findById(conversationId).orElseThrow(() -> new ConversationNotFoundException(conversationId));
		User user = getUser();
		if(!convo.getConversation_users().contains(user)){
			throw new UnauthorizedException(user);
		}
		
		//Add user to Conversation
		User userToBeAdded = userRepository.findById(userID).orElseThrow(() -> new UserNotFoundException(userID));
		convo.getConversation_users().add(userToBeAdded);
		convo.setNumUsers(convo.getNumUsers() + 1);
		
		//Adding This As A Conversation The Added User is In
		userToBeAdded.getList_conversations().add(convo);
		
		return conversationRepository.save(convo);
	}
	
	/**
	 * Leave Conversation
	 * 
	 * @param id
	 */
	public void leaveConversation(Long id) {
		Conversation convo = conversationRepository.findById(id).orElseThrow(() -> new ConversationNotFoundException(id));
		User user = getUser();
		if(!convo.getConversation_users().contains(user)){
			throw new UnauthorizedException(user);
		}
		
		Set<Conversation> userConversation = user.getList_conversations();
		Set<User> conversationUsers = convo.getConversation_users();
		//Remove User From Conversation and Conversation From List of Conversations For User
		userConversation.remove(convo);
		conversationUsers.remove(user);
		convo.setConversation_users(conversationUsers);
		user.setList_conversations(userConversation);
		
		convo.setNumUsers(convo.getNumUsers() - 1);
		
		//Delete Conversation If No Users 
		if(convo.getNumUsers() <= 0) {
			conversationRepository.deleteById(id);
		} else {
			conversationRepository.save(convo);
		}
	}
	
	/**
	 * Delete a Conversation
	 * 
	 * @param id
	 */
	public void deleteConversation(Long id) {
		if(!validateAdmin()) {
			throw new UnauthorizedException(getUser());
		}
		conversationRepository.deleteById(id);
	}
	
	/**
	 * Helper Function to Extract Current AuthenticatedUser
	 * 
	 * @return
	 */
	public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName().trim();
        Optional<User> user = userRepository.findByUserName(userName);
        User foundUser = user.orElseThrow(() -> new UserNotFoundException(userName));
        return foundUser;
	}
	
	/**
	 * Helper Function To Determine If AuthenitcatedUser is an Admin
	 * 
	 * @return
	 */
	public boolean validateAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName().trim();
        var user = userRepository.findByUserName(userName).orElseThrow(() -> new UserNotFoundException(userName));
        if(user.getRole() == Role.ADMIN) {
        	return true;
        }
        return false;
	}
}
