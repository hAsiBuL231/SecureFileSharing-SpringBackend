package com.example.securefilesharing.dto;

public class OtpVerifyResponse {
    private boolean success;
    private String message;
    private String downloadUrl;
    private String originalFilename;
    private long fileSizeBytes;
    private String fileExpiresAt;
    private String note;

    private OtpVerifyResponse() {}

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getOriginalFilename() { return originalFilename; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public String getFileExpiresAt() { return fileExpiresAt; }
    public String getNote() { return note; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final OtpVerifyResponse r = new OtpVerifyResponse();
        public Builder success(boolean v) { r.success = v; return this; }
        public Builder message(String v) { r.message = v; return this; }
        public Builder downloadUrl(String v) { r.downloadUrl = v; return this; }
        public Builder originalFilename(String v) { r.originalFilename = v; return this; }
        public Builder fileSizeBytes(long v) { r.fileSizeBytes = v; return this; }
        public Builder fileExpiresAt(String v) { r.fileExpiresAt = v; return this; }
        public Builder note(String v) { r.note = v; return this; }
        public OtpVerifyResponse build() { return r; }
    }
}
