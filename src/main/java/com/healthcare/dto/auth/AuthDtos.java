package com.healthcare.dto.auth;

public class AuthDtos {

    // 회원가입 요청
    public static class SignupRequest {
        public String username;
        public String password;
        public String role; // "USER" / "TRAINER" / "ADMIN"
        public String displayName;
        public UserProfilePayload userProfile;
        public TrainerProfilePayload trainerProfile;
    }

    public static class UserProfilePayload {
        public String gender;
        public Double heightCm;
        public Double weightKg;
        public Integer age;
        public String activityLevel;
        public String intro;
        public Boolean isPublic;
    }

    public static class TrainerProfilePayload {
        public String gender;
        public Double heightCm;
        public Double weightKg;
        public Integer age;
        public Integer careerYears;
        public String specialty;
        public String intro;
    }

    // 로그인 요청
    public static class LoginRequest {
        public String username;
        public String password;
    }

    // 토큰 응답
    public static class TokenResponse {
        public String token;
        public String username;
        public String role;
        public String displayName;

        public TokenResponse(String token, String username, String role, String displayName) {
            this.token = token;
            this.username = username;
            this.role = role;
            this.displayName = displayName;
        }
    }
}
