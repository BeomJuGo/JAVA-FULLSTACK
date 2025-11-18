package com.healthcare.web;

import com.healthcare.dto.upload.UploadDtos;
import com.healthcare.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }

    /** 업로드 서명 발급 (USER/TRAINER 모두 가능) */
    @PreAuthorize("hasAnyRole('USER','TRAINER','ADMIN')")
    @PostMapping("/sign")
    public ResponseEntity<UploadDtos.SignResponse> sign() {
        return ResponseEntity.ok(service.sign());
    }

    /** 업로드 성공 후 서버에 메타 저장 */
    @PreAuthorize("hasAnyRole('USER','TRAINER','ADMIN')")
    @PostMapping
    public ResponseEntity<Long> saveMeta(@Valid @RequestBody UploadDtos.SaveRequest req) {
        return ResponseEntity.ok(service.saveMeta(req));
    }

    /** 숨김/해제 (업로더 본인만) */
    @PreAuthorize("hasAnyRole('USER','TRAINER','ADMIN')")
    @PostMapping("/{id}/hidden")
    public ResponseEntity<Void> hide(@PathVariable Long id, @RequestParam boolean hidden) {
        service.setHidden(id, hidden);
        return ResponseEntity.noContent().build();
    }

    /** 삭제 (업로더 본인만) */
    @PreAuthorize("hasAnyRole('USER','TRAINER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
