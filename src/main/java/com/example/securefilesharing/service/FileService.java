package com.example.securefilesharing.service;

import com.example.securefilesharing.dto.OtpVerifyResponse;
import com.example.securefilesharing.dto.UploadResponse;
import com.example.securefilesharing.entity.DownloadToken;
import com.example.securefilesharing.entity.FileRecord;
import com.example.securefilesharing.entity.OtpRecord;
import com.example.securefilesharing.exception.ApiException;
import com.example.securefilesharing.repository.DownloadTokenRepository;
import com.example.securefilesharing.repository.FileRecordRepository;
import com.example.securefilesharing.repository.OtpRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final long EXPIRY_MINUTES = 10;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    private final FileRecordRepository fileRecordRepository;
    private final OtpRecordRepository otpRecordRepository;
    private final DownloadTokenRepository downloadTokenRepository;
    private final Random random = new Random();

    public FileService(FileRecordRepository fileRecordRepository,
                       OtpRecordRepository otpRecordRepository,
                       DownloadTokenRepository downloadTokenRepository) {
        this.fileRecordRepository = fileRecordRepository;
        this.otpRecordRepository = otpRecordRepository;
        this.downloadTokenRepository = downloadTokenRepository;
    }

    @Transactional
    public UploadResponse uploadFile(MultipartFile file) {
        // --- Validations ---
        if (file == null || file.isEmpty()) {
            throw new ApiException("No file provided or file is empty.", HttpStatus.BAD_REQUEST);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ApiException("File must have a valid name.", HttpStatus.BAD_REQUEST);
        }

        // Prevent path traversal
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new ApiException("Invalid file name.", HttpStatus.BAD_REQUEST);
        }

        long maxSize = 50L * 1024 * 1024; // 50 MB
        if (file.getSize() > maxSize) {
            throw new ApiException("File size exceeds the 50MB limit.", HttpStatus.PAYLOAD_TOO_LARGE);
        }

        // --- Save file to disk ---
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // UUID prefix prevents filename collisions
            String storedFilename = UUID.randomUUID() + "_" + originalFilename;
            Path targetPath = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(EXPIRY_MINUTES);

            // --- Persist file record ---
            FileRecord fileRecord = FileRecord.builder()
                    .originalFilename(originalFilename)
                    .storagePath(targetPath.toString())
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .fileSize(file.getSize())
                    .uploadedAt(now)
                    .expiresAt(expiresAt)
                    .build();
            fileRecordRepository.save(fileRecord);

            // --- Generate unique 6-digit numeric OTP ---
            String otp = generateUniqueOtp();

            OtpRecord otpRecord = OtpRecord.builder()
                    .otp(otp)
                    .fileRecord(fileRecord)
                    .createdAt(now)
                    .expiresAt(expiresAt)
                    .build();
            otpRecordRepository.save(otpRecord);

            log.info("File uploaded: {} | OTP: {} | Expires: {}", originalFilename, otp, expiresAt);

            return UploadResponse.builder()
                    .success(true)
                    .message("File uploaded successfully.")
                    .otp(otp)
                    .fileId(fileRecord.getId())
                    .originalFilename(originalFilename)
                    .fileSizeBytes(file.getSize())
                    .uploadedAt(now.format(FORMATTER))
                    .expiresAt(expiresAt.format(FORMATTER))
                    .note("Share the OTP with the recipient. It is valid for 10 minutes and can only be used once.")
                    .build();

        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw new ApiException("Could not save file. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public OtpVerifyResponse verifyOtp(String otp) {
        if (otp == null || otp.isBlank()) {
            throw new ApiException("OTP must not be empty.", HttpStatus.BAD_REQUEST);
        }

        // Must be exactly 6 digits
        if (!otp.matches("\\d{6}")) {
            throw new ApiException("OTP must be a 6-digit number.", HttpStatus.BAD_REQUEST);
        }

        OtpRecord otpRecord = otpRecordRepository.findByOtp(otp)
                .orElseThrow(() -> new ApiException("Invalid OTP.", HttpStatus.NOT_FOUND));

        // One-time use check
        if (otpRecord.isUsed()) {
            throw new ApiException("This OTP has already been used.", HttpStatus.GONE);
        }

        // Expiry check
        if (LocalDateTime.now().isAfter(otpRecord.getExpiresAt())) {
            throw new ApiException("OTP has expired.", HttpStatus.GONE);
        }

        FileRecord fileRecord = otpRecord.getFileRecord();

        if (fileRecord.isDeleted()) {
            throw new ApiException("The file associated with this OTP has been deleted.", HttpStatus.GONE);
        }

        // Consume the OTP
        otpRecord.setUsed(true);
        otpRecordRepository.save(otpRecord);

        // Issue a download token
        DownloadToken downloadToken = DownloadToken.builder()
                .fileRecord(fileRecord)
                .createdAt(LocalDateTime.now())
                .build();
        downloadTokenRepository.save(downloadToken);

        String downloadUrl = baseUrl + "/api/files/download/" + downloadToken.getToken();

        log.info("OTP verified for file: {} | Download token: {}", fileRecord.getOriginalFilename(), downloadToken.getToken());

        return OtpVerifyResponse.builder()
                .success(true)
                .message("OTP verified. Use the download URL to retrieve the file.")
                .downloadUrl(downloadUrl)
                .originalFilename(fileRecord.getOriginalFilename())
                .fileSizeBytes(fileRecord.getFileSize())
                .fileExpiresAt(fileRecord.getExpiresAt().format(FORMATTER))
                .note("The download link is valid until the file expires at " + fileRecord.getExpiresAt().format(FORMATTER))
                .build();
    }

    public Resource downloadFile(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException("Download token must not be empty.", HttpStatus.BAD_REQUEST);
        }

        DownloadToken downloadToken = downloadTokenRepository.findById(token)
                .orElseThrow(() -> new ApiException("Invalid download token.", HttpStatus.NOT_FOUND));

        FileRecord fileRecord = downloadToken.getFileRecord();

        // File must still be within its 10-minute upload window
        if (LocalDateTime.now().isAfter(fileRecord.getExpiresAt())) {
            throw new ApiException("The file download window has expired.", HttpStatus.GONE);
        }

        if (fileRecord.isDeleted()) {
            throw new ApiException("The file has been deleted.", HttpStatus.GONE);
        }

        try {
            Path filePath = Paths.get(fileRecord.getStoragePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ApiException("File not found on server.", HttpStatus.NOT_FOUND);
            }

            log.info("File downloaded: {}", fileRecord.getOriginalFilename());
            return resource;

        } catch (MalformedURLException e) {
            throw new ApiException("Could not resolve file path.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getContentType(String token) {
        return downloadTokenRepository.findById(token)
                .map(dt -> dt.getFileRecord().getContentType())
                .orElse("application/octet-stream");
    }

    public String getOriginalFilename(String token) {
        return downloadTokenRepository.findById(token)
                .map(dt -> dt.getFileRecord().getOriginalFilename())
                .orElse("download");
    }

    // Generates a 6-digit OTP that doesn't collide with any active (unused, not-yet-expired) OTP
    private String generateUniqueOtp() {
        String otp;
        int attempts = 0;
        do {
            otp = String.format("%06d", random.nextInt(1_000_000));
            attempts++;
            if (attempts > 100) {
                throw new ApiException("Could not generate a unique OTP. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } while (isOtpActiveInDb(otp));
        return otp;
    }

    private boolean isOtpActiveInDb(String otp) {
        return otpRecordRepository.findByOtp(otp)
                .filter(r -> !r.isUsed() && LocalDateTime.now().isBefore(r.getExpiresAt()))
                .isPresent();
    }
}
