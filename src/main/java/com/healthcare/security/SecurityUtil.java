package com.healthcare.security;

import com.healthcare.domain.Account;
import com.healthcare.repository.AccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityUtil {

    private final AccountRepository accountRepository;

    public SecurityUtil(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /** 로그인한 username (없으면 예외) */
    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new IllegalStateException("unauthenticated");
        return auth.getName();
    }

    /** 로그인한 Account (없으면 예외) */
    public Account currentAccount() {
        String username = currentUsername();
        return accountRepository.findByUsername(username).orElseThrow();
    }

    /** 로그인한 accountId */
    public Long currentAccountId() {
        return currentAccount().getId();
    }

    /** 현재 Role 문자열 (USER/TRAINER/ADMIN) */
    public String currentRole() {
        return currentAccount().getRole().name();
    }

    /** 로그인한 accountId (호환성을 위한 alias) */
    public Long getCurrentAccountId() {
        return currentAccountId();
    }

    /** 현재 역할 집합 (호환성을 위한 메서드) */
    public Set<String> getCurrentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Set.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)     // "ROLE_USER"
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .collect(Collectors.toSet());
    }
}
