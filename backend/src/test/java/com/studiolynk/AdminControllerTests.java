package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.LoginRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.StudioIdentitySubmissionDto;
import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.WorkRequirementRequestDto;
import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.EquipmentRepository;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.ServiceRepository;
import com.studiolynk.repository.SkillRepository;
import com.studiolynk.repository.StudioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Admin Platform (ADM-001 - ADM-006).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    private String adminToken;
    private String studioToken;
    private String freelancerToken;
    private Long studioId;
    private Long freelancerId;
    private Long requirementId;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Admin login (seeded by AdminDataInitializer) (ADM-002)
        LoginRequestDto adminLogin = new LoginRequestDto("admin@studiolynk.com", "Admin@2026!");
        MvcResult adminRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode adminJson = objectMapper.readTree(adminRes.getResponse().getContentAsString());
        adminToken = adminJson.path("data").path("token").asText();
        assertThat(adminJson.path("data").path("user").path("role").asText()).isEqualTo("ADMIN");

        // 2. Studio registration & onboarding
        String studioEmail = "studio.adm." + ts + "@studiolynk.local";
        studioToken = registerAndGetToken(studioEmail, UserRole.STUDIO);

        StudioOnboardingRequestDto studioOnboard = new StudioOnboardingRequestDto();
        studioOnboard.setStudioName("Apex Visual Studios " + ts);
        studioOnboard.setOwnerName("Raghavan Sundaram");
        studioOnboard.setPhone("+91 98401 11223");
        studioOnboard.setAddress("Alwarpet, Chennai, Tamil Nadu");
        studioOnboard.setLatitude(new BigDecimal("13.0334"));
        studioOnboard.setLongitude(new BigDecimal("80.2528"));
        studioOnboard.setYearsOfOperation(7);
        studioOnboard.setDeclarationText("I hereby declare that I am the authorized owner of Apex Visual Studios.");
        studioOnboard.setDocumentType("GSTIN_CERTIFICATE");
        studioOnboard.setDocumentUrl("https://studiolynk-test.s3.ap-south-1.amazonaws.com/test-gst.pdf");

        mockMvc.perform(post("/api/onboarding/studio")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studioOnboard)))
                .andExpect(status().isOk());

        studioId = studioRepository.findAll().stream()
                .filter(s -> s.getStudioName().equals("Apex Visual Studios " + ts))
                .findFirst()
                .orElseThrow()
                .getId();

        // 3. Freelancer registration & onboarding
        String freelancerEmail = "creator.adm." + ts + "@studiolynk.local";
        freelancerToken = registerAndGetToken(freelancerEmail, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto freelancerOnboard = new FreelancerOnboardingRequestDto();
        freelancerOnboard.setFullName("Ananya Krishnan " + ts);
        freelancerOnboard.setPhone("+91 98402 33445");
        freelancerOnboard.setAddress("Adyar, Chennai, Tamil Nadu");
        freelancerOnboard.setLatitude(new BigDecimal("13.0067"));
        freelancerOnboard.setLongitude(new BigDecimal("80.2570"));
        freelancerOnboard.setBio("Cinematographer & Candid Wedding Specialist");
        freelancerOnboard.setExperienceYears(5);
        freelancerOnboard.setFullDayRate(new BigDecimal("14000"));
        freelancerOnboard.setHalfDayRate(new BigDecimal("8000"));

        Long skillId = skillRepository.findAll().get(0).getId();
        Long serviceId = serviceRepository.findAll().get(0).getId();
        Long equipmentId = equipmentRepository.findAll().get(0).getId();

        freelancerOnboard.setSkillIds(List.of(skillId));
        freelancerOnboard.setServiceIds(List.of(serviceId));
        freelancerOnboard.setEquipmentIds(List.of(equipmentId));

        mockMvc.perform(post("/api/onboarding/freelancer")
                .header("Authorization", "Bearer " + freelancerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(freelancerOnboard)))
                .andExpect(status().isOk());

        freelancerId = freelancerRepository.findAll().stream()
                .filter(f -> f.getFullName().equals("Ananya Krishnan " + ts))
                .findFirst()
                .orElseThrow()
                .getId();

        // 4. Create sample work requirement
        WorkRequirementRequestDto reqDto = new WorkRequirementRequestDto();
        reqDto.setEventName("Corporate Conclave 2026 " + ts);
        reqDto.setEventType("Corporate");
        reqDto.setEventDate(LocalDate.now().plusDays(5));
        reqDto.setStartTime(LocalTime.of(10, 0));
        reqDto.setEndTime(LocalTime.of(18, 0));
        reqDto.setLocation("ITC Grand Chola, Guindy, Chennai");
        reqDto.setLatitude(new BigDecimal("13.0102"));
        reqDto.setLongitude(new BigDecimal("80.2156"));
        reqDto.setDayType(DayType.FULL_DAY);
        reqDto.setBudget(new BigDecimal("15000"));
        reqDto.setDescription("High profile leadership summit photography");
        reqDto.setRequiredSkillIds(List.of(skillId));
        reqDto.setRequiredServiceIds(List.of(serviceId));
        reqDto.setRequiredEquipmentIds(List.of(equipmentId));

        MvcResult reqRes = mockMvc.perform(post("/api/requirements")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andReturn();

        requirementId = objectMapper.readTree(reqRes.getResponse().getContentAsString()).path("data").path("id").asLong();
    }

    private String registerAndGetToken(String email, UserRole role) throws Exception {
        RegisterRequestDto reg = new RegisterRequestDto();
        reg.setEmail(email);
        reg.setPassword("StudioLynk@2026#Sec");
        reg.setRole(role);

        MvcResult res = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(res.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    @Test
    void testAdminDashboardMetrics_Success() throws Exception {
        // ADM-001, ADM-003: Aggregated monitoring dashboard
        mockMvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudios", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalFreelancers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalRequirements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.recentActivity", notNullValue()));
    }

    @Test
    void testNonAdminAccess_Forbidden() throws Exception {
        // ADM-001: Only users with ROLE_ADMIN may access /api/admin/**
        mockMvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedAccess_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminGetAllStudios_Success() throws Exception {
        // ADM-003, ADM-005: Studios view-only list
        mockMvc.perform(get("/api/admin/studios")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].studioName", notNullValue()))
                .andExpect(jsonPath("$.data[0].email", notNullValue()))
                .andExpect(jsonPath("$.data[0].identityStatus", notNullValue()));
    }

    @Test
    void testAdminGetAllFreelancers_Success() throws Exception {
        // ADM-003: Freelancers view-only list
        mockMvc.perform(get("/api/admin/freelancers")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].fullName", notNullValue()))
                .andExpect(jsonPath("$.data[0].fullDayRate", notNullValue()))
                .andExpect(jsonPath("$.data[0].skills", notNullValue()));
    }

    @Test
    void testAdminGetAllRequirements_Success() throws Exception {
        // ADM-003: Requirements view-only list
        mockMvc.perform(get("/api/admin/requirements")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].eventName", notNullValue()))
                .andExpect(jsonPath("$.data[0].budget", notNullValue()));
    }

    @Test
    void testAdminGetAllRequests_Success() throws Exception {
        // ADM-003: Requests view-only list
        mockMvc.perform(get("/api/admin/requests")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    void testAdminGetActivity_Success() throws Exception {
        // ADM-003: Activity event audit log
        mockMvc.perform(get("/api/admin/activity?limit=20")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    void testAdminStudioIdentitySubmissionMonitoring_Success() throws Exception {
        // ADM-005: Studio identity declaration monitoring
        // Admin fetches studio identity submission
        mockMvc.perform(get("/api/admin/studios/" + studioId + "/identity")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studioId").value(studioId))
                .andExpect(jsonPath("$.data.documentType").value("GSTIN_CERTIFICATE"))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"));

        // Admin fetches all identity submissions
        mockMvc.perform(get("/api/admin/identity-submissions")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    void testAdminNoMutationEndpointsExist_EnforcingAdm004() throws Exception {
        // ADM-004: Admin has no general edit/delete capability.
        // Mutation HTTP methods on mapped /api/admin/** routes must return 405 Method Not Allowed.
        mockMvc.perform(delete("/api/admin/studios")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/api/admin/freelancers")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(delete("/api/admin/requirements")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(put("/api/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(post("/api/admin/activity")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }
}
