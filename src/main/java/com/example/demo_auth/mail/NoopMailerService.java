package com.example.demo_auth.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoopMailerService implements MailerService {
  private static final Logger log = LoggerFactory.getLogger(NoopMailerService.class);
  @Override
  public void sendPasswordReset(String toEmail, String resetLink) {
    log.info("[DEV] Password reset link for {} => {}", toEmail, resetLink);
  }
}

