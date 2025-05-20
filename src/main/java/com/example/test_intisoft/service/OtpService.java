package com.example.test_intisoft.service;

import com.example.test_intisoft.model.OtpCode;
import com.example.test_intisoft.repository.OtpRepository;
import com.example.test_intisoft.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {
    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public OtpService(OtpRepository otpRepository,
                      JavaMailSender mailSender,
                      UserRepository userRepository) {
        this.otpRepository = otpRepository;
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    @Value("${otp.length}")
    private int otpLength;

    @Value("${otp.expiration.minutes}")
    private int expirationMinutes;

    public void sendOtp(String email) {
        // Hapus OTP lama jika ada
        otpRepository.deleteByEmail(email);

        // Generate OTP
        String code = generateOtp();

        // Simpan OTP
        OtpCode otp = new OtpCode();
        otp.setEmail(email);
        otp.setCode(code);
        otp.setExpirationTime(LocalDateTime.now().plusMinutes(expirationMinutes));
        otpRepository.save(otp);

        // Kirim email
        sendEmail(email, "Your OTP Code", "Your OTP code is: " + code);
    }

    public boolean validateOtp(String email, String code) {
        Optional<OtpCode> otpOpt = otpRepository.findByEmail(email);
        if (otpOpt.isEmpty()) {
            return false;
        }

        OtpCode otp = otpOpt.get();
        return otp.getCode().equals(code) &&
                otp.getExpirationTime().isAfter(LocalDateTime.now());
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
