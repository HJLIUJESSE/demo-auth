// src/main/java/com/example/demoauth/auth/AuthController.java
package com.example.demo_auth.auth;

import com.example.demo_auth.security.JwtService;
import com.example.demo_auth.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authManager;
  private final JwtService jwt;
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req){
    var u = userService.register(req.getUsername(), req.getEmail(), req.getPassword());
    return ResponseEntity.ok("registered: " + u.getUsername());
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req){
    try {
      Authentication auth = authManager.authenticate(
          new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
      String token = jwt.generateToken(req.getUsername());
      return ResponseEntity.ok(new TokenResponse(token));
    } catch (AuthenticationException e) {
      throw new BadCredentialsException(e.getMessage());
    }
  }

  @GetMapping("/hello")
  public String hello() { return "public ok"; }

  @Data static class RegisterRequest {
    @NotBlank @Size(min=3,max=50) private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min=6,max=100) private String password;
  }
  @Data static class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
  }
  @Data static class TokenResponse { private final String token; }
}
