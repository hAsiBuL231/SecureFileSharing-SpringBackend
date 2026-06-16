package com.example.securefilesharing.repository;

import com.example.securefilesharing.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRecordRepository extends JpaRepository<OtpRecord, Long> {

    Optional<OtpRecord> findByOtp(String otp);
}
