package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class TokenRefreshRequest {
    
    @NotBlank
    private String refreshToken;

    
    public TokenRefreshRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}