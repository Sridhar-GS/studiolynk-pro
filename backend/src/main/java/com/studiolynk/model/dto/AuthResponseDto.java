package com.studiolynk.model.dto;

public class AuthResponseDto {
    private String token;
    private String type = "Bearer";
    private UserSummaryDto user;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String token, UserSummaryDto user) {
        this.token = token;
        this.type = "Bearer";
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserSummaryDto getUser() {
        return user;
    }

    public void setUser(UserSummaryDto user) {
        this.user = user;
    }
}
