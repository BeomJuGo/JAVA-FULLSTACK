package com.healthcare.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.healthcare.domain.TrainerProfile;
import com.healthcare.domain.UserProfile;
import com.healthcare.dto.profile.ProfileDtos;
import com.healthcare.repository.TrainerProfileRepository;
import com.healthcare.repository.UserProfileRepository;
import com.healthcare.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProfileService {

    private final UserProfileRepository userProfileRepo;
    private final TrainerProfileRepository trainerProfileRepo;
    private final SecurityUtil securityUtil;
    private final Cloudinary cloudinary;

    public ProfileService(UserProfileRepository userProfileRepo,
                         TrainerProfileRepository trainerProfileRepo,
                         SecurityUtil securityUtil,
                         Cloudinary cloudinary) {
        this.userProfileRepo = userProfileRepo;
        this.trainerProfileRepo = trainerProfileRepo;
        this.securityUtil = securityUtil;
        this.cloudinary = cloudinary;
    }

    /**
     * 프로필 이미지 업데이트
     * 기존 이미지가 있으면 Cloudinary에서 삭제 후 새 이미지 URL 저장
     */
    @Transactional
    public void updateProfileImage(String imageUrl, String imagePublicId) {
        Long accountId = securityUtil.currentAccountId();
        String role = securityUtil.currentRole();

        if ("USER".equals(role)) {
            UserProfile profile = userProfileRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalStateException("User profile not found"));

            // 기존 이미지가 있으면 Cloudinary에서 삭제
            if (profile.getImagePublicId() != null && !profile.getImagePublicId().isEmpty()) {
                try {
                    cloudinary.uploader().destroy(profile.getImagePublicId(), ObjectUtils.emptyMap());
                } catch (Exception e) {
                    // Cloudinary 삭제 실패해도 계속 진행 (로그만 남김)
                    System.err.println("Failed to delete old image from Cloudinary: " + e.getMessage());
                }
            }

            profile.setImageUrl(imageUrl);
            profile.setImagePublicId(imagePublicId);
            userProfileRepo.save(profile);

        } else if ("TRAINER".equals(role)) {
            TrainerProfile profile = trainerProfileRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalStateException("Trainer profile not found"));

            // 기존 이미지가 있으면 Cloudinary에서 삭제
            if (profile.getImagePublicId() != null && !profile.getImagePublicId().isEmpty()) {
                try {
                    cloudinary.uploader().destroy(profile.getImagePublicId(), ObjectUtils.emptyMap());
                } catch (Exception e) {
                    // Cloudinary 삭제 실패해도 계속 진행 (로그만 남김)
                    System.err.println("Failed to delete old image from Cloudinary: " + e.getMessage());
                }
            }

            profile.setImageUrl(imageUrl);
            profile.setImagePublicId(imagePublicId);
            trainerProfileRepo.save(profile);

        } else {
            throw new IllegalStateException("Only USER or TRAINER can update profile image");
        }
    }

    @Transactional
    public void updateProfileDetails(ProfileDtos.MyProfileUpdateRequest request) {
        Long accountId = securityUtil.currentAccountId();
        String role = securityUtil.currentRole();

        if ("USER".equals(role)) {
            if (request == null || request.userProfile == null) {
                throw new IllegalArgumentException("userProfile payload is required");
            }
            UserProfile profile = userProfileRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalStateException("User profile not found"));
            applyUserProfileUpdate(profile, request.userProfile);
            userProfileRepo.save(profile);

        } else if ("TRAINER".equals(role)) {
            if (request == null || request.trainerProfile == null) {
                throw new IllegalArgumentException("trainerProfile payload is required");
            }
            TrainerProfile profile = trainerProfileRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalStateException("Trainer profile not found"));
            applyTrainerProfileUpdate(profile, request.trainerProfile);
            trainerProfileRepo.save(profile);
        } else {
            throw new IllegalStateException("Only USER or TRAINER can update profile details");
        }
    }

    private void applyUserProfileUpdate(UserProfile profile, ProfileDtos.UserProfileUpdateRequest payload) {
        profile.setGender(parseUserGender(payload.gender));
        profile.setHeightCm(toBigDecimal(payload.heightCm));
        profile.setWeightKg(toBigDecimal(payload.weightKg));
        profile.setAge(payload.age);
        profile.setActivityLevel(parseActivityLevel(payload.activityLevel));
        profile.setIntro(payload.intro);
        if (payload.isPublic != null) {
            profile.setPublic(payload.isPublic);
        }
    }

    private void applyTrainerProfileUpdate(TrainerProfile profile, ProfileDtos.TrainerProfileUpdateRequest payload) {
        profile.setGender(parseTrainerGender(payload.gender));
        profile.setHeightCm(toBigDecimal(payload.heightCm));
        profile.setWeightKg(toBigDecimal(payload.weightKg));
        profile.setAge(payload.age);
        profile.setCareerYears(payload.careerYears);
        profile.setSpecialty(payload.specialty);
        profile.setIntro(payload.intro);
    }

    private UserProfile.Gender parseUserGender(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UserProfile.Gender.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid gender value: " + value);
        }
    }

    private TrainerProfile.Gender parseTrainerGender(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TrainerProfile.Gender.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid gender value: " + value);
        }
    }

    private UserProfile.ActivityLevel parseActivityLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UserProfile.ActivityLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid activityLevel value: " + value);
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}

