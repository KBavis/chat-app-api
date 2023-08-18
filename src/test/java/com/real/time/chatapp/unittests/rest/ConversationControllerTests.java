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

import java.util.Date;
import java.util.List;
import java.util.Optional;
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

import com.real.time.chatapp.Assemblers.ConversationModelAssembler;
import com.real.time.chatapp.ControllerServices.ConversationService;
import com.real.time.chatapp.Controllers.ConversationController;
import com.real.time.chatapp.DTO.ConversationDTO;
import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Repositories.ConversationRepository;

import jakarta.transaction.Transactional;

/**
 * Unit Tests for Conversation Controller
 * 
 * @author bavis
 *
 */
@SpringBootTest
@DirtiesContext
public class ConversationControllerTests {

	@Mock
	private ConversationService conversationService;

	@Mock
	private ConversationModelAssembler converationAssembler;
	
	@Mock
	private ConversationRepository conversationRepository;

	@InjectMocks
	private ConversationController conversationController;

	List<Conversation> mockConversations;
	Conversation conversation1;
	Conversation conversation2;
	EntityModel<Conversation> mockEntityModel1;
	EntityModel<Conversation> mockEntityModel2;
	Link conversation1Link;
	Link conversation2Link;
	Link conversationAllLink;

	@BeforeEach
	void setUp() {
		// Prepare mock data
		conversation1 = new Conversation();
		conversation1.setConversation_id(1L);
		conversation2 = new Conversation();
		conversation2.setConversation_id(2L);
		mockConversations = List.of(conversation1, conversation2);

		// Mock ConversationModelAssembler
		mockEntityModel1 = mockEntityModel(conversation1);
		mockEntityModel2 = mockEntityModel(conversation2);
		when(converationAssembler.toModel(mockConversations.get(0))).thenReturn(mockEntityModel1);
		when(converationAssembler.toModel(mockConversations.get(1))).thenReturn(mockEntityModel2);

		// Set Up Links To Compare to Entity Models
		conversation1Link = linkTo(methodOn(ConversationController.class).one(conversation1.getConversation_id()))
				.withSelfRel();
		conversation2Link = linkTo(methodOn(ConversationController.class).one(conversation1.getConversation_id()))
				.withSelfRel();
		conversationAllLink = linkTo(methodOn(ConversationController.class).all()).withSelfRel();
	}

	@Test
	void test_allConversations_isSuccesful() {

		// Mocking Conversation Service
		when(conversationService.getAllConversations()).thenReturn(mockConversations);

		// Act
		CollectionModel<EntityModel<Conversation>> response = conversationController.all();

		// Assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).getAllConversations();
		verify(converationAssembler, times(2)).toModel(any(Conversation.class));
	}

	@Test
	void test_allConversations_returnsUnauthorized() {
		when(conversationService.getAllConversations()).thenThrow(new UnauthorizedException(new User()));

		// Act and Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			conversationController.all();
		});
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Unauthorized access", exception.getReason());
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).getAllConversations();
	}

	@Test
	void test_userConversations_isSuccesful() {
		// Mocking Conversation Service
		when(conversationService.getAllUserConversations()).thenReturn(mockConversations);

		// Act
		CollectionModel<EntityModel<Conversation>> response = conversationController.getConversationByUser();

		// Assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).getAllUserConversations();
		verify(converationAssembler, times(2)).toModel(any(Conversation.class));
	}

	@Test
	void test_getConversationById_isSuccesful() {
		// Mock
		when(conversationService.getConversationById(1L)).thenReturn(conversation1);

		// Act
		EntityModel<Conversation> entityModel = conversationController.one(1L);

		// Assert
		assertNotNull(entityModel);
		assertTrue(entityModel.getLinks().hasLink(conversation1Link.getRel()));
		assertTrue(entityModel.getLinks().hasLink(conversationAllLink.getRel()));
		assertEquals(entityModel.getContent(), conversation1);
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).getConversationById(1L);
		verify(converationAssembler, times(1)).toModel(conversation1);

	}

	@Test
	void test_getConversationById_returnsUnauthorized() {
		// Mock
		when(conversationService.getConversationById(1L)).thenThrow(new UnauthorizedException(new User()));

		// Act
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			conversationController.one(1L);
		});

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Unauthorized access", exception.getReason());
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).getConversationById(1L);
	}

	@Test
	void test_searchConversationByDate_isSuccesful() {
		// Mock
		when(conversationService.searchConversationsByDate(any())).thenReturn(mockConversations);

		// Act
		CollectionModel<EntityModel<Conversation>> response = conversationController
				.findConversationsByDate(new Date());

		// Assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).searchConversationsByDate(any());
		verify(converationAssembler, times(2)).toModel(any(Conversation.class));
	}

	@Test
	void test_searchConversationWithUser_isSuccesful() {
		// Mock
		when(conversationService.searchConversationsWithUser(1L)).thenReturn(mockConversations);

		// Act
		CollectionModel<EntityModel<Conversation>> response = conversationController.findConversationsWithUser(1L);

		// Assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).searchConversationsWithUser(1L);
		verify(converationAssembler, times(2)).toModel(any(Conversation.class));
	}

	@SuppressWarnings("static-access")
	@Test
	@Transactional
	void test_createConversation_isSuccesful() {
		//Mock 
		when(conversationService.createConversation(2L)).thenReturn(conversation1);
		
		//Act
		ResponseEntity<?> responseEntity = conversationController.createConversationBetweenUsers(2L);
		
		//Assert
		validateResponse(responseEntity);
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).createConversation(2L);
		verify(converationAssembler, times(1)).toModel(conversation1);
	}

	@Test
	@Transactional
	void test_updateConversation_isSuccesful() {
		//Mock
		ConversationDTO dto = new ConversationDTO();
		when(conversationService.updateConversation(1L, dto)).thenReturn(conversation1);
		
		//Act
		ResponseEntity<?> responseEntity = conversationController.updateConversation(1L, dto);
		
		//Assert
		validateResponse(responseEntity);
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).updateConversation(1L, dto);
		verify(converationAssembler, times(1)).toModel(conversation1);
	}

	@Test
	@Transactional
	void test_updateConversation_returnsUnauthorized() {
		//Mock
		ConversationDTO dto = new ConversationDTO();
		when(conversationService.updateConversation(1L,dto)).thenThrow(new UnauthorizedException(new User()));
		
		//Act
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			conversationController.updateConversation(1L, dto);
		});
		
		assertEquals(exception.getStatusCode(), HttpStatus.UNAUTHORIZED);
		assertEquals(exception.getReason(), "Unauthorized access");
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).updateConversation(1L, dto);
	}

	@Test
	@Transactional
	void test_addUserToConversation_isSuccesful() {
		//Mock
		when(conversationService.addUserToConversation(1L, 2L)).thenReturn(conversation1);
		
		//Act
		ResponseEntity<?> responseEntity = conversationController.addUserToConversation(1L, 2L);
		
		//Assert
		validateResponse(responseEntity);
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).addUserToConversation(1L, 2L);
		verify(converationAssembler, times(1)).toModel(conversation1);
	}

	@Test
	@Transactional
	void test_addUserToConversation_returnsUnauthorized() {
		//Mock
		when(conversationService.addUserToConversation(1L, 2l)).thenThrow(new UnauthorizedException(new User()));
		
		//Act
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			conversationController.addUserToConversation(1L, 2L);
		});
		
		//Assert
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals(exception.getReason(), "Unauthorized access");
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).addUserToConversation(1L, 2L);
	}

	@Test
	@Transactional
	void test_leaveConversation_isSuccesful() {
		//Mock 
	    when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation1));
	    
	    //Act
	    ResponseEntity<?> responseEntity = conversationController.leaveConversation(1L);
	    
	    //Assert
	    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	    
	}

	@Test
	@Transactional
	void test_leaveConversation_returnsUnauthorized() {
		//Mock
		 doThrow(new UnauthorizedException(new User())).when(conversationService).leaveConversation(1L);
		 
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			conversationController.leaveConversation(1L);
		});
		
		//Assert
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals(exception.getReason(), "Unauthorized access");
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).leaveConversation(1L);
	}

	@Test
	@Transactional
	void test_deleteConversation_isSuccesful() {
		//Act
		ResponseEntity<?> responseEntity = conversationController.deleteConversation(1L);
		
	    //Assert
	    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	}

	@Test
	@Transactional
	void test_deleteConversation_returnsUnauthorized() {
		//Mock
		doThrow(new UnauthorizedException(new User())).when(conversationService).deleteConversation(1L);
		
		//Act
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			conversationController.deleteConversation(1L);
		});
		
		//Assert
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals(exception.getReason(), "Unauthorized access");
		
		//Ensure Stubbed Methods Are Called
		verify(conversationService, times(1)).deleteConversation(1L);
		
	}
	
	void validateResponse(ResponseEntity<?> responseEntity) {
		assertNotNull(responseEntity);
		assertEquals(responseEntity.getStatusCode(), HttpStatus.CREATED);
		assertEquals(responseEntity.getBody(), mockEntityModel1);
		
		//Ensure the Response Entity Model Has Proper Link
		EntityModel<?> responseEntityModel = (EntityModel<?>) responseEntity.getBody();
		assertTrue(responseEntityModel.hasLink(IanaLinkRelations.SELF));
		assertEquals(responseEntityModel.getRequiredLink(IanaLinkRelations.SELF).getHref(), conversation1Link.getHref());
	}

	/**
	 * Helper Function to Validate Collection Model and Corresponding EntityModels
	 * 
	 * @param response
	 */
	void validateModel(CollectionModel<EntityModel<Conversation>> response) {
		// Validate CollectionModel
		assertNotNull(response);
		assertTrue(response.getLinks().hasLink(conversationAllLink.getRel()));

		// Extract EntityModels from CollectionModel
		List<EntityModel<Conversation>> entityModels = response.getContent().stream().collect(Collectors.toList());

		// Ensure Our Collectiton Model Has Our Two Mocked Entity Models
		assertNotNull(entityModels);
		assertEquals(2, entityModels.size());

		// Ensure Our EntityModels Have Link To Their Self And All
		assertTrue(entityModels.get(0).getLinks().hasLink(conversationAllLink.getRel()));
		assertTrue(entityModels.get(0).getLinks().hasLink(conversation1Link.getRel()));
		assertTrue(entityModels.get(1).getLinks().hasLink(conversationAllLink.getRel()));
		assertTrue(entityModels.get(1).getLinks().hasLink(conversation2Link.getRel()));

		// Ensure The EntityModels Content Is Equal To Corresponding Conversation
		assertEquals(entityModels.get(0).getContent(), conversation1);
		assertEquals(entityModels.get(1).getContent(), conversation2);
	}
	
	/**
	 * Helper Method to Mock ConversationAssembler
	 * 
	 * @param conversation
	 * @return
	 */
	EntityModel<Conversation> mockEntityModel(Conversation conversation) {
		return EntityModel.of(conversation,
				linkTo(methodOn(ConversationController.class).one(conversation.getConversation_id())).withSelfRel(),
				linkTo(methodOn(ConversationController.class).all()).withRel("/conversations"));
	}
}
