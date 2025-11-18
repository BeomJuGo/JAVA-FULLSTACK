package com.healthcare.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.healthcare.domain.Upload;
import com.healthcare.dto.upload.UploadDtos;
import com.healthcare.repository.UploadRepository;
import com.healthcare.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UploadService {

    private final Cloudinary cloudinary;
    private final UploadRepository uploadRepo;
    private final SecurityUtil security;

    @Value("${cloudinary.api-key}")
    private String apiKey;
    @Value("${cloudinary.cloud-name}")
    private String cloudName;
    @Value("${cloudinary.folder}")
    private String folder;
    @Value("${cloudinary.thumb-transformation}")
    private String thumbTransformation;

    public UploadService(Cloudinary cloudinary, UploadRepository uploadRepo, SecurityUtil security) {
        this.cloudinary = cloudinary;
        this.uploadRepo = uploadRepo;
        this.security = security;
    }

    /** 프론트가 업로드할 때 필요한 timestamp/signature 발급 (서명 업로드) */
    public com.healthcare.dto.upload.UploadDtos.SignResponse sign() {
        long ts = System.currentTimeMillis() / 1000L;
        // 주의: 필요한 파라미터만 signature에 포함시켜야 함
        Map<String, Object> params = ObjectUtils.asMap(
                "timestamp", ts,
                "folder", folder
        );
        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        return new UploadDtos.SignResponse(
                apiKey, ts, signature, folder, cloudName, thumbTransformation
        );
    }

    /** 업로드 메타 저장 (클라가 업로드 성공 후 호출) */
    @Transactional
    public Long saveMeta(UploadDtos.SaveRequest req) {
        Long accountId = security.getCurrentAccountId();

        // 중복 public_id 방지
        uploadRepo.findByPublicId(req.publicId).ifPresent(u -> {
            throw new IllegalStateException("이미 등록된 public_id 입니다.");
        });

        var u = new Upload();
        u.setUploaderAccountId(accountId);
        u.setPublicId(req.publicId);
        u.setSecureUrl(req.secureUrl);
        u.setOriginalName(req.originalName);
        u.setResourceType(req.resourceType);
        u.setFormat(req.format);
        u.setBytesSize(req.bytesSize);
        u.setWidth(req.width);
        u.setHeight(req.height);
        u.setFolder(req.folder);
        u.setContext(req.context);

        return uploadRepo.save(u).getId();
    }

    /** 숨김/해제 */
    @Transactional
    public void setHidden(Long id, boolean hidden) {
        var u = uploadRepo.findById(id).orElseThrow();
        // 업로더 본인 또는 ADMIN만 허용 (간단 검증)
        Long accountId = security.getCurrentAccountId();
        if (!u.getUploaderAccountId().equals(accountId)) {
            // 추후 ActorGuard.isAdmin() 연동 가능
            throw new com.healthcare.security.ForbiddenException("권한이 없습니다.");
        }
        u.setHidden(hidden);
    }

    /** Cloudinary에서 삭제 + 로컬 메타 삭제 */
    @Transactional
    public void delete(Long id) {
        var u = uploadRepo.findById(id).orElseThrow();
        Long accountId = security.getCurrentAccountId();
        if (!u.getUploaderAccountId().equals(accountId)) {
            throw new com.healthcare.security.ForbiddenException("권한이 없습니다.");
        }

        try {
            cloudinary.uploader().destroy(u.getPublicId(), ObjectUtils.emptyMap());
        } catch (Exception e) {
            // Cloudinary 삭제 실패해도 메타 제거 여부는 정책에 맞게 결정
            // 여기서는 예외 그대로 던져 롤백
            throw new IllegalStateException("Cloudinary 삭제 실패: " + e.getMessage());
        }

        u.setDeletedAt(LocalDateTime.now());
        uploadRepo.delete(u);
    }
}
