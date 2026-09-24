package com.studiolynk.model.dto;

import com.studiolynk.model.enums.UserRole;

public class UserSummaryDto {
    private Long id;
    private String email;
    private UserRole role;
    private boolean onboardingCompleted;

    public UserSummaryDto() {
    }

    public UserSummaryDto(Long id, String email, UserRole role, boolean onboardingCompleted) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.onboardingCompleted = onboardingCompleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }
}
