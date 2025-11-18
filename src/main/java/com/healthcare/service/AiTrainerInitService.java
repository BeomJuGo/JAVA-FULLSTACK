package com.healthcare.service;

import com.healthcare.domain.Account;
import com.healthcare.domain.TrainerProfile;
import com.healthcare.repository.AccountRepository;
import com.healthcare.repository.TrainerProfileRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시스템 시작 시 AI 트레이너 계정 및 프로필을 자동으로 생성하는 서비스
 */
@Service
public class AiTrainerInitService {

    private static final String AI_TRAINER_USERNAME = "ai_trainer";
    private static final String AI_TRAINER_DISPLAY_NAME = "AI 트레이너";

    private final AccountRepository accountRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AiTrainerInitService(AccountRepository accountRepository,
                               TrainerProfileRepository trainerProfileRepository,
                               PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.trainerProfileRepository = trainerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void initAiTrainer() {
        // AI 트레이너 계정이 이미 존재하는지 확인
        var existingAccount = accountRepository.findByUsername(AI_TRAINER_USERNAME);
        
        if (existingAccount.isPresent()) {
            // 이미 존재하면 프로필만 확인
            var account = existingAccount.get();
            trainerProfileRepository.findByAccountId(account.getId())
                    .orElseGet(() -> {
                        // 계정은 있지만 프로필이 없으면 생성
                        var profile = new TrainerProfile();
                        profile.setAccountId(account.getId());
                        profile.setSpecialty("AI 기반 맞춤형 운동 및 식단 추천");
                        profile.setIntro("GPT를 활용하여 개인 맞춤형 운동 계획과 식단을 추천해드립니다.");
                        profile.setCareerYears(0); // AI는 경력 없음
                        return trainerProfileRepository.save(profile);
                    });
            return;
        }

        // AI 트레이너 계정 생성
        var account = new Account();
        account.setUsername(AI_TRAINER_USERNAME);
        account.setPassword(passwordEncoder.encode("ai_trainer_secure_password_never_used")); // 실제로는 로그인 불가
        account.setRole(Account.Role.TRAINER);
        account.setDisplayName(AI_TRAINER_DISPLAY_NAME);
        account.setStatus(Account.Status.ACTIVE);
        
        var savedAccount = accountRepository.save(account);

        // AI 트레이너 프로필 생성
        var profile = new TrainerProfile();
        profile.setAccountId(savedAccount.getId());
        profile.setSpecialty("AI 기반 맞춤형 운동 및 식단 추천");
        profile.setIntro("GPT를 활용하여 개인 맞춤형 운동 계획과 식단을 추천해드립니다.");
        profile.setCareerYears(0); // AI는 경력 없음
        
        trainerProfileRepository.save(profile);
    }

    /**
     * AI 트레이너 계정 ID를 조회
     */
    @Transactional(readOnly = true)
    public Long getAiTrainerAccountId() {
        return accountRepository.findByUsername(AI_TRAINER_USERNAME)
                .map(Account::getId)
                .orElseThrow(() -> new IllegalStateException("AI 트레이너 계정이 초기화되지 않았습니다."));
    }

    /**
     * AI 트레이너 프로필 ID를 조회
     */
    @Transactional(readOnly = true)
    public Long getAiTrainerProfileId() {
        var accountId = getAiTrainerAccountId();
        return trainerProfileRepository.findByAccountId(accountId)
                .map(TrainerProfile::getId)
                .orElseThrow(() -> new IllegalStateException("AI 트레이너 프로필이 초기화되지 않았습니다."));
    }
}

