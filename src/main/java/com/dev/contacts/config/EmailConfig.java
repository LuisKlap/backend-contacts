package com.dev.contacts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@Profile("!test")
public class EmailConfig {

  @Value("${spring.mail.host}")
  private String host;

  @Value("${spring.mail.port}")
  private int port;

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  @Value("${spring.mail.properties.mail.smtp.auth}")
  private boolean smtpAuth;

  @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
  private boolean starttlsEnable;

  @Bean
  public JavaMailSender javaMailSender() {
    try {
      JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
      mailSender.setHost(host);
      mailSender.setPort(port);
      mailSender.setUsername(username);
      mailSender.setPassword(password);

      Properties props = mailSender.getJavaMailProperties();
      props.put("mail.smtp.auth", smtpAuth);
      props.put("mail.smtp.starttls.enable", starttlsEnable);
      props.put("mail.smtp.ssl.enable", "true");
      props.put("mail.smtp.socketFactory.port", port);
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

      return mailSender;
    } catch (Exception e) {
      throw new RuntimeException("Erro ao configurar JavaMailSender", e);
    }
  }
}
