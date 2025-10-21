// src/main/java/com/example/demoauth/security/CustomUserDetailsService.java
package com.example.demo_auth.security;

import com.example.demo_auth.user.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository repo;
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var u = repo.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new CustomUserDetails(u);
  }
}
