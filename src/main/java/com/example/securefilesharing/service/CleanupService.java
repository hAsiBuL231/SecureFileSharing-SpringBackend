package com.example.securefilesharing.service;

import com.example.securefilesharing.entity.FileRecord;
import com.example.securefilesharing.repository.FileRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleanupService {

    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    private final FileRecordRepository fileRecordRepository;

    public CleanupService(FileRecordRepository fileRecordRepository) {
        this.fileRecordRepository = fileRecordRepository;
    }

    /**
     * Runs every 60 seconds.
     * Finds files whose 10-minute window has passed and deletes them from disk,
     * then marks the DB record as deleted so no further downloads are served.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void deleteExpiredFiles() {
        List<FileRecord> expired = fileRecordRepository.findByExpiresAtBeforeAndDeletedFalse(LocalDateTime.now());

        if (expired.isEmpty()) {
            return;
        }

        log.info("Cleanup: found {} expired file(s) to delete.", expired.size());

        for (FileRecord record : expired) {
            try {
                boolean deleted = Files.deleteIfExists(Paths.get(record.getStoragePath()));
                record.setDeleted(true);
                fileRecordRepository.save(record);

                if (deleted) {
                    log.info("Deleted expired file: {}", record.getOriginalFilename());
                } else {
                    log.warn("File not found on disk (already removed?): {}", record.getStoragePath());
                }
            } catch (IOException e) {
                log.error("Failed to delete file {}: {}", record.getStoragePath(), e.getMessage());
            }
        }
    }
}
