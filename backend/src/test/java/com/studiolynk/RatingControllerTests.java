package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.*;
import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequirementStatus;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RatingControllerTests {

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
    private FreelancerRepository freelancerRepository;

    @Autowired
    private StudioRepository studioRepository;

    private String studioToken;
    private String freelancerToken;
    private Long studioId;
    private Long freelancerId;
    private Long requirementId;
    private Long requestId;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Studio registration & onboarding
        String studioEmail = "studio.rating." + ts + "@studiolynk.local";
        studioToken = registerAndGetToken(studioEmail, UserRole.STUDIO);

        StudioOnboardingRequestDto studioOnboard = new StudioOnboardingRequestDto();
        studioOnboard.setStudioName("Lumina Media Lab " + ts);
        studioOnboard.setOwnerName("Senthil Nathan");
        studioOnboard.setPhone("+91 98401 55667");
        studioOnboard.setAddress("T. Nagar, Chennai, Tamil Nadu");
        studioOnboard.setLatitude(new BigDecimal("13.0418"));
        studioOnboard.setLongitude(new BigDecimal("80.2341"));
        studioOnboard.setYearsOfOperation(5);

        mockMvc.perform(post("/api/onboarding/studio")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studioOnboard)))
                .andExpect(status().isOk());

        studioId = studioRepository.findAll().stream()
                .filter(s -> s.getStudioName().equals("Lumina Media Lab " + ts))
                .findFirst()
                .orElseThrow()
                .getId();

        // 2. Freelancer registration & onboarding
        String freelancerEmail = "creator.rating." + ts + "@studiolynk.local";
        freelancerToken = registerAndGetToken(freelancerEmail, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto freelancerOnboard = new FreelancerOnboardingRequestDto();
        freelancerOnboard.setFullName("Karthik Raja " + ts);
        freelancerOnboard.setPhone("+91 98402 77889");
        freelancerOnboard.setAddress("Mylapore, Chennai, Tamil Nadu");
        freelancerOnboard.setLatitude(new BigDecimal("13.0420"));
        freelancerOnboard.setLongitude(new BigDecimal("80.2350"));
        freelancerOnboard.setBio("Award-winning portrait and event photographer");
        freelancerOnboard.setExperienceYears(4);
        freelancerOnboard.setFullDayRate(new BigDecimal("12000"));
        freelancerOnboard.setHalfDayRate(new BigDecimal("7000"));

        Long skillId = skillRepository.findAll().get(0).getId();
        Long serviceId = serviceRepository.findAll().get(0).getId();
        Long equipmentIdVal = equipmentRepository.findAll().get(0).getId();

        freelancerOnboard.setSkillIds(List.of(skillId));
        freelancerOnboard.setServiceIds(List.of(serviceId));
        freelancerOnboard.setEquipmentIds(List.of(equipmentIdVal));

        mockMvc.perform(post("/api/onboarding/freelancer")
                .header("Authorization", "Bearer " + freelancerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(freelancerOnboard)))
                .andExpect(status().isOk());

        freelancerId = freelancerRepository.findAll().stream()
                .filter(f -> f.getFullName().equals("Karthik Raja " + ts))
                .findFirst()
                .orElseThrow()
                .getId();

        // 3. Create work requirement
        WorkRequirementRequestDto reqDto = new WorkRequirementRequestDto();
        reqDto.setEventName("Destination Wedding Shoot " + ts);
        reqDto.setEventType("Wedding");
        reqDto.setEventDate(LocalDate.now().plusDays(3));
        reqDto.setStartTime(LocalTime.of(9, 0));
        reqDto.setEndTime(LocalTime.of(19, 0));
        reqDto.setLocation("Mahabalipuram Beach Resort, Chennai");
        reqDto.setDayType(DayType.FULL_DAY);
        reqDto.setBudget(new BigDecimal("25000"));
        reqDto.setDescription("Full-day candid and traditional wedding coverage");
        reqDto.setRequiredSkillIds(List.of(skillId));
        reqDto.setRequiredServiceIds(List.of(serviceId));
        reqDto.setRequiredEquipmentIds(List.of(equipmentIdVal));

        MvcResult reqResult = mockMvc.perform(post("/api/requirements")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode rootReq = objectMapper.readTree(reqResult.getResponse().getContentAsString());
        requirementId = rootReq.path("data").path("id").asLong();

        // 4. Send work request to freelancer
        CreateWorkRequestDto requestDto = new CreateWorkRequestDto();
        requestDto.setRequirementId(requirementId);
        requestDto.setFreelancerId(freelancerId);
        requestDto.setOfferedPrice(new BigDecimal("22000"));
        requestDto.setMessage("We would love to hire you for our wedding shoot");

        MvcResult createReqRes = mockMvc.perform(post("/api/requests")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode rootWorkReq = objectMapper.readTree(createReqRes.getResponse().getContentAsString());
        requestId = rootWorkReq.path("data").path("id").asLong();

        // 5. Freelancer accepts work request
        mockMvc.perform(patch("/api/requests/" + requestId + "/accept")
                .header("Authorization", "Bearer " + freelancerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AcceptRequestDto(new BigDecimal("22000")))))
                .andExpect(status().isOk());

        // 6. Studio confirms booking
        mockMvc.perform(patch("/api/requests/" + requestId + "/confirm")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk());
    }

    private void advanceRequirementToCompleted() throws Exception {
        // Move to IN_PROGRESS
        RequirementStatusUpdateDto inProgressUpdate = new RequirementStatusUpdateDto();
        inProgressUpdate.setStatus(RequirementStatus.IN_PROGRESS);
        mockMvc.perform(patch("/api/requirements/" + requirementId + "/status")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inProgressUpdate)))
                .andExpect(status().isOk());

        // Move to COMPLETED
        RequirementStatusUpdateDto completedUpdate = new RequirementStatusUpdateDto();
        completedUpdate.setStatus(RequirementStatus.COMPLETED);
        mockMvc.perform(patch("/api/requirements/" + requirementId + "/status")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completedUpdate)))
                .andExpect(status().isOk());
    }

    @Test
    void testCannotRateBeforeWorkIsCompleted() throws Exception {
        // Requirement is currently CONFIRMED, not COMPLETED
        RatingSubmissionDto ratingDto = new RatingSubmissionDto(5, "Prompt and professional!");

        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testStudioRatesFreelancerSuccess() throws Exception {
        advanceRequirementToCompleted();

        RatingSubmissionDto submission = new RatingSubmissionDto(5, "Exceptional composition and punctual delivery!");

        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.score", is(5)))
                .andExpect(jsonPath("$.data.targetType", is("FREELANCER")))
                .andExpect(jsonPath("$.data.reviewText", is("Exceptional composition and punctual delivery!")));

        // Public freelancer ratings check (RAT-003)
        mockMvc.perform(get("/api/freelancers/" + freelancerId + "/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRatings", is(1)))
                .andExpect(jsonPath("$.data.averageRating", is(5.0)))
                .andExpect(jsonPath("$.data.ratings", hasSize(1)))
                .andExpect(jsonPath("$.data.ratings[0].score", is(5)));
    }

    @Test
    void testFreelancerRatesStudioSuccess() throws Exception {
        advanceRequirementToCompleted();

        RatingSubmissionDto submission = new RatingSubmissionDto(4, "Great coordination, clear brief, and timely settlement.");

        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + freelancerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.score", is(4)))
                .andExpect(jsonPath("$.data.targetType", is("STUDIO")))
                .andExpect(jsonPath("$.data.reviewText", is("Great coordination, clear brief, and timely settlement.")));

        // Public studio ratings check (RAT-003)
        mockMvc.perform(get("/api/studios/" + studioId + "/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRatings", is(1)))
                .andExpect(jsonPath("$.data.averageRating", is(4.0)))
                .andExpect(jsonPath("$.data.ratings", hasSize(1)))
                .andExpect(jsonPath("$.data.ratings[0].score", is(4)));
    }

    @Test
    void testCannotRateTwiceInSameDirection() throws Exception {
        advanceRequirementToCompleted();

        RatingSubmissionDto submission = new RatingSubmissionDto(5, "Initial rating submission.");

        // First rating succeeds
        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isCreated());

        // Second rating from same user for same requirement must fail with 409 Conflict (RAT-006)
        RatingSubmissionDto duplicateSubmission = new RatingSubmissionDto(4, "Attempting second review.");
        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateSubmission)))
                .andExpect(status().isConflict());
    }

    @Test
    void testBothDirectionsPermittedForCompletedWork() throws Exception {
        advanceRequirementToCompleted();

        // 1. Studio rates Freelancer
        RatingSubmissionDto studioSubmission = new RatingSubmissionDto(5, "Fantastic output!");
        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studioSubmission)))
                .andExpect(status().isCreated());

        // 2. Freelancer rates Studio (one rating per direction - RAT-006)
        RatingSubmissionDto freelancerSubmission = new RatingSubmissionDto(5, "Smooth shoot experience!");
        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + freelancerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(freelancerSubmission)))
                .andExpect(status().isCreated());
    }

    @Test
    void testUnrelatedUserCannotRateRequirement() throws Exception {
        advanceRequirementToCompleted();

        // Create unrelated studio user
        long ts = System.currentTimeMillis();
        String otherStudioToken = registerAndGetToken("unrelated." + ts + "@studiolynk.local", UserRole.STUDIO);

        RatingSubmissionDto submission = new RatingSubmissionDto(5, "Trying to rate unrelated project.");
        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + otherStudioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRatingScoreValidation() throws Exception {
        advanceRequirementToCompleted();

        // Score 0 (below min 1)
        RatingSubmissionDto zeroScore = new RatingSubmissionDto(0, "Invalid zero score");
        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroScore)))
                .andExpect(status().isBadRequest());

        // Score 6 (above max 5)
        RatingSubmissionDto sixScore = new RatingSubmissionDto(6, "Invalid 6 score");
        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sixScore)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRequirementRatingStatus() throws Exception {
        // Initially not completed
        mockMvc.perform(get("/api/requirements/" + requirementId + "/ratings/status")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canRate", is(false)))
                .andExpect(jsonPath("$.data.alreadyRated", is(false)));

        // Advance to COMPLETED
        advanceRequirementToCompleted();

        mockMvc.perform(get("/api/requirements/" + requirementId + "/ratings/status")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canRate", is(true)))
                .andExpect(jsonPath("$.data.alreadyRated", is(false)));

        // Submit rating
        RatingSubmissionDto submission = new RatingSubmissionDto(5, "Terrific work!");
        mockMvc.perform(post("/api/requirements/" + requirementId + "/ratings")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isCreated());

        // Check rating status again: alreadyRated must be true, canRate must be false
        mockMvc.perform(get("/api/requirements/" + requirementId + "/ratings/status")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canRate", is(false)))
                .andExpect(jsonPath("$.data.alreadyRated", is(true)))
                .andExpect(jsonPath("$.data.myRating.score", is(5)));
    }

    @Test
    void testPublicRatingsEndpointsWithoutAuth() throws Exception {
        // Freelancer public ratings accessible without auth (RAT-003)
        mockMvc.perform(get("/api/freelancers/" + freelancerId + "/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRatings", is(0)));

        // Studio public ratings accessible without auth (RAT-003)
        mockMvc.perform(get("/api/studios/" + studioId + "/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRatings", is(0)));
    }

    private String registerAndGetToken(String email, UserRole role) throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail(email);
        dto.setPassword("Secret123#Pass");
        dto.setRole(role);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }
}
