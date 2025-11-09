// src/main/java/com/example/demoauth/user/UserRepository.java
package com.example.demo_auth.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  Optional<User> findByEmailIgnoreCase(String email);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  boolean existsByEmailIgnoreCase(String email);
}
