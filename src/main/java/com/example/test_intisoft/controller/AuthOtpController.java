package com.example.test_intisoft.controller;

import com.example.test_intisoft.config.JwtTokenUtil;
import com.example.test_intisoft.model.AuthResponse;
import com.example.test_intisoft.model.User;
import com.example.test_intisoft.repository.UserRepository;
import com.example.test_intisoft.service.AuthService;
import com.example.test_intisoft.service.OtpService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthOtpController {
    private final OtpService otpService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    public AuthOtpController(
            OtpService otpService,
            AuthService authService,
            UserRepository userRepository,
            JwtTokenUtil jwtTokenUtil
    ) {
        this.otpService = otpService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestParam String email) {
        if (!userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email not registered");
        }

        otpService.sendOtp(email);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String email,
            @RequestParam String code
    ) {
        if (otpService.validateOtp(email, code)) {
            // Generate token atau langsung autentikasi
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtTokenUtil.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid OTP or OTP expired");
    }
}
