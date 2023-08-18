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

import com.real.time.chatapp.Assemblers.MessageModelAssembler;
import com.real.time.chatapp.ControllerServices.MessageService;
import com.real.time.chatapp.Controllers.ConversationController;
import com.real.time.chatapp.Controllers.MessageController;
import com.real.time.chatapp.DTO.MessageDTO;
import com.real.time.chatapp.Entities.Message;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Exception.UnauthorizedException;
import com.real.time.chatapp.Repositories.MessageRepository;

/**
 * Unit Tests for Message Controlelr 
 * 
 * @author bavis
 *
 */
@SpringBootTest
@DirtiesContext
public class MessageControllerTests {
	
	@Mock
	private MessageService messageService;

	@Mock
	private MessageModelAssembler messageAssembler;
	
	@Mock
	private MessageRepository messageRepository;

	@InjectMocks
	private MessageController messageController;

	List<Message> mockMessages;
	Message message1;
	Message message2;
	EntityModel<Message> mockEntityModel1;
	EntityModel<Message> mockEntityModel2;
	Link message1Link;
	Link message2Link;
	Link messageAllLink;
	
	@BeforeEach
	void setUp() {
		// Prepare mock data
		message1 = new Message();
		message1.setMessage_id(1L);
		message2 = new Message();
		message2.setMessage_id(2L);
		mockMessages = List.of(message1, message2);

		// Mock ConversationModelAssembler
		mockEntityModel1 = mockEntityModel(message1);
		mockEntityModel2 = mockEntityModel(message2);
		when(messageAssembler.toModel(mockMessages.get(0))).thenReturn(mockEntityModel1);
		when(messageAssembler.toModel(mockMessages.get(1))).thenReturn(mockEntityModel2);

		// Set Up Links To Compare to Entity Models
		message1Link= linkTo(methodOn(MessageController.class).one(message1.getMessage_id()))
				.withSelfRel();
		message2Link= linkTo(methodOn(MessageController.class).one(message2.getMessage_id()))
				.withSelfRel();
		messageAllLink= linkTo(methodOn(MessageController.class).all()).withSelfRel();
	}
	
	@Test
	void test_getAllMessages_isSuccesful() {
		//Mock
		when(messageService.getAllMessages()).thenReturn(mockMessages);
		
		//Act
		CollectionModel<EntityModel<Message>> response = messageController.all();
		
		//assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).getAllMessages();
		verify(messageAssembler, times(2)).toModel(any(Message.class));
	}
	
	@Test
	void test_getAllMessages_returnsUnauthroized() {
		when(messageService.getAllMessages()).thenThrow(new UnauthorizedException(new User()));

		// Act and Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			messageController.all();
		});
		
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Unauthorized access", exception.getReason());
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).getAllMessages();
	}
	
	@Test
	void test_getAllUserMessages_isSuccesful() {
		//Mock
		when(messageService.getAllUserMessages()).thenReturn(mockMessages);
		
		//Act
		CollectionModel<EntityModel<Message>> response = messageController.getUserMessages();
		
		//assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).getAllUserMessages();
		verify(messageAssembler, times(2)).toModel(any(Message.class));
	}
	
	@Test
	void test_getMessageById_isSuccesful() {
		//Mock
		when(messageService.getMessageById(1L)).thenReturn(message1);
		
		//Act
		EntityModel<Message> entityModel = messageController.one(1L);
		
		//Assert
		assertNotNull(entityModel);
		assertTrue(entityModel.getLinks().hasLink(message1Link.getRel()));
		assertTrue(entityModel.getLinks().hasLink(messageAllLink.getRel()));
		assertEquals(entityModel.getContent(), message1);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).getMessageById(1L);
		verify(messageAssembler, times(1)).toModel(message1);
	}
	
	@Test
	void test_getMessageById_isUnauthorized() {
		when(messageService.getMessageById(1L)).thenThrow(new UnauthorizedException(new User()));

		// Act and Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			messageController.one(1L);
		});
		
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Unauthorized access", exception.getReason());
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).getMessageById(1L);
	}
	
	@Test
	void test_getMessageByConversation_isSuccesful() {
		//Mock
		when(messageService.getConversationMessages(1L)).thenReturn(mockMessages);
		
		//Act
		CollectionModel<EntityModel<Message>> response = messageController.getConversationMessages(1L);
		
		//assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).getConversationMessages(1L);
		verify(messageAssembler, times(2)).toModel(any(Message.class));
	}
	
	@Test
	void test_getMessagesByConversation_isUnauthorized() {
		when(messageService.getConversationMessages(1L)).thenThrow(new UnauthorizedException(new User()));

		// Act and Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			messageController.getConversationMessages(1L);
		});
		
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Unauthorized access", exception.getReason());	
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).getConversationMessages(1L);
	}
	
	@Test
	void test_searchMessagesByContent_isSucessful() {
		//Mock
		when(messageService.searchMessagesByContent("Content")).thenReturn(mockMessages);
		
		//Act
		CollectionModel<EntityModel<Message>> response = messageController.searchMessagesByContent("Content");
		
		//assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).searchMessagesByContent("Content");
		verify(messageAssembler, times(2)).toModel(any(Message.class));
	}
	
	@Test
	void test_searchMessageByDate_isSuccesful() {
		//Mock
		Date date = new Date();
		when(messageService.searchMessagesByDate(date)).thenReturn(mockMessages);
		
		//Act
		CollectionModel<EntityModel<Message>> response = messageController.searchMessagesByDateSent(date);
		
		//assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).searchMessagesByDate(date);
		verify(messageAssembler, times(2)).toModel(any(Message.class));
	}
	
	@Test
	void test_searchMessagesByRead_isSuccesful() {
		when(messageService.searchMessagesByRead()).thenReturn(mockMessages);
		
		//Act
		CollectionModel<EntityModel<Message>> response = messageController.searchMessagesByIsRead();
		
		//assert
		validateModel(response);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).searchMessagesByRead();
		verify(messageAssembler, times(2)).toModel(any(Message.class));
	}
	
	@Test
	void test_sendMessage_isSuccesful() {
		MessageDTO messageDTO = new MessageDTO();
		when(messageService.createMessage(messageDTO, 1L)).thenReturn(message1);
		
		ResponseEntity<?> responseEntity = messageController.newMessage(messageDTO, 1L);
		
		validateResponse(responseEntity);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).createMessage(messageDTO, 1L);
		verify(messageAssembler, times(1)).toModel(message1);
	}
	
	@Test
	void test_sendMessage_returnsUnauthorized() {
		MessageDTO messageDTO = new MessageDTO();
		when(messageService.createMessage(messageDTO,1L)).thenThrow(new UnauthorizedException(new User()));

		// Act and Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			messageController.newMessage(messageDTO,1L);
		});
		
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Unauthorized access", exception.getReason());
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).createMessage(messageDTO, 1L);
	}
	
	@Test
	void test_updateMessage_isSuccesfull() {
		MessageDTO messageDTO = new MessageDTO();
		when(messageService.updateMessage(messageDTO, 1L)).thenReturn(message1);
		
		ResponseEntity<?> responseEntity = messageController.updateMessage(messageDTO, 1L);
		
		validateResponse(responseEntity);
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).updateMessage(messageDTO, 1L);
		verify(messageAssembler, times(1)).toModel(message1);
	}
	
	@Test
	void test_updateMessage_returnsUnauthorized() {
		MessageDTO messageDTO = new MessageDTO();
		when(messageService.updateMessage(messageDTO,1L)).thenThrow(new UnauthorizedException(new User()));

		// Act and Assert
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			messageController.updateMessage(messageDTO,1L);
		});
		
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals("Unauthorized access", exception.getReason());
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).updateMessage(messageDTO, 1L);
	}
	
	@Test
	void test_deleteMessage_isSuccesful() {
		//Act
		ResponseEntity<?> responseEntity = messageController.deleteMessage(1L);
		
	    //Assert
	    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
	}
	
	@Test
	void test_deleteMessage_returnsUnauthrozied() {
		//Mock
		doThrow(new UnauthorizedException(new User())).when(messageService).deleteMessage(1L);
		
		//Act
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			messageController.deleteMessage(1L);
		});
		
		//Assert
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		assertEquals(exception.getReason(), "Unauthorized access");
		
		//Ensure Stubbed Methods Are Called
		verify(messageService, times(1)).deleteMessage(1L);
	}
	//TODO: Since We Reuse The Vlaidate Model and Validate Response Method in Each Controller Unit Tests
	//			-Consider Creating Helper Class that Uitlizes Generics 
	/**
	 * Helper Function to Validate Collection Model and Corresponding EntityModels
	 * 
	 * @param response
	 */
	void validateModel(CollectionModel<EntityModel<Message>> response) {
		// Validate CollectionModel
		assertNotNull(response);
		assertTrue(response.getLinks().hasLink(messageAllLink.getRel()));

		// Extract EntityModels from CollectionModel
		List<EntityModel<Message>> entityModels = response.getContent().stream().collect(Collectors.toList());

		// Ensure Our Collectiton Model Has Our Two Mocked Entity Models
		assertNotNull(entityModels);
		assertEquals(2, entityModels.size());

		// Ensure Our EntityModels Have Link To Their Self And All
		assertTrue(entityModels.get(0).getLinks().hasLink(messageAllLink.getRel()));
		assertTrue(entityModels.get(0).getLinks().hasLink(message1Link.getRel()));
		assertTrue(entityModels.get(1).getLinks().hasLink(messageAllLink.getRel()));
		assertTrue(entityModels.get(1).getLinks().hasLink(message2Link.getRel()));

		// Ensure The EntityModels Content Is Equal To Corresponding Conversation
		assertEquals(entityModels.get(0).getContent(), message1);
		assertEquals(entityModels.get(1).getContent(), message2);
	}
	
	/**
	 * Helper Method to Validate ResponseEntity 
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
		assertEquals(responseEntityModel.getRequiredLink(IanaLinkRelations.SELF).getHref(), message1Link.getHref());
	}

	
	/**
	 * Helper Method to Mock  MesageAssembler
	 * 
	 * @param conversation
	 * @return
	 */
	EntityModel<Message> mockEntityModel(Message message) {
		return EntityModel.of(message,
				linkTo(methodOn(MessageController.class).one(message.getMessage_id())).withSelfRel(),
				linkTo(methodOn(MessageController.class).all()).withRel("/messages"));
	}
	
}
