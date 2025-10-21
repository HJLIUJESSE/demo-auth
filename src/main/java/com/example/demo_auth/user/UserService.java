// src/main/java/com/example/demoauth/user/UserService.java
package com.example.demo_auth.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class UserService {
  private final UserRepository repo;
  private final PasswordEncoder encoder;

  public User register(String username, String email, String rawPassword){
    if (repo.existsByUsername(username)) throw new RuntimeException("Username taken");
    if (repo.existsByEmail(email)) throw new RuntimeException("Email used");
    var u = User.builder()
        .username(username)
        .email(email)
        .password(encoder.encode(rawPassword))
        .roles("ROLE_USER")
        .enabled(true)
        .build();
    return repo.save(u);
  }
}
