package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.AuthResponseDto;
import com.studiolynk.model.dto.ForgotPasswordRequestDto;
import com.studiolynk.model.dto.LoginRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.ResetPasswordRequestDto;
import com.studiolynk.model.dto.UserSummaryDto;
import com.studiolynk.model.dto.VerifyOtpRequestDto;
import com.studiolynk.service.AuthService;
import com.studiolynk.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Authentication and Password Reset workflows.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, JWT token issuance, and OTP password reset")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new Studio or Freelancer account")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful. Please proceed to role onboarding.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and receive a JWT token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile and onboarding state")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserSummaryDto user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send a 6-digit password reset OTP to user's registered email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        passwordResetService.requestPasswordResetOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset OTP sent to registered email.", null));
    }

    @PostMapping("/verify-reset-otp")
    @Operation(summary = "Verify the 6-digit OTP code")
    public ResponseEntity<ApiResponse<Boolean>> verifyResetOtp(@Valid @RequestBody VerifyOtpRequestDto request) {
        boolean verified = passwordResetService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified successfully.", verified));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using verified OTP code")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully. Please log in with your new password.", null));
    }
}
