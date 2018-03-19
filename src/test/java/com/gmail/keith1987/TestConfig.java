package com.gmail.keith1987;

import org.mockito.Mockito;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.jms.core.JmsTemplate;

/**
 * Created by keith on 18/03/2018.
 */
@Configuration
public class TestConfig {

    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("session", new SimpleThreadScope());
        return configurer;
    }

    @Bean
    public JmsTemplate jmsTemplate(){
        return Mockito.mock(JmsTemplate.class);
    }
}
