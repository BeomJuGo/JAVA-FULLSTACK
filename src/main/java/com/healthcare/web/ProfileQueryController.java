package com.healthcare.web;

import com.healthcare.domain.Account;
import com.healthcare.domain.TrainerProfile;
import com.healthcare.domain.UserProfile;
import com.healthcare.dto.profile.ProfileDtos;
import com.healthcare.repository.AccountRepository;
import com.healthcare.repository.TrainerProfileRepository;
import com.healthcare.repository.UserProfileRepository;
import com.healthcare.security.SecurityUtil;
import com.healthcare.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfileQueryController {

    private final UserProfileRepository userRepo;
    private final TrainerProfileRepository trainerRepo;
    private final AccountRepository accountRepo;
    private final SecurityUtil securityUtil;
    private final ProfileService profileService;

    public ProfileQueryController(UserProfileRepository userRepo, 
                                 TrainerProfileRepository trainerRepo,
                                 AccountRepository accountRepo,
                                 SecurityUtil securityUtil,
                                 ProfileService profileService) {
        this.userRepo = userRepo;
        this.trainerRepo = trainerRepo;
        this.accountRepo = accountRepo;
        this.securityUtil = securityUtil;
        this.profileService = profileService;
    }

    // 현재 사용자의 프로필 정보 조회
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile() {
        Long accountId = securityUtil.currentAccountId();
        String role = securityUtil.currentRole();
        Account account = securityUtil.currentAccount();
        
        Map<String, Object> result = new HashMap<>();
        result.put("accountId", accountId);
        result.put("username", account.getUsername());
        result.put("displayName", account.getDisplayName());
        result.put("role", role);
        
        if ("USER".equals(role)) {
            var userProfile = userRepo.findByAccountId(accountId);
            if (userProfile.isPresent()) {
                result.put("profileId", userProfile.get().getId());
                result.put("profile", userProfile.get());
            }
        } else if ("TRAINER".equals(role)) {
            var trainerProfile = trainerRepo.findByAccountId(accountId);
            if (trainerProfile.isPresent()) {
                result.put("profileId", trainerProfile.get().getId());
                result.put("profile", trainerProfile.get());
            }
        }
        
        return ResponseEntity.ok(result);
    }

    // 유저 프로필 (Account 정보 포함)
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> findAllUsers() {
        List<UserProfile> users = userRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (UserProfile user : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("accountId", user.getAccountId());
            map.put("isPublic", user.isPublic());
            accountRepo.findById(user.getAccountId()).ifPresent(account -> {
                map.put("username", account.getUsername());
                map.put("displayName", account.getDisplayName());
            });
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ProfileDtos.UserProfileView> findUser(@PathVariable Long id) {
        return userRepo.findById(id).map(user -> {
            var account = accountRepo.findById(user.getAccountId()).orElse(null);
            Long viewerId = safeCurrentAccountId();
            boolean isOwner = viewerId != null && viewerId.equals(user.getAccountId());
            boolean visible = user.isPublic() || isOwner;

            ProfileDtos.UserProfileView view = new ProfileDtos.UserProfileView();
            view.id = user.getId();
            view.accountId = user.getAccountId();
            view.isPublic = user.isPublic();
            view.isOwner = isOwner;
            view.visible = visible;
            if (account != null) {
                view.username = account.getUsername();
                view.displayName = account.getDisplayName();
                view.role = account.getRole().name();
            }
            if (visible) {
                view.imageUrl = user.getImageUrl();
                view.imagePublicId = user.getImagePublicId();
                ProfileDtos.UserProfileDetail detail = new ProfileDtos.UserProfileDetail();
                detail.gender = user.getGender() != null ? user.getGender().name() : null;
                detail.heightCm = user.getHeightCm() != null ? user.getHeightCm().doubleValue() : null;
                detail.weightKg = user.getWeightKg() != null ? user.getWeightKg().doubleValue() : null;
                detail.age = user.getAge();
                detail.activityLevel = user.getActivityLevel() != null ? user.getActivityLevel().name() : null;
                detail.intro = user.getIntro();
                view.profile = detail;
            }
            return ResponseEntity.ok(view);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 트레이너 프로필 (Account 정보 포함)
    @GetMapping("/trainers")
    public ResponseEntity<List<Map<String, Object>>> findAllTrainers() {
        List<TrainerProfile> trainers = trainerRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TrainerProfile trainer : trainers) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", trainer.getId());
            map.put("accountId", trainer.getAccountId());
            accountRepo.findById(trainer.getAccountId()).ifPresent(account -> {
                map.put("username", account.getUsername());
                map.put("displayName", account.getDisplayName());
            });
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/trainers/{id}")
    public ResponseEntity<ProfileDtos.TrainerProfileView> findTrainer(@PathVariable Long id) {
        return trainerRepo.findById(id).map(trainer -> {
            var account = accountRepo.findById(trainer.getAccountId()).orElse(null);
            Long viewerId = safeCurrentAccountId();
            boolean isOwner = viewerId != null && viewerId.equals(trainer.getAccountId());
            boolean visible = true; // 트레이너는 기본적으로 공개

            ProfileDtos.TrainerProfileView view = new ProfileDtos.TrainerProfileView();
            view.id = trainer.getId();
            view.accountId = trainer.getAccountId();
            view.isOwner = isOwner;
            view.visible = visible;
            if (account != null) {
                view.username = account.getUsername();
                view.displayName = account.getDisplayName();
                view.role = account.getRole().name();
            }
            if (visible) {
                view.imageUrl = trainer.getImageUrl();
                view.imagePublicId = trainer.getImagePublicId();
                ProfileDtos.TrainerProfileDetail detail = new ProfileDtos.TrainerProfileDetail();
                detail.gender = trainer.getGender() != null ? trainer.getGender().name() : null;
                detail.heightCm = trainer.getHeightCm() != null ? trainer.getHeightCm().doubleValue() : null;
                detail.weightKg = trainer.getWeightKg() != null ? trainer.getWeightKg().doubleValue() : null;
                detail.age = trainer.getAge();
                detail.careerYears = trainer.getCareerYears();
                detail.specialty = trainer.getSpecialty();
                detail.intro = trainer.getIntro();
                view.profile = detail;
            }

            return ResponseEntity.ok(view);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/me/display-name")
    public ResponseEntity<Void> updateDisplayName(@RequestBody Map<String, String> body) {
        String displayName = body.get("displayName");
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName is required");
        }

        Long accountId = securityUtil.currentAccountId();
        var account = accountRepo.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Account not found"));
        account.setDisplayName(displayName);
        accountRepo.save(account);
        return ResponseEntity.noContent().build();
    }

    /**
     * 프로필 이미지 업데이트
     * 요청 본문: { "imageUrl": "...", "imagePublicId": "..." }
     */
    @PatchMapping("/me/image")
    public ResponseEntity<Void> updateProfileImage(@RequestBody Map<String, String> body) {
        String imageUrl = body.get("imageUrl");
        String imagePublicId = body.get("imagePublicId");

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("imageUrl is required");
        }
        if (imagePublicId == null || imagePublicId.isBlank()) {
            throw new IllegalArgumentException("imagePublicId is required");
        }

        profileService.updateProfileImage(imageUrl, imagePublicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<Void> updateMyProfile(@RequestBody ProfileDtos.MyProfileUpdateRequest request) {
        profileService.updateProfileDetails(request);
        return ResponseEntity.noContent().build();
    }

    private Long safeCurrentAccountId() {
        try {
            return securityUtil.currentAccountId();
        } catch (Exception ex) {
            return null;
        }
    }
}
