package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.CustomEquipmentRequestDto;
import com.studiolynk.model.dto.CustomItemRequestDto;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.FreelancerUpdateRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.entity.EquipmentCategory;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.EquipmentCategoryRepository;
import com.studiolynk.repository.EquipmentRepository;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.ServiceRepository;
import com.studiolynk.repository.SkillRepository;
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
class FreelancerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentCategoryRepository equipmentCategoryRepository;

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
    @DisplayName("FRL-001, FRL-007, ONB-002: Complete Freelancer Onboarding and Verify Immediate Access")
    void testFreelancerOnboardingSuccess() throws Exception {
        String email = "freelancer.onboard." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);

        // Preload sample catalogue IDs from Flyway seeds
        List<Long> skillIds = skillRepository.findAll().stream().limit(2).map(Skill::getId).toList();
        List<Long> serviceIds = serviceRepository.findAll().stream().limit(2).map(ServiceEntity::getId).toList();
        List<Long> equipIds = equipmentRepository.findAll().stream().limit(2).map(com.studiolynk.model.entity.Equipment::getId).toList();

        FreelancerOnboardingRequestDto request = new FreelancerOnboardingRequestDto();
        request.setFullName("Aravind Swaminathan");
        request.setPhone("+91 94444 12345");
        request.setAddress("14, Nageswara Rao Park, Mylapore, Chennai, Tamil Nadu 600004");
        request.setLatitude(new BigDecimal("13.03340000"));
        request.setLongitude(new BigDecimal("80.26760000"));
        request.setExperienceYears(4);
        request.setBio("Specializing in candid wedding photography and cinematic 4K drone cinematography.");
        request.setProfilePhotoUrl("https://assets.studiolynk.local/profiles/aravind.jpg");
        request.setFullDayRate(new BigDecimal("15000.00"));
        request.setHalfDayRate(new BigDecimal("8500.00"));
        request.setSkillIds(skillIds);
        request.setServiceIds(serviceIds);
        request.setEquipmentIds(equipIds);

        mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Aravind Swaminathan"))
                .andExpect(jsonPath("$.data.phone").value("+91 94444 12345"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.completionPercentage").value(100))
                .andExpect(jsonPath("$.data.fullDayRate").value(15000.00))
                .andExpect(jsonPath("$.data.halfDayRate").value(8500.00))
                .andExpect(jsonPath("$.data.skills").isArray())
                .andExpect(jsonPath("$.data.services").isArray())
                .andExpect(jsonPath("$.data.equipment").isArray());

        // Verify user entity is unlocked without requiring admin verification (FRL-007)
        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.isOnboardingCompleted()).isTrue();
    }

    @Test
    @DisplayName("ONB-004: Save Freelancer Onboarding Draft without Marking Complete")
    void testFreelancerOnboardingDraft() throws Exception {
        String email = "freelancer.draft." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto request = new FreelancerOnboardingRequestDto();
        request.setFullName("Draft Freelancer");
        request.setPhone("+91 91234 56789");
        request.setAddress("Koramangala, Bangalore");

        mockMvc.perform(put("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Draft Freelancer"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false));

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.isOnboardingCompleted()).isFalse();
    }

    @Test
    @DisplayName("FRL-001: Non-Freelancer user cannot submit Freelancer Onboarding")
    void testNonFreelancerUserCannotSubmitFreelancerOnboarding() throws Exception {
        String email = "studio.caller." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.STUDIO);

        FreelancerOnboardingRequestDto request = new FreelancerOnboardingRequestDto();
        request.setFullName("Studio Guy");
        request.setPhone("+91 99999 11111");
        request.setAddress("Besant Nagar, Chennai");

        mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Only Freelancer accounts")));
    }

    @Test
    @DisplayName("FRL-002, FRL-003, FRL-005: Create Custom Skill, Service, and Equipment")
    void testCreateCustomCatalogueItems() throws Exception {
        String email = "catalogue.creator." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);

        // Custom Skill (FRL-002)
        CustomItemRequestDto skillRequest = new CustomItemRequestDto("Underwater Cinematography");
        mockMvc.perform(post("/api/skills/custom")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(skillRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Underwater Cinematography"))
                .andExpect(jsonPath("$.data.custom").value(true));

        // Custom Service (FRL-003)
        CustomItemRequestDto serviceRequest = new CustomItemRequestDto("Live Virtual Reality Wedding Broadcast");
        mockMvc.perform(post("/api/services/custom")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Live Virtual Reality Wedding Broadcast"))
                .andExpect(jsonPath("$.data.custom").value(true));

        // Custom Equipment (FRL-005)
        EquipmentCategory category = equipmentCategoryRepository.findAll().get(0);
        CustomEquipmentRequestDto equipRequest = new CustomEquipmentRequestDto(category.getId(), "RED V-Raptor 8K VV");
        mockMvc.perform(post("/api/equipment/custom")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(equipRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("RED V-Raptor 8K VV"))
                .andExpect(jsonPath("$.data.custom").value(true));
    }

    @Test
    @DisplayName("FRL-006: Retrieve and Edit Freelancer Profile (/api/freelancers/me)")
    void testGetAndEditFreelancerProfile() throws Exception {
        String email = "freelancer.edit." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);

        // Initial Onboarding
        FreelancerOnboardingRequestDto onboard = new FreelancerOnboardingRequestDto();
        onboard.setFullName("Deepak Raj");
        onboard.setPhone("+91 97777 66666");
        onboard.setAddress("Velachery Main Road, Chennai");

        mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboard)))
                .andExpect(status().isOk());

        // Get /api/freelancers/me
        mockMvc.perform(get("/api/freelancers/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Deepak Raj"))
                .andExpect(jsonPath("$.data.phone").value("+91 97777 66666"));

        // Update profile (FRL-006)
        FreelancerUpdateRequestDto update = new FreelancerUpdateRequestDto();
        update.setFullName("Deepak Rajan");
        update.setPhone("+91 97777 55555");
        update.setAddress("Adyar, Chennai, Tamil Nadu 600020");
        update.setExperienceYears(6);
        update.setBio("Senior commercial and fashion portrait photographer with global editorial features.");
        update.setFullDayRate(new BigDecimal("22000.00"));
        update.setHalfDayRate(new BigDecimal("12000.00"));

        mockMvc.perform(put("/api/freelancers/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Deepak Rajan"))
                .andExpect(jsonPath("$.data.phone").value("+91 97777 55555"))
                .andExpect(jsonPath("$.data.experienceYears").value(6))
                .andExpect(jsonPath("$.data.fullDayRate").value(22000.00));
    }
}
