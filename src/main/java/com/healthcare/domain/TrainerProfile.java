package com.healthcare.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="trainer_profile")
public class TrainerProfile {
    public enum Gender { M, F, O }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="account_id", nullable=false, unique=true)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "age")
    private Integer age;

    @Column(name = "career_years")
    private Integer careerYears;

    @Column(name = "specialty", length = 200)
    private String specialty;

    @Column(name = "intro", length = 500)
    private String intro;

    @Column(name="image_url", length=500)
    private String imageUrl;

    @Column(name="image_public_id", length=255)
    private String imagePublicId;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate public void onUpdate(){ this.updatedAt = LocalDateTime.now(); }

    public Long getId(){ return id; }
    public Long getAccountId(){ return accountId; }
    public void setAccountId(Long accountId){ this.accountId = accountId; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public BigDecimal getHeightCm() { return heightCm; }
    public void setHeightCm(BigDecimal heightCm) { this.heightCm = heightCm; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Integer getCareerYears() { return careerYears; }
    public void setCareerYears(Integer careerYears) { this.careerYears = careerYears; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getImageUrl(){ return imageUrl; }
    public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl; }
    public String getImagePublicId(){ return imagePublicId; }
    public void setImagePublicId(String imagePublicId){ this.imagePublicId = imagePublicId; }
}
