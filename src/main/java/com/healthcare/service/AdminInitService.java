package com.healthcare.service;

import com.healthcare.domain.Account;
import com.healthcare.repository.AccountRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시스템 시작 시 관리자 계정을 자동으로 생성하는 서비스
 */
@Service
public class AdminInitService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_DISPLAY_NAME = "관리자";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitService(AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void initAdmin() {
        // 관리자 계정이 이미 존재하는지 확인
        var existingAccount = accountRepository.findByUsername(ADMIN_USERNAME);
        
        if (existingAccount.isPresent()) {
            var account = existingAccount.get();
            // 기존 계정이 있지만 비밀번호가 올바르지 않을 수 있으므로 업데이트
            if (account.getRole() != Account.Role.ADMIN) {
                account.setRole(Account.Role.ADMIN);
            }
            if (account.getStatus() != Account.Status.ACTIVE) {
                account.setStatus(Account.Status.ACTIVE);
            }
            // 비밀번호를 올바른 BCrypt 해시로 업데이트
            account.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            accountRepository.save(account);
            System.out.println("[AdminInitService] 관리자 계정 비밀번호가 업데이트되었습니다.");
            return;
        }

        // 관리자 계정 생성
        var account = new Account();
        account.setUsername(ADMIN_USERNAME);
        account.setPassword(passwordEncoder.encode(ADMIN_PASSWORD)); // 올바른 BCrypt 해시 생성
        account.setRole(Account.Role.ADMIN);
        account.setDisplayName(ADMIN_DISPLAY_NAME);
        account.setStatus(Account.Status.ACTIVE);
        
        accountRepository.save(account);
        System.out.println("[AdminInitService] 관리자 계정이 생성되었습니다. (username: admin, password: admin123)");
    }
}

