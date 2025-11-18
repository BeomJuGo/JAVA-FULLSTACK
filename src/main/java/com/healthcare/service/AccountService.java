package com.healthcare.service;

import com.healthcare.domain.Account;
import com.healthcare.domain.TrainerProfile;
import com.healthcare.domain.UserProfile;
import com.healthcare.dto.auth.AuthDtos;
import com.healthcare.repository.AccountRepository;
import com.healthcare.repository.TrainerProfileRepository;
import com.healthcare.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;
    private final TrainerProfileRepository trainerProfileRepository;

    public AccountService(AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder,
                          UserProfileRepository userProfileRepository,
                          TrainerProfileRepository trainerProfileRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.userProfileRepository = userProfileRepository;
        this.trainerProfileRepository = trainerProfileRepository;
    }

    @Transactional
    public Account signup(AuthDtos.SignupRequest req) {
        if (req.username == null || req.username.isBlank()) {
            throw new IllegalArgumentException("username is blank");
        }
        if (req.password == null || req.password.isBlank()) {
            throw new IllegalArgumentException("password is blank");
        }
        if (accountRepository.findByUsername(req.username).isPresent()) {
            throw new IllegalArgumentException("username already exists");
        }

        var acc = new Account();
        acc.setUsername(req.username);
        acc.setPassword(passwordEncoder.encode(req.password));
        // displayName 설정 (username을 기본값으로 사용)
        String displayName = (req.displayName != null && !req.displayName.isBlank()) ? req.displayName : req.username;
        acc.setDisplayName(displayName);
        
        // 기본 USER
        Account.Role role = Account.Role.USER;
        if (req.role != null && !req.role.isBlank()) {
            role = Account.Role.valueOf(req.role.toUpperCase());
        }
        acc.setRole(role);

        Account saved = accountRepository.save(acc);

        // 프로필 생성 (역할에 따라)
        switch (role) {
            case USER -> {
                UserProfile userProfile = new UserProfile();
                userProfile.setAccountId(saved.getId());
                userProfile.setPublic(true);
                if (req.userProfile != null) {
                    mapUserProfile(userProfile, req.userProfile);
                }
                userProfileRepository.save(userProfile);
            }
            case TRAINER -> {
                TrainerProfile trainerProfile = new TrainerProfile();
                trainerProfile.setAccountId(saved.getId());
                if (req.trainerProfile != null) {
                    mapTrainerProfile(trainerProfile, req.trainerProfile);
                }
                trainerProfileRepository.save(trainerProfile);
            }
            case ADMIN -> {
                // ADMIN은 프로필 생성 안 함
            }
        }

        return saved;
    }

    private void mapUserProfile(UserProfile profile, AuthDtos.UserProfilePayload payload) {
        if (payload.gender != null && !payload.gender.isBlank()) {
            try {
                profile.setGender(UserProfile.Gender.valueOf(payload.gender.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("invalid gender value: " + payload.gender);
            }
        }
        if (payload.heightCm != null) {
            profile.setHeightCm(BigDecimal.valueOf(payload.heightCm));
        }
        if (payload.weightKg != null) {
            profile.setWeightKg(BigDecimal.valueOf(payload.weightKg));
        }
        if (payload.age != null) {
            profile.setAge(payload.age);
        }
        if (payload.activityLevel != null && !payload.activityLevel.isBlank()) {
            try {
                profile.setActivityLevel(UserProfile.ActivityLevel.valueOf(payload.activityLevel.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("invalid activityLevel value: " + payload.activityLevel);
            }
        }
        if (payload.intro != null) {
            profile.setIntro(payload.intro);
        }
        if (payload.isPublic != null) {
            profile.setPublic(payload.isPublic);
        }
    }

    private void mapTrainerProfile(TrainerProfile profile, AuthDtos.TrainerProfilePayload payload) {
        if (payload.gender != null && !payload.gender.isBlank()) {
            try {
                profile.setGender(TrainerProfile.Gender.valueOf(payload.gender.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("invalid gender value: " + payload.gender);
            }
        }
        if (payload.heightCm != null) {
            profile.setHeightCm(BigDecimal.valueOf(payload.heightCm));
        }
        if (payload.weightKg != null) {
            profile.setWeightKg(BigDecimal.valueOf(payload.weightKg));
        }
        if (payload.age != null) {
            profile.setAge(payload.age);
        }
        if (payload.careerYears != null) {
            profile.setCareerYears(payload.careerYears);
        }
        if (payload.specialty != null) {
            profile.setSpecialty(payload.specialty);
        }
        if (payload.intro != null) {
            profile.setIntro(payload.intro);
        }
    }

    @Transactional(readOnly = true)
    public Account login(AuthDtos.LoginRequest req) {
        var acc = accountRepository.findByUsername(req.username)
                .orElseThrow(() -> new IllegalArgumentException("invalid credentials"));

        if (!passwordEncoder.matches(req.password, acc.getPassword())) {
            throw new IllegalArgumentException("invalid credentials");
        }
        return acc;
    }
}
