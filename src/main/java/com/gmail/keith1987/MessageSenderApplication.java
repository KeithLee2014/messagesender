package com.gmail.keith1987;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class, 
                DataSourceAutoConfiguration.class})
@EnableJms
public class MessageSenderApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageSenderApplication.class, args);
    }
}
