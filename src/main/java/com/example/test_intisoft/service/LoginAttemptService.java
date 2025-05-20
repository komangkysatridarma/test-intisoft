package com.example.test_intisoft.service;

import com.example.test_intisoft.model.LoginAttempt;
import com.example.test_intisoft.repository.LoginAttemptRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.block-time-minutes:30}")
    private int blockTimeMinutes;

    @Value("${security.login.attempt-window-minutes:10}")
    private int attemptWindowMinutes;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    public void recordLoginAttempt(String username, String ipAddress, boolean successful) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setIpAddress(ipAddress);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setSuccessful(successful);
        loginAttemptRepository.save(attempt);
    }

    public boolean isMaxAttemptsExceeded(String username) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(attemptWindowMinutes);

        // Hanya hitung attempt yang gagal dalam window time
        int failedAttempts = loginAttemptRepository.countFailedAttemptsSince(username, windowStart);

        // Debug log untuk memeriksa perhitungan
        System.out.println("Failed attempts for " + username + " in last " +
                attemptWindowMinutes + " minutes: " + failedAttempts);

        return failedAttempts >= maxAttempts;
    }

    public boolean isAccountBlocked(String username) {
        // Cek attempt terakhir yang gagal
        List<LoginAttempt> lastAttempts = loginAttemptRepository
                .findTop1ByUsernameAndSuccessfulOrderByAttemptTimeDesc(username, false);

        if (lastAttempts.isEmpty()) {
            return false;
        }

        LoginAttempt lastFailed = lastAttempts.get(0);
        LocalDateTime blockUntil = lastFailed.getAttemptTime().plusMinutes(blockTimeMinutes);

        // Jika waktu blokir belum habis
        return LocalDateTime.now().isBefore(blockUntil);
    }


    public int getBlockTimeMinutes() {
        return blockTimeMinutes;
    }
}