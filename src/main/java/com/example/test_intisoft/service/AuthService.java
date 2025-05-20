package com.example.test_intisoft.service;

import com.example.test_intisoft.config.JwtTokenUtil;
import com.example.test_intisoft.model.*;
import com.example.test_intisoft.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtTokenUtil jwtTokenUtil,
                       LoginAttemptService loginAttemptService,
                       HttpServletRequest request,
                       PasswordEncoder passwordEncoder,
                       UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.loginAttemptService = loginAttemptService;
        this.request = request;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        String ipAddress = getClientIP();
        String username = loginRequest.usernameOrEmail();

        // Cek apakah akun sedang diblokir
        if (loginAttemptService.isAccountBlocked(username)) {
            throw new RuntimeException("Account temporarily blocked. Please try again later.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.usernameOrEmail(),
                            loginRequest.password()
                    )
            );

            // Jika berhasil, rekam attempt sukses
            loginAttemptService.recordLoginAttempt(username, ipAddress, true);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            String token = jwtTokenUtil.generateToken(user);
            return new AuthResponse(token);

        } catch (BadCredentialsException e) {
            // Rekam attempt gagal
            loginAttemptService.recordLoginAttempt(username, ipAddress, false);

            // Cek apakah attempt ini melebihi batas
            if (loginAttemptService.isMaxAttemptsExceeded(username)) {
                throw new RuntimeException("Too many failed attempts. Account blocked for " +
                        loginAttemptService.getBlockTimeMinutes() + " minutes.");
            }

            throw new RuntimeException("Invalid username or password");
        }
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }


    public UserDTO registerUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.username())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(userDTO.email())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setFullname(userDTO.fullname());
        user.setUsername(userDTO.username());
        user.setEmail(userDTO.email());
        user.setPassword(passwordEncoder.encode(userDTO.password()));
        user.setRole(Role.VIEWER); // Default role

        User savedUser = userRepository.save(user);
        return userService.convertToDTO(savedUser);
    }
}

