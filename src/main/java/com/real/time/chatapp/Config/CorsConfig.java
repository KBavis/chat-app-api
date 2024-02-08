package com.real.time.chatapp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig{

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
					.allowedOrigins(
							"http://localhost:3000", 
							"http://ec2-3-90-160-1.compute-1.amazonaws.com:3000", 
							"http://ec2-3-90-160-1.compute-1.amazonaws.com"
					 ) //added ec2 instance (both for reverse proxy & not) 
					.allowedMethods("*")
					.allowedHeaders("*")
					.allowCredentials(true)
					.maxAge(3600);
			}
		};
	}	
	
}
