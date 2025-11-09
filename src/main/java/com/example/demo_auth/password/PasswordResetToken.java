package com.example.demo_auth.password;

import com.example.demo_auth.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_prt_token_hash", columnList = "tokenHash", unique = true),
    @Index(name = "idx_prt_user", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  private User user;

  @Column(nullable = false, unique = true, length = 128)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  @Builder.Default
  private boolean used = false;
}
