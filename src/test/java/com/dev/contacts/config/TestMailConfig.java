package com.dev.contacts.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
public class TestMailConfig {

  @Bean
  @Primary
  public JavaMailSender javaMailSender() {
    return new JavaMailSenderImpl();
  }
}
