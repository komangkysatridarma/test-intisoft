package com.example.test_intisoft.repository;

import com.example.test_intisoft.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findByEmail(String email);
    void deleteByEmail(String email);
}
