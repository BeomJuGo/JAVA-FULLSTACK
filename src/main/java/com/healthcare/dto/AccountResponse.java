package com.healthcare.dto;

public class AccountResponse {
    public Long id;
    public String username;
    public String displayName;
    public String role;
    public String status;

    public AccountResponse(Long id, String username, String displayName, String role, String status) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.status = status;
    }
}
