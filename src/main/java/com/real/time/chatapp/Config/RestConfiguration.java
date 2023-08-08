package com.real.time.chatapp.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.real.time.chatapp.Entities.Conversation;
import com.real.time.chatapp.Entities.Message;
/**
 * Class Ensures We Are Able To View Id of our Entities 
 * 
 * @author ADMIN
 *
 */
@Configuration
public class RestConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(
      RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(Conversation.class, Message.class);
    }
}
