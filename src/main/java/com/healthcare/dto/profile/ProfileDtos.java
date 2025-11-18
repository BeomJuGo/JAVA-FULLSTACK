package com.healthcare.dto.profile;

public class ProfileDtos {

    public static class MyProfileUpdateRequest {
        public UserProfileUpdateRequest userProfile;
        public TrainerProfileUpdateRequest trainerProfile;
    }

    public static class UserProfileUpdateRequest {
        public String gender;
        public Double heightCm;
        public Double weightKg;
        public Integer age;
        public String activityLevel;
        public String intro;
        public Boolean isPublic;
    }

    public static class TrainerProfileUpdateRequest {
        public String gender;
        public Double heightCm;
        public Double weightKg;
        public Integer age;
        public Integer careerYears;
        public String specialty;
        public String intro;
    }

    public static class UserProfileDetail {
        public String gender;
        public Double heightCm;
        public Double weightKg;
        public Integer age;
        public String activityLevel;
        public String intro;
    }

    public static class TrainerProfileDetail {
        public String gender;
        public Double heightCm;
        public Double weightKg;
        public Integer age;
        public Integer careerYears;
        public String specialty;
        public String intro;
    }

    public static class UserProfileView {
        public Long id;
        public Long accountId;
        public String username;
        public String displayName;
        public String role;
        public boolean isPublic;
        public boolean isOwner;
        public boolean visible;
        public String imageUrl;
        public String imagePublicId;
        public UserProfileDetail profile;
    }

    public static class TrainerProfileView {
        public Long id;
        public Long accountId;
        public String username;
        public String displayName;
        public String role;
        public boolean isOwner;
        public boolean visible;
        public String imageUrl;
        public String imagePublicId;
        public TrainerProfileDetail profile;
    }
}

