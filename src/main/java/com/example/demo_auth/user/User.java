// src/main/java/com/example/demoauth/user/User.java
package com.example.demo_auth.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank @Size(min = 3, max = 50)
  @Column(nullable = false)
  private String username;

  @NotBlank @Email
  @Column(nullable = false)
  private String email;

  @NotBlank
  private String password; // 存放BCrypt後的雜湊

  private String roles; // 例: "ROLE_USER,ROLE_ADMIN"
  
  @Builder.Default
  private boolean enabled = true;

  // 使用者基本資料
  private java.time.LocalDate birthDate;

  // 頭像URL（例如 /uploads/avatars/xxx.png）
  private String avatarUrl;
}
