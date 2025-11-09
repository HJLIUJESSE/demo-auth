package com.example.demo_auth.profile;

import com.example.demo_auth.user.User;
import com.example.demo_auth.user.UserRepository;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

  private final UserRepository userRepo;

  @GetMapping
  public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails principal){
    var u = requireUser(principal);
    return ResponseEntity.ok(new ProfileResponse(u.getUsername(), u.getEmail(), u.getBirthDate(), u.getAvatarUrl()));
  }

  @PutMapping
  public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetails principal,
                                         @RequestBody UpdateProfileRequest req){
    var u = requireUser(principal);
    if (req.getBirthDate()!=null) u.setBirthDate(req.getBirthDate());
    userRepo.save(u);
    return ResponseEntity.ok("updated");
  }

  @PostMapping(path = "/avatar", consumes = {"multipart/form-data"})
  public ResponseEntity<?> uploadAvatar(@AuthenticationPrincipal UserDetails principal,
                                        @RequestPart("file") MultipartFile file) throws IOException {
    var u = requireUser(principal);

    if (file.isEmpty()) return ResponseEntity.badRequest().body("empty file");
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return ResponseEntity.badRequest().body("only image allowed");
    }
    Path root = Paths.get("uploads/avatars");
    Files.createDirectories(root);
    String ext = guessExtension(file.getOriginalFilename());
    String newName = UUID.randomUUID().toString().replace("-", "") + (ext==null?"":"."+ext);
    Path dest = root.resolve(newName);
    Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
    String url = "/uploads/avatars/" + newName;
    u.setAvatarUrl(url);
    userRepo.save(u);
    return ResponseEntity.ok(new AvatarResponse(url));
  }

  private User requireUser(UserDetails principal){
    var u = userRepo.findByUsername(principal.getUsername()).orElseThrow();
    return u;
  }

  private String guessExtension(String original){
    if (!StringUtils.hasText(original)) return null;
    int i = original.lastIndexOf('.');
    if (i<=0 || i==original.length()-1) return null;
    return original.substring(i+1).toLowerCase();
  }

  @Data
  static class UpdateProfileRequest {
    @Past
    private LocalDate birthDate;
  }

  @Data
  static class ProfileResponse {
    private final String username;
    private final String email;
    private final LocalDate birthDate;
    private final String avatarUrl;
  }

  @Data
  static class AvatarResponse {
    private final String url;
  }
}

