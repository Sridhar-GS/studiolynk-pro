package com.studiolynk.service;

import com.studiolynk.model.dto.ForgotPasswordRequestDto;
import com.studiolynk.model.dto.ResetPasswordRequestDto;
import com.studiolynk.model.dto.VerifyOtpRequestDto;

public interface PasswordResetService {
    void requestPasswordResetOtp(ForgotPasswordRequestDto request);
    boolean verifyOtp(VerifyOtpRequestDto request);
    void resetPassword(ResetPasswordRequestDto request);
}
