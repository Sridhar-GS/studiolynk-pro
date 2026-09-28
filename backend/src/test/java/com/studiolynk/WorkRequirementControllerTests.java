package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.RequirementStatusUpdateDto;
import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.WorkRequirementRequestDto;
import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequirementStatus;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.EquipmentRepository;
import com.studiolynk.repository.ServiceRepository;
import com.studiolynk.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkRequirementControllerTests {

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

    private String studioToken;
    private String studioEmail;
    private String freelancerToken;
    private Long skillId;
    private Long serviceId;
    private Long equipmentId;

    @BeforeEach
    void setUp() throws Exception {
        // Register & onboard Studio
        studioEmail = "studio.req." + System.currentTimeMillis() + "@studiolynk.local";
        studioToken = registerAndGetToken(studioEmail, UserRole.STUDIO);

        StudioOnboardingRequestDto studioOnboard = new StudioOnboardingRequestDto();
        studioOnboard.setStudioName("Luminary Wedding Studios");
        studioOnboard.setOwnerName("Senthil Nathan");
        studioOnboard.setPhone("+91 98400 54321");
        studioOnboard.setAddress("Anna Nagar, Chennai, Tamil Nadu");
        studioOnboard.setLatitude(new BigDecimal("13.0850"));
        studioOnboard.setLongitude(new BigDecimal("80.2100"));
        studioOnboard.setYearsOfOperation(5);

        mockMvc.perform(post("/api/onboarding/studio")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioOnboard)))
                .andExpect(status().isOk());

        // Register Freelancer
        String freelancerEmail = "freelancer.req." + System.currentTimeMillis() + "@studiolynk.local";
        freelancerToken = registerAndGetToken(freelancerEmail, UserRole.FREELANCER);

        // Preload catalogue references
        skillId = skillRepository.findAll().get(0).getId();
        serviceId = serviceRepository.findAll().get(0).getId();
        equipmentId = equipmentRepository.findAll().get(0).getId();
    }

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
    @DisplayName("WRK-001, WRK-002: Studio creates work requirement with full specs and private client details")
    void testCreateWorkRequirement_success() throws Exception {
        WorkRequirementRequestDto request = new WorkRequirementRequestDto();
        request.setEventName("Grand Traditional Brahmin Wedding");
        request.setEventType("Wedding");
        request.setEventDate(LocalDate.now().plusDays(5));
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(17, 0));
        request.setLocation("Mayor Ramanathan Chettiar Hall, MRC Nagar, Chennai");
        request.setLatitude(new BigDecimal("13.0180"));
        request.setLongitude(new BigDecimal("80.2740"));
        request.setDayType(DayType.FULL_DAY);
        request.setBudget(new BigDecimal("18000.00"));
        request.setDescription("Looking for lead candid photographer and prime 85mm lens coverage.");
        request.setStatus(RequirementStatus.OPEN);
        request.setEventContactName("Mr. Ramaswamy (Father of Bride)");
        request.setEventContactPhone("+91 94444 88888");
        request.setRequiredSkillIds(List.of(skillId));
        request.setRequiredServiceIds(List.of(serviceId));
        request.setRequiredEquipmentIds(List.of(equipmentId));

        mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.eventName").value("Grand Traditional Brahmin Wedding"))
                .andExpect(jsonPath("$.data.eventType").value("Wedding"))
                .andExpect(jsonPath("$.data.budget").value(18000.00))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.eventContactName").value("Mr. Ramaswamy (Father of Bride)"))
                .andExpect(jsonPath("$.data.eventContactPhone").value("+91 94444 88888"))
                .andExpect(jsonPath("$.data.hasPrivateContactDetails").value(true))
                .andExpect(jsonPath("$.data.privateDetailsRevealed").value(true))
                .andExpect(jsonPath("$.data.requiredSkills[0].id").value(skillId))
                .andExpect(jsonPath("$.data.requiredServices[0].id").value(serviceId))
                .andExpect(jsonPath("$.data.requiredEquipment[0].id").value(equipmentId));
    }

    @Test
    @DisplayName("WRK-002, REQ-002, REQ-006: Private client details are hidden from other users until confirmed")
    void testPrivateClientDetails_privacyProtection() throws Exception {
        // 1. Create requirement with private details as Studio
        WorkRequirementRequestDto request = new WorkRequirementRequestDto();
        request.setEventName("Private Corporate Gala Shoot");
        request.setEventType("Corporate");
        request.setEventDate(LocalDate.now().plusDays(4));
        request.setStartTime(LocalTime.of(18, 0));
        request.setEndTime(LocalTime.of(23, 0));
        request.setLocation("ITC Grand Chola, Guindy, Chennai");
        request.setBudget(new BigDecimal("15000.00"));
        request.setEventContactName("Priya Venkatesh (Corporate VP)");
        request.setEventContactPhone("+91 99999 00000");

        MvcResult createResult = mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long requirementId = root.path("data").path("id").asLong();

        // 2. Fetch as Creator Studio -> Private details MUST be revealed
        mockMvc.perform(get("/api/requirements/" + requirementId)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventContactName").value("Priya Venkatesh (Corporate VP)"))
                .andExpect(jsonPath("$.data.eventContactPhone").value("+91 99999 00000"))
                .andExpect(jsonPath("$.data.privateDetailsRevealed").value(true));

        // 3. Fetch as Freelancer -> Private details MUST be MASKED (null)
        mockMvc.perform(get("/api/requirements/" + requirementId)
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventContactName").doesNotExist())
                .andExpect(jsonPath("$.data.eventContactPhone").doesNotExist())
                .andExpect(jsonPath("$.data.hasPrivateContactDetails").value(true))
                .andExpect(jsonPath("$.data.privateDetailsRevealed").value(false));
    }

    @Test
    @DisplayName("WRK-003: Requirement status lifecycle transitions (DRAFT -> OPEN -> CANCELLED)")
    void testRequirementStatusLifecycle() throws Exception {
        // Create as DRAFT
        WorkRequirementRequestDto request = new WorkRequirementRequestDto();
        request.setEventName("Draft Commercial Shoot");
        request.setEventType("Commercial");
        request.setEventDate(LocalDate.now().plusDays(6));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(15, 0));
        request.setLocation("Besant Nagar Beach, Chennai");
        request.setBudget(new BigDecimal("12000.00"));
        request.setStatus(RequirementStatus.DRAFT);

        MvcResult createRes = mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        long id = objectMapper.readTree(createRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Update status to OPEN
        mockMvc.perform(patch("/api/requirements/" + id + "/status")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequirementStatusUpdateDto(RequirementStatus.OPEN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OPEN"));

        // Update status to CANCELLED
        mockMvc.perform(patch("/api/requirements/" + id + "/status")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequirementStatusUpdateDto(RequirementStatus.CANCELLED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("WRK-001: Studio edits requirement details and deletes requirement")
    void testEditAndDeleteRequirement() throws Exception {
        WorkRequirementRequestDto createReq = new WorkRequirementRequestDto();
        createReq.setEventName("Outdoor Pre-Wedding Shoot");
        createReq.setEventType("Pre-Wedding");
        createReq.setEventDate(LocalDate.now().plusDays(7));
        createReq.setStartTime(LocalTime.of(6, 0));
        createReq.setEndTime(LocalTime.of(11, 0));
        createReq.setLocation("Mahabalipuram Shore Temple");
        createReq.setBudget(new BigDecimal("10000.00"));

        MvcResult createRes = mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        long id = objectMapper.readTree(createRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Update requirement
        createReq.setBudget(new BigDecimal("14000.00"));
        createReq.setEventName("Updated: Outdoor Pre-Wedding Shore Shoot");
        mockMvc.perform(put("/api/requirements/" + id)
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.budget").value(14000.00))
                .andExpect(jsonPath("$.data.eventName").value("Updated: Outdoor Pre-Wedding Shore Shoot"));

        // Delete requirement
        mockMvc.perform(delete("/api/requirements/" + id)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk());

        // Verify it is gone
        mockMvc.perform(get("/api/requirements/" + id)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("WRK-004: Studio lists their own requirements and public open feed is accessible")
    void testListStudioRequirementsAndOpenFeed() throws Exception {
        // Create 1 requirement
        WorkRequirementRequestDto req = new WorkRequirementRequestDto();
        req.setEventName("Fashion Runway Event");
        req.setEventType("Fashion");
        req.setEventDate(LocalDate.now().plusDays(3));
        req.setStartTime(LocalTime.of(17, 0));
        req.setEndTime(LocalTime.of(22, 0));
        req.setLocation("Leela Palace, Chennai");
        req.setBudget(new BigDecimal("22000.00"));
        req.setStatus(RequirementStatus.OPEN);

        mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Check studio requirements list
        mockMvc.perform(get("/api/requirements/studio/me")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));

        // Check open public feed
        mockMvc.perform(get("/api/requirements/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Validation: Inverted start and end time returns 400 Bad Request")
    void testValidation_invertedShootTimes() throws Exception {
        WorkRequirementRequestDto invalidTimes = new WorkRequirementRequestDto();
        invalidTimes.setEventName("Invalid Time Event");
        invalidTimes.setEventType("Commercial");
        invalidTimes.setEventDate(LocalDate.now().plusDays(2));
        invalidTimes.setStartTime(LocalTime.of(18, 0));
        invalidTimes.setEndTime(LocalTime.of(10, 0)); // Inverted: 18:00 before 10:00
        invalidTimes.setLocation("Chennai");
        invalidTimes.setBudget(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTimes)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Security: Freelancer cannot create a work requirement (403 Forbidden)")
    void testFreelancerCannotCreateRequirement() throws Exception {
        WorkRequirementRequestDto req = new WorkRequirementRequestDto();
        req.setEventName("Unauthorized Event");
        req.setEventType("Commercial");
        req.setEventDate(LocalDate.now().plusDays(2));
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(18, 0));
        req.setLocation("Chennai");
        req.setBudget(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
