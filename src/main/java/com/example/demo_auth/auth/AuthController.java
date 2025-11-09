// src/main/java/com/example/demoauth/auth/AuthController.java
package com.example.demo_auth.auth;

import com.example.demo_auth.security.JwtService;
import com.example.demo_auth.password.PasswordResetService;
import com.example.demo_auth.mail.MailerService;
import com.example.demo_auth.rate.ForgetPasswordRateLimiter;
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
  private final PasswordResetService passwordResetService;
  private final MailerService mailer;
  private final ForgetPasswordRateLimiter rateLimiter;

  @org.springframework.beans.factory.annotation.Value("${app.password-reset.return-token-in-response:true}")
  private boolean returnTokenInResponse;
  @org.springframework.beans.factory.annotation.Value("${app.frontend.reset-base-url:http://localhost:5173/reset-password?token=}")
  private String resetBaseUrl;
  @org.springframework.beans.factory.annotation.Value("${app.mail.enabled:false}")
  private boolean mailEnabled;

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

  // 忘記密碼：輸入 email，建立重設 token（開發環境可直接回傳 token）
  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req, jakarta.servlet.http.HttpServletRequest httpReq){
    String ip = httpReq.getRemoteAddr();
    String emailKey = req.getEmail()==null?"":req.getEmail().trim().toLowerCase();
    boolean allowed = rateLimiter.allow("ip:"+ip) && rateLimiter.allow("email:"+emailKey);
    if (!allowed) {
      return ResponseEntity.ok(new ForgotPasswordResponse(null));
    }
    var tokenOpt = passwordResetService.createTokenForEmail(emailKey);
    // 若有找到使用者，視設定選擇回 token（dev）或寄信（prod）
    tokenOpt.ifPresent(token -> {
      String link = resetBaseUrl + token;
      // dev: NoopMailer 會記 log；prod: SmtpMailer 會寄信
      mailer.sendPasswordReset(req.getEmail(), link);
    });
    // 為安全起見，無論是否存在該 email 都回 200；是否回 token 由設定決定
    String tokenToReturn = (returnTokenInResponse ? tokenOpt.orElse(null) : null);
    return ResponseEntity.ok(new ForgotPasswordResponse(tokenToReturn));
  }

  // 以 token 重設密碼
  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req){
    boolean ok = passwordResetService.resetPassword(req.getToken(), req.getNewPassword());
    if (!ok) return ResponseEntity.badRequest().body("invalid or expired token");
    return ResponseEntity.ok("password reset ok");
  }

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
  @Data static class ForgotPasswordRequest { private String email; }
  @Data static class ForgotPasswordResponse { private final String token; }
  @Data static class ResetPasswordRequest { private String token; private String newPassword; }
}
