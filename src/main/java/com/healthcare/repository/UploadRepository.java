package com.healthcare.repository;

import com.healthcare.domain.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UploadRepository extends JpaRepository<Upload, Long> {
    Optional<Upload> findByPublicId(String publicId);
}
