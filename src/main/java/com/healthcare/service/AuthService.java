package com.healthcare.service;

import com.healthcare.domain.Account;
import com.healthcare.domain.TrainerProfile;
import com.healthcare.domain.UserProfile;
import com.healthcare.dto.SignupRequest;
import com.healthcare.repository.AccountRepository;
import com.healthcare.repository.TrainerProfileRepository;
import com.healthcare.repository.UserProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountRepository accountRepository,
                       UserProfileRepository userProfileRepository,
                       TrainerProfileRepository trainerProfileRepository,
                       PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.userProfileRepository = userProfileRepository;
        this.trainerProfileRepository = trainerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Account signup(SignupRequest req) {
        if (accountRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }

        Account account = new Account();
        account.setUsername(req.getUsername());
        // ✅ BCrypt 암호화
        account.setPassword(passwordEncoder.encode(req.getPassword()));
        account.setDisplayName(
                (req.getDisplayName() == null || req.getDisplayName().isBlank())
                        ? req.getUsername() : req.getDisplayName()
        );

        // ✅ 기본값 USER 처리
        String roleStr = (req.getRole() == null || req.getRole().isBlank()) ? "USER" : req.getRole();
        Account.Role role = Account.Role.valueOf(roleStr.toUpperCase());
        account.setRole(role);

        Account saved = accountRepository.save(account);

        // ✅ 프로필 생성 (ID 저장 방식)
        switch (role) {
            case USER -> {
                UserProfile up = new UserProfile();
                up.setAccountId(saved.getId());   // ⬅️ setAccount(...)가 아니라 setAccountId(...)
                up.setPublic(true);               // 기본 공개
                userProfileRepository.save(up);
            }
            case TRAINER -> {
                TrainerProfile tp = new TrainerProfile();
                tp.setAccountId(saved.getId());   // ⬅️ 동일
                trainerProfileRepository.save(tp);
            }
            case ADMIN -> { /* 프로필 생성 안 함 */ }
        }
        return saved;
    }
}
