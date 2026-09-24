package com.studiolynk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.ForgotPasswordRequestDto;
import com.studiolynk.model.dto.LoginRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.ResetPasswordRequestDto;
import com.studiolynk.model.dto.VerifyOtpRequestDto;
import com.studiolynk.model.entity.PasswordResetOtp;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.PasswordResetOtpRepository;
import com.studiolynk.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetOtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("AUTH-001 & AUTH-007: Register new Studio with strong password")
    void testRegisterSuccess() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "studio.test." + System.currentTimeMillis() + "@studiolynk.local",
                "Studio@Pass123!",
                UserRole.STUDIO
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.role").value("STUDIO"))
                .andExpect(jsonPath("$.data.user.onboardingCompleted").value(false));
    }

    @Test
    @DisplayName("AUTH-007: Register fails when password does not meet complexity requirements")
    void testRegisterWeakPasswordFails() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "weak.pass@studiolynk.local",
                "weakpass",
                UserRole.FREELANCER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("Register fails on duplicate email")
    void testRegisterDuplicateEmailFails() throws Exception {
        String email = "duplicate." + System.currentTimeMillis() + "@studiolynk.local";
        RegisterRequestDto request = new RegisterRequestDto(email, "Secure@Pass2026", UserRole.FREELANCER);

        // First registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("AUTH-002: Login with valid credentials returns JWT token")
    void testLoginSuccess() throws Exception {
        String email = "login.test." + System.currentTimeMillis() + "@studiolynk.local";
        String rawPassword = "Strong#Pass2026";
        User user = new User(email, passwordEncoder.encode(rawPassword), UserRole.FREELANCER);
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto(email, rawPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.email").value(email))
                .andExpect(jsonPath("$.data.user.role").value("FREELANCER"));
    }

    @Test
    @DisplayName("Login with invalid password fails with 400 Bad Request")
    void testLoginInvalidPasswordFails() throws Exception {
        String email = "badlogin." + System.currentTimeMillis() + "@studiolynk.local";
        User user = new User(email, passwordEncoder.encode("RealPassword!1"), UserRole.STUDIO);
        userRepository.save(user);

        LoginRequestDto loginRequest = new LoginRequestDto(email, "WrongPassword!1");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    @DisplayName("AUTH-008: GET /api/auth/me requires valid JWT Bearer token")
    void testGetMeWithToken() throws Exception {
        String email = "me.test." + System.currentTimeMillis() + "@studiolynk.local";
        String rawPassword = "Valid!Password1";
        User user = new User(email, passwordEncoder.encode(rawPassword), UserRole.STUDIO);
        userRepository.save(user);

        // Login to get token
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email, rawPassword))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Access protected /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.role").value("STUDIO"));
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401")
    void testUnauthenticatedAccessFails() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("AUTH-003, AUTH-005, AUTH-006: Password reset OTP flow and password update")
    void testPasswordResetFlow() throws Exception {
        String email = "reset." + System.currentTimeMillis() + "@studiolynk.local";
        User user = new User(email, passwordEncoder.encode("Original#Password1"), UserRole.STUDIO);
        userRepository.save(user);

        // 1. Request OTP
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequestDto(email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Find the generated OTP in repository
        PasswordResetOtp otpRecord = otpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElse(null);
        assertThat(otpRecord).isNotNull();
        assertThat(otpRecord.getExpiresAt()).isAfter(Instant.now());

        // 2. Mock a test OTP code and update hash for deterministic verification test
        String testOtp = "654321";
        otpRecord.setOtpHash(passwordEncoder.encode(testOtp));
        otpRepository.save(otpRecord);

        // 3. Verify OTP
        mockMvc.perform(post("/api/auth/verify-reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyOtpRequestDto(email, testOtp))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        // 4. Reset Password
        String newPassword = "NewPassword#2026!";
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequestDto(email, testOtp, newPassword))))
                .andExpect(status().isOk());

        // 5. Verify user can log in with new password
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email, newPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString());
    }

    @Test
    @DisplayName("ONB-001 & ONB-002: Check /api/onboarding/status indicates pending onboarding")
    void testOnboardingStatusCheck() throws Exception {
        String email = "onboard." + System.currentTimeMillis() + "@studiolynk.local";
        String rawPassword = "Onboard#Password1";
        User user = new User(email, passwordEncoder.encode(rawPassword), UserRole.FREELANCER);
        user.setOnboardingCompleted(false);
        userRepository.save(user);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email, rawPassword))))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        mockMvc.perform(get("/api/onboarding/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("FREELANCER"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false))
                .andExpect(jsonPath("$.data.redirectUrl").value("/onboarding/freelancer"));
    }
}
