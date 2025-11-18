package com.healthcare.dto.upload;

import jakarta.validation.constraints.*;

public class UploadDtos {

    /** 클라가 직접 업로드 후 서버에 메타 저장 */
    public static class SaveRequest {
        @NotBlank public String publicId;
        @NotBlank public String secureUrl;

        @Size(max=255) public String originalName;
        @Size(max=50) public String resourceType; // image|video|raw
        @Size(max=50) public String format;

        @PositiveOrZero public Long bytesSize;
        @PositiveOrZero public Integer width;
        @PositiveOrZero public Integer height;

        @Size(max=255) public String folder;
        @Size(max=500) public String context; // e.g. "COMMUNITY:postId=123"
    }

    /** 서버 서명 응답 (프론트 업로드 용 파라미터) */
    public static class SignResponse {
        public String apiKey;
        public Long timestamp;
        public String signature;
        public String folder;
        public String cloudName;
        public String thumbTransformation; // 프론트에서 eager 변환 용도

        public SignResponse(String apiKey, Long timestamp, String signature,
                            String folder, String cloudName, String thumb) {
            this.apiKey = apiKey;
            this.timestamp = timestamp;
            this.signature = signature;
            this.folder = folder;
            this.cloudName = cloudName;
            this.thumbTransformation = thumb;
        }
    }
}
