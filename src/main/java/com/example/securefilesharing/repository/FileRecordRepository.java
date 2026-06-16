package com.example.securefilesharing.repository;

import com.example.securefilesharing.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FileRecordRepository extends JpaRepository<FileRecord, String> {

    // Finds files whose 10-minute window has passed and haven't been cleaned up yet
    List<FileRecord> findByExpiresAtBeforeAndDeletedFalse(LocalDateTime now);
}
