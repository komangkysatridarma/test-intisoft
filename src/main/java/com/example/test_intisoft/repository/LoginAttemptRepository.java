package com.example.test_intisoft.repository;

import com.example.test_intisoft.model.LoginAttempt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE " +
            "la.username = :username AND " +
            "la.attemptTime > :time AND " +
            "la.successful = false")
    int countFailedAttemptsSince(@Param("username") String username,
                                 @Param("time") LocalDateTime time);

    @Query("SELECT la FROM LoginAttempt la WHERE " +
            "la.username = :username AND " +
            "la.successful = false " +
            "ORDER BY la.attemptTime DESC")
    List<LoginAttempt> findTop1ByUsernameAndSuccessfulOrderByAttemptTimeDesc(
            @Param("username") String username,
            @Param("successful") boolean successful);
}