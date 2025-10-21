package com.example.demo_auth.me;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") // <-- ✅ 加上這個 API 基礎路徑
public class MeController {

    // 這現在會被組合為 /api/me
    @GetMapping("/me") 
    public Object me(@AuthenticationPrincipal UserDetails user){
        return user==null ? "no auth" : user.getUsername();
    }
}