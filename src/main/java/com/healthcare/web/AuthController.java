package com.healthcare.web;

import com.healthcare.domain.Account;
import com.healthcare.dto.auth.AuthDtos;
import com.healthcare.security.JwtUtil;
import com.healthcare.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AuthController(AccountService accountService, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@RequestBody AuthDtos.SignupRequest req) {
        Account acc = accountService.signup(req);
        return ResponseEntity.ok(acc.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.TokenResponse> login(@RequestBody AuthDtos.LoginRequest req) {
        Account acc = accountService.login(req);
        String token = jwtUtil.generateToken(acc.getUsername(), acc.getId(), acc.getRole().name());
        return ResponseEntity.ok(new AuthDtos.TokenResponse(token, acc.getUsername(), acc.getRole().name(), acc.getDisplayName()));
    }
}
