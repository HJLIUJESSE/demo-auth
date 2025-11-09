package com.example.demo_auth.mail;

public interface MailerService {
  void sendPasswordReset(String toEmail, String resetLink);
}

