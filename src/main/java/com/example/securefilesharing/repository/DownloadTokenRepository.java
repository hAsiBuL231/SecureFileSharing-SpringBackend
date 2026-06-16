package com.example.securefilesharing.repository;

import com.example.securefilesharing.entity.DownloadToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadTokenRepository extends JpaRepository<DownloadToken, String> {
}
