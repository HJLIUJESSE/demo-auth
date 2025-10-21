// src/main/java/com/example/demoauth/security/JwtService.java
package com.example.demo_auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
  private final Key key;
  private final long expirationMillis;

  public JwtService(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration-minutes}") long expMinutes
  ){
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMillis = expMinutes * 60 * 1000;
  }

  public String generateToken(String username){
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis()+expirationMillis))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String validateAndGetSubject(String token){
    return Jwts.parserBuilder().setSigningKey(key).build()
        .parseClaimsJws(token).getBody().getSubject();
  }
}
