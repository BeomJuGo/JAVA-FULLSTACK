// src/main/java/com/healthcare/dto/plan/PlanUpdateDto.java
package com.healthcare.dto.plan;

import jakarta.validation.constraints.*;

public record PlanUpdateDto(
        @NotBlank(message = "플랜 제목은 필수입니다.")
        @Size(max = 50, message = "제목은 50자를 초과할 수 없습니다.")
        String title,

        @NotNull(message = "주차는 필수입니다.")
        @Min(value = 1, message = "주차는 1 이상이어야 합니다.")
        @Max(value = 52, message = "주차는 52 이하여야 합니다.")
        Integer week
) {}
