package com.real.time.chatapp.ControllerServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.ConversationNotFoundException;
import com.real.time.chatapp.Exception.MessageNotFoundException;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.ConversationRepository;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;
	private final ConversationRepository conversationRepository;
	private final UserRepository userRepository;

	/**
	 * Get All Messages Sent on Application
	 * 
	 * @return
	 */
	public List<Message> getAllMessages() {
		if (!validateAdmin()) {
			throw new UnauthorizedException(getUser());
		}
		return messageRepository.findAll();
	}

	/**
	 * Get All Of Authenticated Users Messages
	 * 
	 * @return
	 */
	public List<Message> getAllUserMessages() {
		return messageRepository.findMessagesByUser(getUser());
	}

	/**
	 * Get Message By Id
	 * 
	 * @param id
	 * @return
	 */
	public Message getMessageById(Long id) {
		Message msg = messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
		User user = getUser();
		if (!canAccessMessage(msg, user)) {
			throw new UnauthorizedException(user);
		}
		return msg;
	}

	private boolean canAccessMessage(Message msg, User user) {
		List<Message> sentMessages = user.getSentMessages();
		Set<Message> recievedMessages = user.getRecievedMessages();
		return sentMessages.contains(msg) || recievedMessages.contains(msg);
	}

	/**
	 * Get Message In Specific Converastion
	 * 
	 * @param id
	 * @return
	 */
	public List<Message> getConversationMessages(Long id) {
		Conversation conversation = conversationRepository.findById(id)
				.orElseThrow(() -> new ConversationNotFoundException(id));

		if (!validateUserConversation(conversation)) {
			throw new UnauthorizedException(getUser());
		}

		return conversation.getMessages();
	}

	/**
	 * Fetch Messages By Given Content
	 * 
	 * @param content
	 * @return
	 */
	public List<Message> searchMessagesByContent(String content) {
		return messageRepository.findMessagesByContent(content, getUser());
	}

	/**
	 * Fetch Messages By Given Date
	 * 
	 * @param content
	 * @return
	 */
	public List<Message> searchMessagesByDate(Date date) {
		return messageRepository.findMessagesByDate(date, getUser());
	}

	/**
	 * Fetch Messages By If They Haven't Been Read
	 * 
	 * @param content
	 * @return
	 */
	public List<Message> searchMessagesByRead() {
		return messageRepository.findMessageByIsRead(getUser());
	}

	/**
	 * Create New Message
	 * 
	 * @param messageDTO
	 * @param conversationID
	 */
	public Message createMessage(MessageDTO messageDTO, Long conversationID) {
		User sender = getUser();
		Conversation conversation = conversationRepository.findById(conversationID)
				.orElseThrow(() -> new ConversationNotFoundException(conversationID));

		if (!conversation.getConversation_users().contains(sender)) {
			throw new UnauthorizedException(sender);
		}

		// Set Recipients of Message As All Conversation Users Other Than Sender
		Set<User> conversationUsers = conversation.getConversation_users();
		Set<User> recipients = new HashSet<>();
		for (User recipient : conversationUsers) {
			if (recipient != sender) {
				recipients.add(recipient);
			}
		}

		// Create Message
		Message message = new Message();
		message.setContent(messageDTO.getContent());
		message.setSendDate(new Date());
		message.setRead(false);
		message.setRecipients(recipients);
		message.setSender(sender);
		message.setConversation(conversation);

		System.out.println("Message Generated");

		// Add Messages to List of Messages For The Current Conversation
		List<Message> conversationMessages = conversation.getMessages();
		if (conversationMessages == null)
			conversationMessages = new ArrayList<>();
		conversationMessages.add(message);
		conversation.setMessages(conversationMessages);

		// Add Sent Messages to the List of Sender's Sent Messages
		List<Message> sentMessages = sender.getSentMessages();
		if (sentMessages == null)
			sentMessages = new ArrayList<>();
		sentMessages.add(message);
		sender.setSentMessages(sentMessages);

		// Add Recieved Messages to the List of Each Recievers Recieved Messages
		for (User recipient : recipients) {
			Set<Message> recievedMessages = recipient.getRecievedMessages();
			recievedMessages.add(message);
			recipient.setRecievedMessages(recievedMessages);
		}

		return messageRepository.save(message);
	}

	/**
	 * Update A Message
	 * 
	 * @param newMessage
	 * @param id
	 * @return
	 */
	public Message updateMessage(MessageDTO newMessage, Long id) {
		if (!validateMessage(id)) {
			throw new UnauthorizedException(getUser());
		}
		Message updatedMessage = messageRepository.findById(id).map(message -> {
			message.setContent(newMessage.getContent());
			message.setSendDate(newMessage.getSendDate());
			return messageRepository.save(message);
		}).orElseGet(() -> {
			Message msg = new Message();
			msg.setMessage_id(id);
			msg.setContent(newMessage.getContent());
			msg.setSendDate(newMessage.getSendDate());
			return messageRepository.save(msg);
		});

		return updatedMessage;
	}

	/**
	 * Delete A Message
	 * 
	 * @param id
	 */
	public void deleteMessage(Long id) {
		if (!validateMessage(id) && !validateAdmin()) {
			throw new UnauthorizedException(getUser());
		}
		messageRepository.deleteById(id);
	}

	/**
	 * Helper Function To Determine That Authenticated User is Sender of Message
	 * 
	 * @param id
	 * @return
	 */
	public boolean validateMessage(Long id) {
		Message message = messageRepository.findById(id).orElseThrow(() -> new MessageNotFoundException(id));
		User sender = message.getSender();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userName = authentication.getName().trim();
		if (!userName.equals(sender.getUsername())) {
			return false;
		}
		return true;
	}

	/**
	 * Helper Function to Determine If User In Conversation
	 * 
	 * @param conversation
	 * @return
	 */
	public boolean validateUserConversation(Conversation conversation) {
		Set<User> users = conversation.getConversation_users();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userName = authentication.getName().trim();
		for (User user : users) {
			if (user.getUsername().equals(userName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper Function to Determine If Logged in User is an Admin
	 * 
	 * @return
	 */
	public boolean validateAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userName = authentication.getName().trim();
		var user = userRepository.findByUserName(userName).orElseThrow(() -> new UserNotFoundException(userName));
		if (user.getRole() == Role.ADMIN) {
			return true;
		}
		return false;
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

}
