package com.real.time.chatapp.Auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.real.time.chatapp.Entities.Role;
import com.real.time.chatapp.Entities.User;
import com.real.time.chatapp.Repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LoadAdmin {

	private static final Logger log = LoggerFactory.getLogger(LoadAdmin.class);
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	@Bean
	CommandLineRunner initDatabase() {
		Properties properties = new Properties();
        try (InputStream inputStream = LoadAdmin.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String adminPassword = properties.getProperty("admin.password");
		return args -> {
			Long id = (long) 0;
			if(!userRepository.findByUserName("AdminUser").isPresent()) {
				User adminUser = new User(id, "AdminUser","Admin", "User", passwordEncoder.encode(adminPassword),null, Role.ADMIN, null, null, null);
				log.info(userRepository.save(adminUser) + " saved.");
				log.info("Database initialized.");
			}
			
		};
	}
}
