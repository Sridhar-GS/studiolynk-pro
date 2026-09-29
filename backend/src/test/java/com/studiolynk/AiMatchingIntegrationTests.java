package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.FreelancerCardDto;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.WorkRequirementRequestDto;
import com.studiolynk.model.dto.ml.MatchFeaturesDto;
import com.studiolynk.model.entity.*;
import com.studiolynk.model.enums.*;
import com.studiolynk.repository.*;
import com.studiolynk.service.AiMatchingService;
import com.studiolynk.service.MlServiceClient;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 15 AI Ranking & Discovery Integration Tests.
 * Covers DIS-002, DIS-006, ML-012, ML-013, POR-009, WRK-005.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AiMatchingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiMatchingService aiMatchingService;

    @Autowired
    private MlServiceClient mlServiceClient;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private WorkRequirementRepository requirementRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private FreelancerAvailabilityRepository availabilityRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioCategoryRepository categoryRepository;

    @Autowired
    private RatingRepository ratingRepository;

    private String studioToken;
    private Long requirementId;
    private Long freelancerAId;
    private Long freelancerBId;
    private Freelancer freelancerA;
    private Freelancer freelancerB;
    private WorkRequirement requirement;

    private Long candidSkillId;
    private Long droneSkillId;
    private Long weddingServiceId;
    private Long cameraEquipId;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Catalogue items
        List<ServiceEntity> services = serviceRepository.findAll();
        weddingServiceId = services.get(0).getId();

        List<Skill> skills = skillRepository.findAll();
        candidSkillId = skills.get(0).getId();
        droneSkillId = skills.size() > 1 ? skills.get(1).getId() : skills.get(0).getId();

        List<Equipment> equipments = equipmentRepository.findAll();
        cameraEquipId = equipments.get(0).getId();

        // 2. Register & onboard Studio
        String studioEmail = "studio.aimatch." + System.currentTimeMillis() + "@studiolynk.local";
        studioToken = registerAndGetToken(studioEmail, UserRole.STUDIO);

        StudioOnboardingRequestDto studioOnboard = new StudioOnboardingRequestDto();
        studioOnboard.setStudioName("Candid Glow Visuals");
        studioOnboard.setOwnerName("Senthil Kumaran");
        studioOnboard.setPhone("+91 98400 99887");
        studioOnboard.setAddress("T. Nagar, Chennai, Tamil Nadu");
        studioOnboard.setLatitude(new BigDecimal("13.0418"));
        studioOnboard.setLongitude(new BigDecimal("80.2341"));
        studioOnboard.setYearsOfOperation(8);

        mockMvc.perform(post("/api/onboarding/studio")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioOnboard)))
                .andExpect(status().isOk());

        // 3. Create Work Requirement (Wedding, Chennai, 15k budget, Candid skill)
        LocalDate shootDate = LocalDate.now().plusDays(3);
        WorkRequirementRequestDto reqDto = new WorkRequirementRequestDto();
        reqDto.setEventName("Ananya & Siddharth Brahmin Wedding");
        reqDto.setEventType("Wedding");
        reqDto.setEventDate(shootDate);
        reqDto.setStartTime(LocalTime.of(8, 0));
        reqDto.setEndTime(LocalTime.of(16, 0));
        reqDto.setLocation("Mayor Ramanathan Chettiar Hall, Chennai");
        reqDto.setLatitude(new BigDecimal("13.0180"));
        reqDto.setLongitude(new BigDecimal("80.2740"));
        reqDto.setDayType(DayType.FULL_DAY);
        reqDto.setBudget(new BigDecimal("15000.00"));
        reqDto.setDescription("Lead candid photographer needed for traditional temple wedding.");
        reqDto.setStatus(RequirementStatus.OPEN);
        reqDto.setRequiredSkillIds(List.of(candidSkillId));
        reqDto.setRequiredServiceIds(List.of(weddingServiceId));
        reqDto.setRequiredEquipmentIds(List.of(cameraEquipId));

        MvcResult reqRes = mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode reqNode = objectMapper.readTree(reqRes.getResponse().getContentAsString());
        requirementId = reqNode.path("data").path("id").asLong();
        requirement = requirementRepository.findById(requirementId).orElseThrow();

        // 4. Create Freelancer A (Chennai, 7 yrs exp, budget 12k, Candid skill, Wedding service)
        String emailA = "freelancer.ai.a." + System.currentTimeMillis() + "@studiolynk.local";
        String tokenA = registerAndGetToken(emailA, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto dtoA = new FreelancerOnboardingRequestDto();
        dtoA.setFullName("Karthik Raja");
        dtoA.setPhone("+91 98888 12345");
        dtoA.setAddress("Mylapore, Chennai, Tamil Nadu");
        dtoA.setLatitude(new BigDecimal("13.0333"));
        dtoA.setLongitude(new BigDecimal("80.2667"));
        dtoA.setExperienceYears(7);
        dtoA.setBio("Award-winning candid wedding specialist with Sony A7IV system.");
        dtoA.setFullDayRate(new BigDecimal("12000.00"));
        dtoA.setHalfDayRate(new BigDecimal("7000.00"));
        dtoA.setProfilePhotoUrl("https://assets.studiolynk.local/profiles/karthik.jpg");
        dtoA.setSkillIds(List.of(candidSkillId));
        dtoA.setServiceIds(List.of(weddingServiceId));
        dtoA.setEquipmentIds(List.of(cameraEquipId));

        MvcResult resA = mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoA)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootA = objectMapper.readTree(resA.getResponse().getContentAsString());
        freelancerAId = rootA.path("data").path("id").asLong();
        freelancerA = freelancerRepository.findById(freelancerAId).orElseThrow();

        // Add Portfolio with category "Traditional Wedding Highlights" to Freelancer A (ML-013, POR-009)
        Portfolio portfolioA = portfolioRepository.findByFreelancerId(freelancerAId).orElseGet(() -> {
            Portfolio p = new Portfolio(freelancerA);
            return portfolioRepository.save(p);
        });
        PortfolioCategory weddingCat = new PortfolioCategory(portfolioA, "Traditional Wedding Highlights", 1);
        categoryRepository.save(weddingCat);

        // 5. Create Freelancer B (Coimbatore, 2 yrs exp, budget 22k, Drone skill, NO wedding portfolio)
        String emailB = "freelancer.ai.b." + System.currentTimeMillis() + "@studiolynk.local";
        String tokenB = registerAndGetToken(emailB, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto dtoB = new FreelancerOnboardingRequestDto();
        dtoB.setFullName("Meera Sunder");
        dtoB.setPhone("+91 97777 54321");
        dtoB.setAddress("RS Puram, Coimbatore, Tamil Nadu");
        dtoB.setLatitude(new BigDecimal("11.0168"));
        dtoB.setLongitude(new BigDecimal("76.9558"));
        dtoB.setExperienceYears(2);
        dtoB.setBio("Drone operator and commercial fashion shooter.");
        dtoB.setFullDayRate(new BigDecimal("22000.00"));
        dtoB.setHalfDayRate(new BigDecimal("14000.00"));
        dtoB.setProfilePhotoUrl("https://assets.studiolynk.local/profiles/meera.jpg");
        dtoB.setSkillIds(List.of(droneSkillId));
        dtoB.setServiceIds(List.of());
        dtoB.setEquipmentIds(List.of());

        MvcResult resB = mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoB)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootB = objectMapper.readTree(resB.getResponse().getContentAsString());
        freelancerBId = rootB.path("data").path("id").asLong();
        freelancerB = freelancerRepository.findById(freelancerBId).orElseThrow();

        // Pre-configure Freelancer A as AVAILABLE on shootDate for ML-012 matching eligibility
        FreelancerAvailability defaultAvailA = new FreelancerAvailability(freelancerA, shootDate, AvailabilityStatus.AVAILABLE);
        availabilityRepository.save(defaultAvailA);
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
    @DisplayName("Feature Extraction: 6 features normalized in [0, 1] per ML specification")
    void testExtractMatchFeatures_NormalizedRange() {
        MatchFeaturesDto featuresA = aiMatchingService.extractFeatures(requirement, freelancerA);

        assertThat(featuresA).isNotNull();
        // 1. Skill Match: Candid matches Candid -> 1.0
        assertThat(featuresA.getSkillMatch()).isEqualTo(1.0);

        // 2. Portfolio Relevance: category "Traditional Wedding Highlights" matches "Wedding" eventType -> >= 0.90
        assertThat(featuresA.getPortfolioRelevance()).isGreaterThanOrEqualTo(0.90);

        // 3. Experience: 7 years / 10.0 = 0.70
        assertThat(featuresA.getExperience()).isEqualTo(0.70);

        // 4. Budget: 12,000 <= 15,000 requirement budget -> 0.96
        assertThat(featuresA.getBudgetCompatibility()).isGreaterThanOrEqualTo(0.95);

        // 5. Distance: Mylapore to MRC Nagar is ~2-3 km -> very high compatibility
        assertThat(featuresA.getLocationDistance()).isGreaterThan(0.90);

        // Verify all values are within [0.0, 1.0]
        assertThat(featuresA.getSkillMatch()).isBetween(0.0, 1.0);
        assertThat(featuresA.getPortfolioRelevance()).isBetween(0.0, 1.0);
        assertThat(featuresA.getExperience()).isBetween(0.0, 1.0);
        assertThat(featuresA.getBudgetCompatibility()).isBetween(0.0, 1.0);
        assertThat(featuresA.getLocationDistance()).isBetween(0.0, 1.0);
        assertThat(featuresA.getAvailabilityTimeCompatibility()).isBetween(0.0, 1.0);
    }

    @Test
    @DisplayName("ML-012: Hard availability filter strictly precedes ML ranking (Busy candidates excluded)")
    void testHardAvailabilityFilter_ExcludesBusyCandidate() {
        LocalDate shootDate = requirement.getEventDate();

        // Freelancer A is already AVAILABLE from setUp(). Set Freelancer B to BUSY on shootDate.
        FreelancerAvailability availB = new FreelancerAvailability(freelancerB, shootDate, AvailabilityStatus.BUSY);
        availabilityRepository.save(availB);

        // Run ranking via requirement matching service
        List<FreelancerCardDto> ranked =
                aiMatchingService.getMatchingFreelancersForRequirement(requirementId);

        // Freelancer B must be strictly excluded by ML-012 pre-filter
        assertThat(ranked).isNotEmpty();
        assertThat(ranked).anyMatch(c -> c.getId().equals(freelancerAId));
        assertThat(ranked).noneMatch(c -> c.getId().equals(freelancerBId));
    }

    @Test
    @DisplayName("DIS-002, DIS-006: Matching Freelancers API returns candidates sorted descending by aiMatchScore")
    void testMatchingFreelancersEndpoint_ReturnsSortedDescending() throws Exception {
        mockMvc.perform(get("/api/requirements/" + requirementId + "/matching-freelancers")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].aiMatchScore").isNumber());

        // Directly verify ordering from AiMatchingService
        List<FreelancerCardDto> ranked =
                aiMatchingService.getMatchingFreelancersForRequirement(requirementId);

        assertThat(ranked).isNotEmpty();
        for (int i = 0; i < ranked.size() - 1; i++) {
            Double score1 = ranked.get(i).getAiMatchScore();
            Double score2 = ranked.get(i + 1).getAiMatchScore();
            assertThat(score1).isNotNull();
            assertThat(score2).isNotNull();
            assertThat(score1).isGreaterThanOrEqualTo(score2);
        }
    }

    @Test
    @DisplayName("ML-013, POR-009: Portfolio relevance uses structured category text matching, not computer vision")
    void testPortfolioRelevance_StructuredMatching() {
        MatchFeaturesDto featuresA = aiMatchingService.extractFeatures(requirement, freelancerA);

        // Freelancer A has "Traditional Wedding Highlights" category matching "Wedding" eventType
        assertThat(featuresA.getPortfolioRelevance()).isGreaterThanOrEqualTo(0.90);

        MatchFeaturesDto featuresB = aiMatchingService.extractFeatures(requirement, freelancerB);

        // Freelancer B has no portfolio
        assertThat(featuresB.getPortfolioRelevance()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("ML-005, RAT-004: Ratings are strictly EXCLUDED from ML feature extraction")
    void testRatingsExcludedFromMlFeatures() {
        // Extract features before rating
        MatchFeaturesDto beforeRating = aiMatchingService.extractFeatures(requirement, freelancerA);

        // Add 5-star rating to Freelancer A
        Rating rating = new Rating();
        rating.setRequirement(requirement);
        rating.setFromUser(requirement.getStudio().getUser());
        rating.setToUser(freelancerA.getUser());
        rating.setTargetType(RatingTargetType.FREELANCER);
        rating.setScore(5);
        rating.setReviewText("Superb wedding photography!");
        ratingRepository.save(rating);

        // Extract features after rating
        MatchFeaturesDto afterRating = aiMatchingService.extractFeatures(requirement, freelancerA);

        // Features must remain completely identical
        assertThat(beforeRating.getSkillMatch()).isEqualTo(afterRating.getSkillMatch());
        assertThat(beforeRating.getPortfolioRelevance()).isEqualTo(afterRating.getPortfolioRelevance());
        assertThat(beforeRating.getExperience()).isEqualTo(afterRating.getExperience());
        assertThat(beforeRating.getBudgetCompatibility()).isEqualTo(afterRating.getBudgetCompatibility());
        assertThat(beforeRating.getLocationDistance()).isEqualTo(afterRating.getLocationDistance());
        assertThat(beforeRating.getAvailabilityTimeCompatibility()).isEqualTo(afterRating.getAvailabilityTimeCompatibility());
    }

    @Test
    @DisplayName("Fallback Resilience: MlServiceClient fallback produces deterministic score [0, 100]")
    void testFallbackResilience_FallbackScoring() {
        MatchFeaturesDto perfectFeatures = new MatchFeaturesDto(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);

        double score = mlServiceClient.computeFallbackScore(perfectFeatures);
        assertThat(score).isBetween(95.0, 100.0);

        MatchFeaturesDto lowFeatures = new MatchFeaturesDto(0.0, 0.0, 0.1, 0.2, 0.1, 0.5);

        double lowScore = mlServiceClient.computeFallbackScore(lowFeatures);
        assertThat(lowScore).isLessThan(score);
        assertThat(lowScore).isGreaterThanOrEqualTo(0.0);
    }
}
