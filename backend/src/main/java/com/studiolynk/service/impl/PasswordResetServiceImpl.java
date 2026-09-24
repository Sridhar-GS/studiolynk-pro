package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.ForgotPasswordRequestDto;
import com.studiolynk.model.dto.ResetPasswordRequestDto;
import com.studiolynk.model.dto.VerifyOtpRequestDto;
import com.studiolynk.model.entity.PasswordResetOtp;
import com.studiolynk.model.entity.User;
import com.studiolynk.repository.PasswordResetOtpRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.EmailService;
import com.studiolynk.service.PasswordResetService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Service managing password reset OTP generation, throttling, verification, and password updates.
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetOtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void requestPasswordResetOtp(ForgotPasswordRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();

        // Verify account exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account registered with email: " + email));

        // Enforce resend cooldown (AUTH-006)
        Optional<PasswordResetOtp> latestOtpOpt = otpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email);
        if (latestOtpOpt.isPresent()) {
            PasswordResetOtp latestOtp = latestOtpOpt.get();
            long secondsSinceCreation = ChronoUnit.SECONDS.between(latestOtp.getCreatedAt(), Instant.now());
            if (secondsSinceCreation < RESEND_COOLDOWN_SECONDS) {
                long waitSeconds = RESEND_COOLDOWN_SECONDS - secondsSinceCreation;
                throw new BadRequestException("Please wait " + waitSeconds + " seconds before requesting another OTP code.");
            }
            // Invalidate older unused OTP
            latestOtp.setUsed(true);
            otpRepository.save(latestOtp);
        }

        // Generate 6-digit OTP
        int code = 100000 + secureRandom.nextInt(900000);
        String otpString = String.valueOf(code);

        // Store hashed OTP
        String hashedOtp = passwordEncoder.encode(otpString);
        Instant expiresAt = Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        PasswordResetOtp otpRecord = new PasswordResetOtp(email, hashedOtp, expiresAt);
        otpRepository.save(otpRecord);

        // Send via SMTP
        emailService.sendOtpEmail(email, otpString);
    }

    @Override
    @Transactional
    public boolean verifyOtp(VerifyOtpRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();

        PasswordResetOtp otpRecord = otpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("No active OTP request found. Please request a new code."));

        // Check expiry (AUTH-005)
        if (otpRecord.getExpiresAt().isBefore(Instant.now())) {
            otpRecord.setUsed(true);
            otpRepository.save(otpRecord);
            throw new BadRequestException("OTP code has expired. Please request a new one.");
        }

        // Check attempts (AUTH-006)
        if (otpRecord.getAttempts() >= MAX_ATTEMPTS) {
            otpRecord.setUsed(true);
            otpRepository.save(otpRecord);
            throw new BadRequestException("Maximum verification attempts exceeded. Please request a new OTP code.");
        }

        otpRecord.setAttempts(otpRecord.getAttempts() + 1);

        if (!passwordEncoder.matches(otp, otpRecord.getOtpHash())) {
            otpRepository.save(otpRecord);
            int remaining = MAX_ATTEMPTS - otpRecord.getAttempts();
            throw new BadRequestException("Invalid OTP code. " + remaining + " attempt(s) remaining.");
        }

        otpRepository.save(otpRecord);
        return true;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();
        String newPassword = request.getNewPassword();

        // Validate OTP
        verifyOtp(new VerifyOtpRequestDto(email, otp));

        // Find user and update password
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark OTP as used
        PasswordResetOtp otpRecord = otpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElse(null);
        if (otpRecord != null) {
            otpRecord.setUsed(true);
            otpRepository.save(otpRecord);
        }
    }
}
