package com.real.time.chatapp.unittests.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import com.real.time.chatapp.Assemblers.UserModelAssembler;
import com.real.time.chatapp.ControllerServices.UserService;
import com.real.time.chatapp.Controllers.UserController;
import com.real.time.chatapp.DTO.UserDTO;
import com.real.time.chatapp.DTO.UserResponseDTO;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Repositories.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Unit Tests for User Controller 
 * 
 * @author bavis
 *
 */
@SpringBootTest
@DirtiesContext
public class UserControllerTests {
	
	@Mock
	private UserService userService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock 
	private UserModelAssembler userAssembler;
	
	@InjectMocks
	private UserController userController;
	
	User user1;
	User user2;
	List<User> mockUsers;
	EntityModel<UserResponseDTO> mockEntityModel1; 
	EntityModel<UserResponseDTO> mockEntityModel2; 
	Link user1Link;
	Link user2Link;
	Link userAllLink;
	
	@BeforeEach
	@Transactional
	void setUp() {
		// Prepare mock data
		user1 = new User();
		user1.setUser_id(1L);
		user2 = new User();
		user2.setUser_id(2L);
		mockUsers = List.of(user1, user2);

		// Mock ConversationModelAssembler
		mockEntityModel1 = mockEntityModel(user1);
		mockEntityModel2 = mockEntityModel(user2);
		when(userAssembler.toModel(mockUsers.get(0))).thenReturn(mockEntityModel1);
		when(userAssembler.toModel(mockUsers.get(1))).thenReturn(mockEntityModel2);

		// Set Up Links To Compare to Entity Models
		user1Link = linkTo(methodOn(UserController.class).one(user1.getUser_id()))
				.withSelfRel();
		user2Link = linkTo(methodOn(UserController.class).one(user2.getUser_id()))
				.withSelfRel();
		userAllLink = linkTo(methodOn(UserController.class).all()).withSelfRel();
	}
	
	@Test
	@Transactional
	void test_getAllUsers_isSuccesfull() {
		//Mock
		when(userService.getAllUsers()).thenReturn(mockUsers);
		
		//Act
		CollectionModel<EntityModel<UserResponseDTO>> response = userController.all();
		
		//Assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(userService, times(1)).getAllUsers();
		verify(userAssembler, times(2)).toModel(any(User.class));
	}
	
	@Test
	@Transactional
	void test_getUserById_isSuccesful() {
		//Mock
		when(userService.getUserById(1L)).thenReturn(user1);
		
		//Act
		EntityModel<UserResponseDTO> entityModel = userController.one(1L);
		
		//Assert
		assertNotNull(entityModel);
		assertTrue(entityModel.getLinks().hasLink(user1Link.getRel()));
		assertTrue(entityModel.getLinks().hasLink(userAllLink.getRel()));
		assertEquals(entityModel.getContent().getUser_id(), user1.getUser_id());
		
		//Ensure Stubbed Methods Are Called
		verify(userService, times(1)).getUserById(1L);
		verify(userAssembler, times(1)).toModel(user1);
	}
	
	@Test
	@Transactional
	void void_testSearchUsersByName_isSuccesful() {
		//Mock
		when(userService.searchUserByName(any(String.class))).thenReturn(mockUsers);
		
		//Act
		CollectionModel<EntityModel<UserResponseDTO>> response = userController.searchUsersByName("Name");
		
		//Assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(userService, times(1)).searchUserByName(any(String.class));
		verify(userAssembler, times(2)).toModel(any(User.class));
	}
	
	@Test
	@Transactional
	void test_searchUsersByUserName_isSuccesful() {
		//Mock
		when(userService.searchUserByUsername(any())).thenReturn(mockUsers);
		
		//Act
		CollectionModel<EntityModel<UserResponseDTO>> response = userController.searchUsersByUserName("Name");
		
		//Assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(userService, times(1)).searchUserByUsername(any());
		verify(userAssembler, times(2)).toModel(any(User.class));
	}
	
	@Test
	@Transactional
	void test_updateUser_isSuccesful() {
		//Mock
		UserDTO userDTO = new UserDTO();
		when(userService.updateUser(1L,userDTO)).thenReturn(user1);
		
		//Act
		ResponseEntity<?> responseEntity = userController.updateUser(userDTO, 1L);
		
		//Assert
		validateResponse(responseEntity);
		
		//Ensure Stubbed Methods Are Called
		verify(userService, times(1)).updateUser(1L,userDTO);
		verify(userAssembler, times(1)).toModel(user1);
	}
	
	@Test
	@Transactional
	void test_updateUser_returnsUnauthorized() {
		//Mock
		UserDTO userDTO = new UserDTO();
		when(userService.updateUser(1L, userDTO)).thenThrow(new UnauthorizedException(new User()));
		
		
		//Act
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			userController.updateUser(userDTO, 1L);
		});
		
		//Assert
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals(exception.getReason(), "Unauthorized access");
		
		//Ensure Stubbed Methods Are Called
		verify(userService, times(1)).updateUser(1L,userDTO);
	}
	
	@Test
	@Transactional
	void test_deleteUser_isSuccesful() {
		//Act
		ResponseEntity<?> responseEntity = userController.deleteUser(1L);
		
	    //Assert
	    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	    
		//Ensure Stubbed Methods Are Called
		verify(userService, times(1)).deleteUser(1L);
	}
	
	@Test
	@Transactional
	void test_deleteUser_isUnauthorized() {
		//Mock
		 doThrow(new UnauthorizedException(new User())).when(userService).deleteUser(1L);
		 
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			userController.deleteUser(1L);
		});
		
		
		//Assert
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals(exception.getReason(), "Unauthorized access");
		
		//Ensure Stubbed Methods Are Called
		verify(userService, times(1)).deleteUser(1L);
	}
	
	
	/**
	 * Helper Function to Validate Collection Model and Corresponding EntityModels
	 * 
	 * @param response
	 */
	void validateModel(CollectionModel<EntityModel<UserResponseDTO>> response) {
		// Validate CollectionModel
		assertNotNull(response);
		assertTrue(response.getLinks().hasLink(userAllLink.getRel()));

		// Extract EntityModels from CollectionModel
		List<EntityModel<UserResponseDTO>> entityModels = response.getContent().stream().collect(Collectors.toList());

		// Ensure Our Collectiton Model Has Our Two Mocked Entity Models
		assertNotNull(entityModels);
		assertEquals(2, entityModels.size());

		// Ensure Our EntityModels Have Link To Their Self And All
		assertTrue(entityModels.get(0).getLinks().hasLink(userAllLink.getRel()));
		assertTrue(entityModels.get(0).getLinks().hasLink(user1Link.getRel()));
		assertTrue(entityModels.get(1).getLinks().hasLink(userAllLink.getRel()));
		assertTrue(entityModels.get(1).getLinks().hasLink(user2Link.getRel()));

		// Ensure The EntityModels Content Is Equal To Corresponding Conversation
		assertEquals(entityModels.get(0).getContent().getUser_id(), user1.getUser_id());
		assertEquals(entityModels.get(1).getContent().getUser_id(), user2.getUser_id());
	}
	
	
	/**
	 * Helper Method To Validate Response Enitity
	 * 
	 * @param responseEntity
	 */
	void validateResponse(ResponseEntity<?> responseEntity) {
		assertNotNull(responseEntity);
		assertEquals(responseEntity.getStatusCode(), HttpStatus.CREATED);
		assertEquals(responseEntity.getBody(), mockEntityModel1);
		
		//Ensure the Response Entity Model Has Proper Link
		EntityModel<?> responseEntityModel = (EntityModel<?>) responseEntity.getBody();
		assertTrue(responseEntityModel.hasLink(IanaLinkRelations.SELF));
		assertEquals(responseEntityModel.getRequiredLink(IanaLinkRelations.SELF).getHref(), user1Link.getHref());
	}
	
	/**
	 * Helper Method to Mock ConversationAssembler
	 * 
	 * @param conversation
	 * @return
	 */
	EntityModel<UserResponseDTO> mockEntityModel(User user) {
		UserResponseDTO responseDTO = new UserResponseDTO();
		responseDTO.setUser_id(user.getUser_id());
		responseDTO.setUserName(user.getUsername());
		responseDTO.setFirstName(user.getFirstName());
		responseDTO.setLastName(user.getLastName());
		responseDTO.setPassword(user.getPassword());
		responseDTO.setRole(user.getRole());
		responseDTO.setList_conversations(user.getList_conversations());
		responseDTO.setSentMessges(user.getSentMessages());
		responseDTO.setRecievedMessages(user.getRecievedMessages());
		return EntityModel.of(responseDTO,
			linkTo(methodOn(UserController.class).one(user.getUser_id())).withSelfRel(),
			linkTo(methodOn(UserController.class).all()).withRel("/users"));
	}
}
