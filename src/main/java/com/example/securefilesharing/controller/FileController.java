package com.example.securefilesharing.controller;

import com.example.securefilesharing.dto.OtpVerifyResponse;
import com.example.securefilesharing.dto.UploadResponse;
import com.example.securefilesharing.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * POST /api/files/upload
     * Upload a file (max 50 MB). Returns a 6-digit numeric OTP that the
     * recipient can use within 10 minutes to get a download link.
     * Form-data key: "file"
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        UploadResponse response = fileService.uploadFile(file);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/files/verify-otp?otp=123456
     * Provide the 6-digit OTP. If valid (not expired, not used) you receive
     * a one-time download URL. The OTP is consumed immediately.
     */
    @GetMapping("/verify-otp")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(@RequestParam("otp") String otp) {
        OtpVerifyResponse response = fileService.verifyOtp(otp);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/files/download/{token}
     * Download the file using the token returned by /verify-otp.
     * Valid until the file's 10-minute upload window expires — even if the
     * OTP itself was used earlier (scenario: OTP used at t+3min, download at t+6min is still valid).
     */
    @GetMapping("/download/{token}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String token) {
        Resource resource = fileService.downloadFile(token);
        String contentType = fileService.getContentType(token);
        String filename = fileService.getOriginalFilename(token);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
