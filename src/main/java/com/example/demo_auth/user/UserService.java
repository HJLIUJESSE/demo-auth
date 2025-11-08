// src/main/java/com/example/demoauth/user/UserService.java
package com.example.demo_auth.user;

import com.example.demo_auth.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class UserService {
  private final UserRepository repo;
  private final PasswordEncoder encoder;

  @Transactional
  public User register(String username, String email, String rawPassword){
    if (repo.existsByUsername(username)) {
      throw new ResourceConflictException("Username taken");
    }
    if (repo.existsByEmail(email)) {
      throw new ResourceConflictException("Email used");
    }
    var user = User.builder()
        .username(username)
        .email(email)
        .password(encoder.encode(rawPassword))
        .roles("ROLE_USER")
        .enabled(true)
        .build();
    try {
      return repo.save(user);
    } catch (DataIntegrityViolationException ex) {
      throw new ResourceConflictException("Username or email already exists");
    }
  }
}
