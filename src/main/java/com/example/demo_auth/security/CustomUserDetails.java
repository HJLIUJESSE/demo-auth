// src/main/java/com/example/demoauth/security/CustomUserDetails.java
package com.example.demo_auth.security;

import com.example.demo_auth.user.User;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;
public class CustomUserDetails implements UserDetails {
  private final User u;
  public CustomUserDetails(User u){ this.u=u; }
  @Override public Collection<? extends GrantedAuthority> getAuthorities() {
    if (u.getRoles()==null) return List.of();
    String[] roles = u.getRoles().split(",");
    List<GrantedAuthority> list = new ArrayList<>();
    for (String r: roles) if(!r.isBlank()) list.add(new SimpleGrantedAuthority(r.trim()));
    return list;
  }
  @Override public String getPassword() { return u.getPassword(); }
  @Override public String getUsername() { return u.getUsername(); }
  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return u.isEnabled(); }
}
