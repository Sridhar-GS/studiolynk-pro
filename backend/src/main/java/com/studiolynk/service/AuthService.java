package com.studiolynk.service;

import com.studiolynk.model.dto.AuthResponseDto;
import com.studiolynk.model.dto.LoginRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.UserSummaryDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto request);
    AuthResponseDto login(LoginRequestDto request);
    UserSummaryDto getCurrentUser(String email);
}
