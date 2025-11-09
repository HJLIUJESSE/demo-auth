package com.example.demo_auth.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpMailerService implements MailerService {
  private static final Logger log = LoggerFactory.getLogger(SmtpMailerService.class);
  private final JavaMailSender sender;
  private final String from;
  private final String fromName;
  private final boolean sandboxEnabled;
  private final String sandboxRecipient;
  private final List<String> allowlist;
  private final String replyTo;
  private final String supportUrl;

  public SmtpMailerService(
      JavaMailSender sender,
      @Value("${app.mail.from:noreply@localhost}") String from,
      @Value("${app.mail.fromName:Demo App}") String fromName,
      @Value("${app.mail.sandbox.enabled:false}") boolean sandboxEnabled,
      @Value("${app.mail.sandbox.recipient:}") String sandboxRecipient,
      @Value("${app.mail.allowlist:}") String allowlist,
      @Value("${app.mail.reply-to:}") String replyTo,
      @Value("${app.mail.support-url:https://example.com/support}") String supportUrl
  ){
    this.sender = sender;
    this.from = from;
    this.fromName = fromName;
    this.sandboxEnabled = sandboxEnabled;
    this.sandboxRecipient = sandboxRecipient;
    this.allowlist = Arrays.stream(allowlist.split(","))
        .map(String::trim).filter(s -> !s.isEmpty()).toList();
    this.replyTo = replyTo;
    this.supportUrl = supportUrl;
  }

  @Override
  public void sendPasswordReset(String toEmail, String resetLink) {
    try {
      String finalTo = decideRecipient(toEmail);

      MimeMessage mime = sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mime, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
      helper.setFrom(new InternetAddress(from, fromName));
      helper.setTo(finalTo);
      if (sandboxEnabled && !isAllowed(toEmail)) {
        mime.setHeader("X-Original-To", toEmail);
      }
      helper.setSubject("Reset your password");
      if (replyTo != null && !replyTo.isBlank()) {
        helper.setReplyTo(replyTo);
      }

      String text = "You requested a password reset for " + fromName + "\n" +
          "Reset link: " + resetLink + "\n\n" +
          "If you didn't request this, you can ignore this email.\n" +
          "Need help? " + supportUrl + "\n";

      String html = "<!doctype html><html><head><meta charset=\\\"utf-8\\\">" +
          "<meta name=\\\"viewport\\\" content=\\\"width=device-width, initial-scale=1\\\">" +
          "<title>Password Reset</title>" +
          "</head><body style=\\\"font-family:Arial,sans-serif;background:#f6f9fc;margin:0;padding:24px;\\\">" +
          "<table role=\\\"presentation\\\" cellpadding=0 cellspacing=0 width=\\\"100%\\\" style=\\\"max-width:560px;margin:0 auto;background:#ffffff;border:1px solid #e9ecef;border-radius:8px;\\\">" +
          "<tr><td style=\\\"padding:24px 24px 8px 24px;border-bottom:1px solid #f1f3f5;\\\">" +
          "<h2 style=\\\"margin:0;color:#222;\\\">" + escapeHtml(fromName) + "</h2>" +
          "</td></tr>" +
          "<tr><td style=\\\"padding:24px;\\\">" +
          "<p style=\\\"margin:0 0 12px 0;color:#222;\\\">We received a request to reset your password.</p>" +
          "<p style=\\\"margin:0 0 20px 0;color:#555;\\\">Click the button below to set a new password. This link will expire soon.</p>" +
          "<p style=\\\"margin:0 0 24px 0;\\\"><a href=\\\"" + resetLink + "\\\" style=\\\"display:inline-block;background:#2d6cdf;color:#fff;text-decoration:none;padding:12px 18px;border-radius:6px;\\\">Reset Password</a></p>" +
          "<p style=\\\"margin:0 0 8px 0;color:#555;\\\">If the button doesn’t work, copy and paste this link into your browser:</p>" +
          "<p style=\\\"margin:0 0 16px 0;word-break:break-all;color:#0b7285;\\\"><a href=\\\"" + resetLink + "\\\" style=\\\"color:#0b7285;\\\">" + resetLink + "</a></p>" +
          "<p style=\\\"margin:24px 0 0 0;color:#777;font-size:13px;\\\">If you didn’t request this, you can ignore this email. Need help? <a href=\\\"" + supportUrl + "\\\" style=\\\"color:#0b7285;\\\">Contact support</a>.</p>" +
          "</td></tr></table>" +
          "</body></html>";
      helper.setText(text, html);

      // Disable tracking for transactional (click/open) and set category via X-SMTPAPI (SendGrid over SMTP)
      try {
        String xsmtp = "{\"filters\":{\"clicktrack\":{\"settings\":{\"enable\":0}}," +
            "\"opentrack\":{\"settings\":{\"enable\":0}}},\"categories\":[\"password_reset\"]}";
        mime.setHeader("X-SMTPAPI", xsmtp);
      } catch (Exception ignored) {}
      sender.send(mime);
      log.info("password reset mail sent to {}{}", finalTo,
          (sandboxEnabled && !isAllowed(toEmail)) ? " (sandbox, original: " + toEmail + ")" : "");
    } catch (Exception e) {
      log.warn("send mail failed: {}", e.toString());
    }
  }

  private String decideRecipient(String to){
    if (sandboxEnabled && !isAllowed(to)) {
      if (sandboxRecipient == null || sandboxRecipient.isBlank()) {
        log.warn("sandbox enabled but no sandbox recipient configured; using original to: {}", to);
        return to;
      }
      return sandboxRecipient;
    }
    return to;
  }

  private boolean isAllowed(String to){
    if (allowlist.isEmpty()) return false;
    String lower = to.toLowerCase();
    for (String token : allowlist) {
      String t = token.toLowerCase();
      if (t.contains("@")) {
        if (lower.equals(t)) return true;
      } else {
        if (lower.endsWith("@" + t)) return true;
      }
    }
    return false;
  }

  private static String escapeHtml(String s){
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }
}
