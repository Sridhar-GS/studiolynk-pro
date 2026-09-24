package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.StudioSocialLinkDto;
import com.studiolynk.model.dto.StudioUpdateRequestDto;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.StudioRepository;
import com.studiolynk.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudioControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudioRepository studioRepository;

    private String registerAndGetToken(String email, UserRole role) throws Exception {
        RegisterRequestDto registerDto = new RegisterRequestDto(email, "StrongPass@2026!", role);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    @Test
    @DisplayName("STU-001 & STU-004: Complete Studio Onboarding and Verify Platform Unlocked")
    void testStudioOnboardingSuccess() throws Exception {
        String email = "studio.onboard." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.STUDIO);

        StudioOnboardingRequestDto request = new StudioOnboardingRequestDto();
        request.setStudioName("Lumina Studios");
        request.setOwnerName("Karthik Raman");
        request.setPhone("+91 98765 43210");
        request.setAddress("42, Anna Salai, Thousand Lights, Chennai, Tamil Nadu 600002");
        request.setLatitude(new BigDecimal("13.05830000"));
        request.setLongitude(new BigDecimal("80.25200000"));
        request.setYearsOfOperation(5);
        request.setLogoUrl("https://assets.studiolynk.local/logos/lumina.jpg");
        request.setSocialLinks(List.of(
                new StudioSocialLinkDto("Instagram", "https://instagram.com/luminastudios"),
                new StudioSocialLinkDto("Website", "https://luminastudios.com")
        ));
        request.setDocumentType("STUDIO_REGISTRATION_CERTIFICATE");
        request.setDocumentUrl("https://assets.studiolynk.local/docs/cert-01.pdf");
        request.setDeclarationText("I hereby declare that Lumina Studios is a registered business operating ethically in Chennai.");

        mockMvc.perform(post("/api/onboarding/studio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studioName").value("Lumina Studios"))
                .andExpect(jsonPath("$.data.ownerName").value("Karthik Raman"))
                .andExpect(jsonPath("$.data.phone").value("+91 98765 43210"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.completionPercentage").value(100))
                .andExpect(jsonPath("$.data.socialLinks").isArray())
                .andExpect(jsonPath("$.data.identitySubmission.status").value("VERIFIED"));

        // Verify in database
        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.isOnboardingCompleted()).isTrue();
    }

    @Test
    @DisplayName("ONB-004: Save Studio Onboarding Draft without Marking Complete")
    void testStudioOnboardingDraft() throws Exception {
        String email = "studio.draft." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.STUDIO);

        StudioOnboardingRequestDto request = new StudioOnboardingRequestDto();
        request.setStudioName("Draft Photo Studio");
        request.setOwnerName("Sridhar G");
        request.setPhone("+91 98765 11111");
        request.setAddress("MG Road, Bangalore");

        mockMvc.perform(put("/api/onboarding/studio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studioName").value("Draft Photo Studio"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false));

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.isOnboardingCompleted()).isFalse();
    }

    @Test
    @DisplayName("STU-001: Non-Studio Account cannot submit Studio Onboarding")
    void testNonStudioUserCannotSubmitStudioOnboarding() throws Exception {
        String email = "freelancer.fake." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);

        StudioOnboardingRequestDto request = new StudioOnboardingRequestDto();
        request.setStudioName("Fake Studio");
        request.setOwnerName("Fake Owner");
        request.setPhone("+91 98765 00000");
        request.setAddress("Test Address");

        mockMvc.perform(post("/api/onboarding/studio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Only Studio accounts")));
    }

    @Test
    @DisplayName("STU-005: Retrieve and Edit Studio Profile (/api/studios/me)")
    void testGetAndEditStudioProfile() throws Exception {
        String email = "studio.edit." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.STUDIO);

        // First complete onboarding
        StudioOnboardingRequestDto onboard = new StudioOnboardingRequestDto();
        onboard.setStudioName("Original Studio Name");
        onboard.setOwnerName("Original Owner");
        onboard.setPhone("+91 99999 88888");
        onboard.setAddress("Old Address, Chennai");

        mockMvc.perform(post("/api/onboarding/studio")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboard)))
                .andExpect(status().isOk());

        // Get /api/studios/me
        mockMvc.perform(get("/api/studios/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studioName").value("Original Studio Name"));

        // Update profile (STU-005)
        StudioUpdateRequestDto update = new StudioUpdateRequestDto();
        update.setStudioName("Apex Creative Studios");
        update.setOwnerName("Original Owner");
        update.setPhone("+91 99999 77777");
        update.setAddress("New High-Tech Studio Hub, OMR, Chennai");
        update.setYearsOfOperation(8);
        update.setLatitude(new BigDecimal("12.9716"));
        update.setLongitude(new BigDecimal("80.2450"));
        update.setSocialLinks(List.of(
                new StudioSocialLinkDto("Instagram", "https://instagram.com/apexcreatives")
        ));

        mockMvc.perform(put("/api/studios/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studioName").value("Apex Creative Studios"))
                .andExpect(jsonPath("$.data.phone").value("+91 99999 77777"))
                .andExpect(jsonPath("$.data.yearsOfOperation").value(8));
    }
}
