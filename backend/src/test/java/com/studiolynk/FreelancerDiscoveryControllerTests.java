package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.FreelancerSearchFilterDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.UpdateAvailabilityItemDto;
import com.studiolynk.model.dto.UpdateAvailabilityRequestDto;
import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.model.enums.AvailabilityStatus;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.EquipmentRepository;
import com.studiolynk.repository.FreelancerRepository;
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
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FreelancerDiscoveryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    private String studioToken;
    private Long freelancerAId;
    private Long freelancerBId;
    private String freelancerAToken;
    private String freelancerBToken;

    private Long weddingServiceId;
    private Long portraitServiceId;
    private Long candidSkillId;
    private Long droneSkillId;
    private Long cameraEquipId;

    @BeforeEach
    void setUp() throws Exception {
        // Register Studio user
        String studioEmail = "studio.discovery." + System.currentTimeMillis() + "@studiolynk.local";
        studioToken = registerAndGetToken(studioEmail, UserRole.STUDIO);

        // Preload catalogue references
        List<ServiceEntity> services = serviceRepository.findAll();
        weddingServiceId = services.get(0).getId();
        portraitServiceId = services.size() > 1 ? services.get(1).getId() : services.get(0).getId();

        List<Skill> skills = skillRepository.findAll();
        candidSkillId = skills.get(0).getId();
        droneSkillId = skills.size() > 1 ? skills.get(1).getId() : skills.get(0).getId();

        List<Equipment> equipments = equipmentRepository.findAll();
        cameraEquipId = equipments.get(0).getId();

        // 1. Create Freelancer A: Chennai, 5 years exp, full day 10,000, half day 6,000, Candid + Wedding + Camera
        String emailA = "freelancer.a." + System.currentTimeMillis() + "@studiolynk.local";
        freelancerAToken = registerAndGetToken(emailA, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto reqA = new FreelancerOnboardingRequestDto();
        reqA.setFullName("Karthik Raja");
        reqA.setPhone("+91 98888 11111");
        reqA.setAddress("T. Nagar, Chennai, Tamil Nadu");
        reqA.setLatitude(new BigDecimal("13.0418"));
        reqA.setLongitude(new BigDecimal("80.2341"));
        reqA.setExperienceYears(5);
        reqA.setBio("Award-winning candid wedding specialist with high-end Sony gear.");
        reqA.setFullDayRate(new BigDecimal("10000.00"));
        reqA.setHalfDayRate(new BigDecimal("6000.00"));
        reqA.setProfilePhotoUrl("https://assets.studiolynk.local/profiles/karthik.jpg");
        reqA.setServiceIds(List.of(weddingServiceId));
        reqA.setSkillIds(List.of(candidSkillId));
        reqA.setEquipmentIds(List.of(cameraEquipId));

        MvcResult resA = mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + freelancerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rootA = objectMapper.readTree(resA.getResponse().getContentAsString());
        freelancerAId = rootA.path("data").path("id").asLong();

        // 2. Create Freelancer B: Coimbatore, 2 years exp, full day 18,000, half day 10,000, Drone + Portrait
        String emailB = "freelancer.b." + System.currentTimeMillis() + "@studiolynk.local";
        freelancerBToken = registerAndGetToken(emailB, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto reqB = new FreelancerOnboardingRequestDto();
        reqB.setFullName("Meera Sunder");
        reqB.setPhone("+91 97777 22222");
        reqB.setAddress("RS Puram, Coimbatore, Tamil Nadu");
        reqB.setLatitude(new BigDecimal("11.0168"));
        reqB.setLongitude(new BigDecimal("76.9558"));
        reqB.setExperienceYears(2);
        reqB.setBio("Licensed commercial drone pilot and portrait photographer.");
        reqB.setFullDayRate(new BigDecimal("18000.00"));
        reqB.setHalfDayRate(new BigDecimal("10000.00"));
        reqB.setProfilePhotoUrl("https://assets.studiolynk.local/profiles/meera.jpg");
        reqB.setServiceIds(List.of(portraitServiceId));
        reqB.setSkillIds(List.of(droneSkillId));

        MvcResult resB = mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + freelancerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rootB = objectMapper.readTree(resB.getResponse().getContentAsString());
        freelancerBId = rootB.path("data").path("id").asLong();
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
    @DisplayName("DIS-001, DIS-004, DIS-005: Studio basic discovery search returns freelancer cards")
    void testBasicDiscoverySearch() throws Exception {
        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalResults", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.freelancers").isArray())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].fullName").value("Karthik Raja"))
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].experienceYears").value(5))
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].fullDayRate").value(10000.00))
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].averageRating").exists())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].reviewCount").exists());
    }

    @Test
    @DisplayName("DIS-003: Multi-parameter search by keyword, service, skill, and budget")
    void testMultiParameterFilters() throws Exception {
        // Filter by keyword "Sony" or "candid"
        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("keyword", "candid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].fullName").value("Karthik Raja"));

        // Filter by budget: max budget 12000 for FULL_DAY should match A (10000) but not B (18000)
        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("maxBudget", "12000")
                        .param("dayType", "FULL_DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")]").exists())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerBId + ")]").doesNotExist());

        // Filter by minExperience: 4 years should match A (5) but not B (2)
        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("minExperience", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")]").exists())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerBId + ")]").doesNotExist());
    }

    @Test
    @DisplayName("DIS-003: Distance calculation using Haversine formula and distance sorting")
    void testHaversineDistanceAndSorting() throws Exception {
        // Search centered near Chennai (13.0827, 80.2707)
        // Freelancer A (T. Nagar ~6 km) is much closer than Freelancer B (Coimbatore ~420 km)
        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("latitude", "13.0827")
                        .param("longitude", "80.2707")
                        .param("sortBy", "distance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancers[0].id").value(freelancerAId))
                .andExpect(jsonPath("$.data.freelancers[0].distanceKm").isNumber());

        // Max distance filter: within 50 km should return A and exclude B
        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("latitude", "13.0827")
                        .param("longitude", "80.2707")
                        .param("maxDistanceKm", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")]").exists())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerBId + ")]").doesNotExist());
    }

    @Test
    @DisplayName("AVL-004, AVL-005, AVL-006, DIS-003: Rolling 10-day availability filter behavior")
    void testAvailabilityFilteringInDiscovery() throws Exception {
        LocalDate dateTomorrow = LocalDate.now().plusDays(1);
        LocalDate dateFarFuture = LocalDate.now().plusDays(25);

        // 1. Mark Freelancer A as AVAILABLE tomorrow 09:00 - 18:00
        UpdateAvailabilityRequestDto availA = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(dateTomorrow, AvailabilityStatus.AVAILABLE, LocalTime.of(9, 0), LocalTime.of(18, 0))
        ));

        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availA)))
                .andExpect(status().isOk());

        // 2. Mark Freelancer B as BUSY tomorrow
        UpdateAvailabilityRequestDto availB = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(dateTomorrow, AvailabilityStatus.BUSY, null, null)
        ));

        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availB)))
                .andExpect(status().isOk());

        // 3. Search for dateTomorrow:
        // Inside 10-day window: Freelancer A should match (AVAILABLE), Freelancer B must be EXCLUDED (BUSY)
        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("date", dateTomorrow.toString())
                        .param("startTime", "10:00")
                        .param("endTime", "16:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].availabilityStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerBId + ")]").doesNotExist());

        // 4. Search for dateFarFuture (beyond 10-day window):
        // Neither candidate should be excluded. Status must be NOT_SET with explanatory notice (AVL-006).
        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("date", dateFarFuture.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].availabilityStatus").value("NOT_SET"))
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerAId + ")].withinWindow").value(false))
                .andExpect(jsonPath("$.data.freelancers[?(@.id == " + freelancerBId + ")].withinWindow").value(false));
    }

    @Test
    @DisplayName("DIS-004, DIS-005: Individual Freelancer Card endpoint with live availability details")
    void testGetFreelancerCardById() throws Exception {
        LocalDate dateTomorrow = LocalDate.now().plusDays(2);
        UpdateAvailabilityRequestDto availA = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(dateTomorrow, AvailabilityStatus.AVAILABLE, LocalTime.of(8, 0), LocalTime.of(17, 0))
        ));

        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availA)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/freelancers/" + freelancerAId + "/card")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("date", dateTomorrow.toString())
                        .param("startTime", "09:00")
                        .param("endTime", "15:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(freelancerAId))
                .andExpect(jsonPath("$.data.fullName").value("Karthik Raja"))
                .andExpect(jsonPath("$.data.availabilityStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.availableHours").value("08:00 - 17:00"))
                .andExpect(jsonPath("$.data.skills").isArray())
                .andExpect(jsonPath("$.data.services").isArray())
                .andExpect(jsonPath("$.data.equipment").isArray());
    }

    @Test
    @DisplayName("DIS-001: Incomplete onboarding freelancers are strictly excluded from search results")
    void testIncompleteOnboardingExcluded() throws Exception {
        // Register freelancer who hasn't completed onboarding
        String incompleteEmail = "incomplete." + System.currentTimeMillis() + "@studiolynk.local";
        registerAndGetToken(incompleteEmail, UserRole.FREELANCER);

        mockMvc.perform(get("/api/freelancers/search")
                        .header("Authorization", "Bearer " + studioToken)
                        .param("keyword", "incomplete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalResults").value(0))
                .andExpect(jsonPath("$.data.freelancers", hasSize(0)));
    }
}
