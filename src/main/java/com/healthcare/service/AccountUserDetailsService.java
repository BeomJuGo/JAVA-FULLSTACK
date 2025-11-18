package com.healthcare.service;

import com.healthcare.repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public AccountUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var acc = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        var auth = new SimpleGrantedAuthority("ROLE_" + acc.getRole().name());
        return new User(acc.getUsername(), acc.getPassword(), List.of(auth));
    }
}
