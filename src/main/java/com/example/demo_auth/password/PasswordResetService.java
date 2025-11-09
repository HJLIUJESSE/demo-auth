package com.example.demo_auth.password;

import com.example.demo_auth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class PasswordResetService {
  private final PasswordResetTokenRepository tokenRepo;
  private final UserRepository userRepo;
  private final PasswordEncoder encoder;

  private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

  @Transactional
  public Optional<String> createTokenForEmail(String email){
    String key = email == null ? null : email.trim().toLowerCase();
    var userOpt = userRepo.findByEmailIgnoreCase(key);
    if (userOpt.isEmpty()) return Optional.empty();
    var user = userOpt.get();
    // 清掉舊 token（可選）
    tokenRepo.deleteByUserId(user.getId());
    var token = UUID.randomUUID().toString().replace("-", "");
    var tokenHash = sha256(token);
    var prt = PasswordResetToken.builder()
        .user(user)
        .tokenHash(tokenHash)
        .expiresAt(Instant.now().plus(DEFAULT_TTL))
        .used(false)
        .build();
    tokenRepo.save(prt);
    return Optional.of(token);
  }

  @Transactional
  public boolean resetPassword(String token, String newRawPassword){
    var prt = tokenRepo.findByTokenHash(sha256(token)).orElse(null);
    if (prt == null) return false;
    if (prt.isUsed()) return false;
    if (prt.getExpiresAt().isBefore(Instant.now())) return false;
    var user = prt.getUser();
    user.setPassword(encoder.encode(newRawPassword));
    prt.setUsed(true);
    // JPA 追蹤狀態會自動 flush
    return true;
  }

  private static String sha256(String s){
    try {
      var md = java.security.MessageDigest.getInstance("SHA-256");
      byte[] b = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte x: b) sb.append(String.format("%02x", x));
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("hash error", e);
    }
  }
}
