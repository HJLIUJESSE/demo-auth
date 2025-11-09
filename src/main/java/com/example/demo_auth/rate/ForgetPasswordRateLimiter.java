package com.example.demo_auth.rate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ForgetPasswordRateLimiter {
  private final int maxPerWindow;
  private final int windowSeconds;

  private static class Counter { int count; long windowStartEpoch; }
  private final Map<String, Counter> counters = new ConcurrentHashMap<>();

  public ForgetPasswordRateLimiter(
      @Value("${app.rate-limit.forgot-password.max-per-window:5}") int maxPerWindow,
      @Value("${app.rate-limit.forgot-password.window-seconds:900}") int windowSeconds) {
    this.maxPerWindow = maxPerWindow;
    this.windowSeconds = windowSeconds;
  }

  public boolean allow(String key){
    long now = Instant.now().getEpochSecond();
    Counter c = counters.computeIfAbsent(key, k -> { Counter x = new Counter(); x.count = 0; x.windowStartEpoch = now; return x; });
    synchronized (c){
      if (now - c.windowStartEpoch >= windowSeconds){
        c.windowStartEpoch = now;
        c.count = 0;
      }
      if (c.count >= maxPerWindow) return false;
      c.count++;
      return true;
    }
  }
}

