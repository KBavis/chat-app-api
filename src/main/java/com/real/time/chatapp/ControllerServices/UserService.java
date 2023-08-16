package com.real.time.chatapp.ControllerServices;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.real.time.chatapp.DTO.UserDTO;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Exception.UserNotFoundException;
import com.real.time.chatapp.Repositories.MessageRepository;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final MessageRepository messageRepository;

	/**
	 * Fetch All Users
	 * 
	 * @return
	 */
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	/**
	 * Fetch A User By ID
	 * 
	 * @param id
	 * @return
	 */
	public User getUserById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	/**
	 * Fetch A User By Full Name
	 * 
	 * @param name
	 * @return
	 */
	public List<User> searchUserByName(String name) {
		String[] firstAndLast = name.split(" ");
		return userRepository.searchUsersByName(firstAndLast[0], firstAndLast[1]);
	}

	/**
	 * Fetch A User By Username
	 * 
	 * @param userName
	 * @return
	 */
	public List<User> searchUserByUsername(String userName) {
		return userRepository.searchUsersByUserName(userName);
	}

	/**
	 * Update a User
	 * 
	 * @param id
	 * @param userDTO
	 * @return
	 */
	public User updateUser(Long id, UserDTO userDTO) {
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

		System.out.println("Validate User: " + validateUser(user));
		if (!validateUser(user)) {
			throw new UnauthorizedException(user);
		}

		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		user.setPassword(userDTO.getPassword());
		user.setRole(userDTO.getRole());
		user.setUserName(userDTO.getUsername());
		return userRepository.save(user);
	}

	/**
	 * Delete a User
	 * 
	 * @param id
	 */
	public void deleteUser(Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

		if (!validateUser(user)) {
			throw new UnauthorizedException(user);
		}

		deleteMessageAndRemoveRecipients(user.getSentMessages());
		userRepository.deleteById(id);
	}

	/**
	 * Helper Function to Delete a Message and Remove Recipients
	 * 
	 * @param list
	 */
	public void deleteMessageAndRemoveRecipients(List<Message> list) {
		if (list != null) {
			for (Message msg : list) {
				Set<User> recipients = msg.getRecipients();
				for (User current : recipients) {
					current.getRecievedMessages().remove(msg);
				}
				messageRepository.deleteById(msg.getMessage_id());
			}
		}
	}

	/**
	 * Helper Function To Validate A User
	 * TODO: Update Below Functionality to Check if AUTHENTICATED USER is Admin, Not User Being Passed In
	 * 
	 * @param user
	 * @return
	 */
	public boolean validateUser(User user) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String authenticatedUsername = authentication.getName().trim();

		if (user.getRole() != Role.ADMIN && !user.getUsername().trim().equals(authenticatedUsername)) {
			return false;
		}

		// Get Authenticated User
//        User authUser = userRepository.findByUserName(authenticatedUsername).orElseThrow(() -> new UserNotFoundException(authenticatedUsername));
//		if(authUser.getRole() != Role.ADMIN && !user.getUsername().trim().equals(authenticatedUsername)) {
//			return false;
//		}
		return true;
	}
}
