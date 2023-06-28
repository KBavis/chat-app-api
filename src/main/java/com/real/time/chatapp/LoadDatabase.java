//package com.real.time.chatapp;
//
//import java.time.LocalTime;
//import java.util.Date;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class LoadDatabase {
//
//    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private MessageRepository messageRepository;
//
//    @Autowired
//    private ConversationRepository conversationRepository;
//
//    @Bean
//    CommandLineRunner initDatabase() {
//        return args -> {
//            User userOne = new User("jermiahH", "Jerimiah", "Withers", "swerve76");
//            User userTwo = new User("ggTTv_user", "Kiope", "Pite", "nice123");
//
//            userOne = userRepository.save(userOne);
//            userTwo = userRepository.save(userTwo);
//
//            LocalTime myTime = LocalTime.now();
//            Message msg1 = new Message(false, userOne, userTwo, new Date(), "Hey Jerimiah Withers!", myTime);
//            Message msg2 = new Message(false, userTwo, userOne, new Date(), "Hey Kiope Pite!", myTime);
//            Conversation conversation = new Conversation(2, new Date());
//            conversation = conversationRepository.save(conversation);
//            msg1.setConversation(conversation);
//            msg2.setConversation(conversation);
//            msg1 = messageRepository.save(msg1);
//            msg2 = messageRepository.save(msg2);
//
////            TODO: Figure out error for detatched entity
////            TODO: Make GitHUb Repo
//            User savedUserOne = userRepository.findById(userOne.getId()).orElseThrow();
//            User savedUserTwo = userRepository.findById(userTwo.getId()).orElseThrow();
//            
//            conversation.addUser(savedUserOne);
//            conversation.addUser(savedUserTwo);
//            conversation.addMessage(msg1);
//            conversation.addMessage(msg2);
//
//            conversationRepository.save(conversation);
//
//            log.info("Database initialized.");
//        };
//    }
//}
	
