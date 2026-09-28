package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.AvailabilityCheckRequestDto;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.UpdateAvailabilityItemDto;
import com.studiolynk.model.dto.UpdateAvailabilityRequestDto;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.AvailabilityStatus;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.FreelancerAvailabilityRepository;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FreelancerAvailabilityControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private FreelancerAvailabilityRepository availabilityRepository;

    private String freelancerToken;
    private Long freelancerId;
    private String studioToken;

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

    @BeforeEach
    void setUp() throws Exception {
        String flEmail = "fl.avail." + System.currentTimeMillis() + "@studiolynk.local";
        freelancerToken = registerAndGetToken(flEmail, UserRole.FREELANCER);

        // Complete onboarding for freelancer
        FreelancerOnboardingRequestDto flOnboarding = new FreelancerOnboardingRequestDto();
        flOnboarding.setFullName("Ananya Krishnan");
        flOnboarding.setPhone("9876543210");
        flOnboarding.setAddress("Chennai, Tamil Nadu");
        flOnboarding.setLatitude(new BigDecimal("13.0827"));
        flOnboarding.setLongitude(new BigDecimal("80.2707"));
        flOnboarding.setExperienceYears(6);
        flOnboarding.setBio("Senior Fashion and Commercial Photographer.");
        flOnboarding.setFullDayRate(new BigDecimal("8000.00"));
        flOnboarding.setHalfDayRate(new BigDecimal("4500.00"));
        flOnboarding.setSkillIds(List.of());
        flOnboarding.setServiceIds(List.of());
        flOnboarding.setEquipmentIds(List.of());
        mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flOnboarding)))
                .andExpect(status().isOk());

        User flUser = userRepository.findByEmail(flEmail).orElseThrow();
        Freelancer fl = freelancerRepository.findByUserId(flUser.getId()).orElseThrow();
        freelancerId = fl.getId();

        String studioEmail = "studio.avail." + System.currentTimeMillis() + "@studiolynk.local";
        studioToken = registerAndGetToken(studioEmail, UserRole.STUDIO);
    }

    @Test
    @DisplayName("AVL-001: Get rolling 10-day availability window with default NOT_SET")
    void testGetMyAvailability_returns10DayWindowDefaultNotSet() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.freelancerId").value(freelancerId))
                .andExpect(jsonPath("$.data.totalDays").value(10))
                .andExpect(jsonPath("$.data.notSetDaysCount").value(10))
                .andExpect(jsonPath("$.data.availableDaysCount").value(0))
                .andExpect(jsonPath("$.data.busyDaysCount").value(0))
                .andExpect(jsonPath("$.data.slots").isArray())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode slots = root.path("data").path("slots");
        assertThat(slots.size()).isEqualTo(10);

        LocalDate expectedStart = LocalDate.now();
        for (int i = 0; i < 10; i++) {
            JsonNode slot = slots.get(i);
            assertThat(slot.path("date").asText()).isEqualTo(expectedStart.plusDays(i).toString());
            assertThat(slot.path("status").asText()).isEqualTo("NOT_SET");
            assertThat(slot.path("withinWindow").asBoolean()).isTrue();
            assertThat(slot.path("available").asBoolean()).isFalse();
        }
    }

    @Test
    @DisplayName("AVL-002, AVL-003: Update availability slots with AVAILABLE, BUSY, and valid time intervals")
    void testUpdateMyAvailability_success() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfter = today.plusDays(2);

        UpdateAvailabilityRequestDto updateReq = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(today, AvailabilityStatus.AVAILABLE, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                new UpdateAvailabilityItemDto(tomorrow, AvailabilityStatus.BUSY, null, null),
                new UpdateAvailabilityItemDto(dayAfter, AvailabilityStatus.AVAILABLE, LocalTime.of(10, 0), LocalTime.of(16, 0))
        ));

        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableDaysCount").value(2))
                .andExpect(jsonPath("$.data.busyDaysCount").value(1))
                .andExpect(jsonPath("$.data.notSetDaysCount").value(7))
                .andExpect(jsonPath("$.data.slots[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.slots[0].formattedTime").value("09:00 - 18:00"))
                .andExpect(jsonPath("$.data.slots[0].available").value(true))
                .andExpect(jsonPath("$.data.slots[1].status").value("BUSY"))
                .andExpect(jsonPath("$.data.slots[1].available").value(false))
                .andExpect(jsonPath("$.data.slots[2].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.slots[2].formattedTime").value("10:00 - 16:00"));
    }

    @Test
    @DisplayName("AVL-003: Validation failure when AVAILABLE without valid times or inverted start/end times")
    void testUpdateMyAvailability_validationErrors() throws Exception {
        LocalDate today = LocalDate.now();

        // 1. Missing times when AVAILABLE
        UpdateAvailabilityRequestDto missingTimes = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(today, AvailabilityStatus.AVAILABLE, null, null)
        ));
        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingTimes)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Start time and end time are required")));

        // 2. Inverted times (end time before start time)
        UpdateAvailabilityRequestDto invertedTimes = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(today, AvailabilityStatus.AVAILABLE, LocalTime.of(18, 0), LocalTime.of(9, 0))
        ));
        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invertedTimes)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("must be before end time")));

        // 3. Past date
        UpdateAvailabilityRequestDto pastDate = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(today.minusDays(1), AvailabilityStatus.AVAILABLE, LocalTime.of(9, 0), LocalTime.of(18, 0))
        ));
        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastDate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot update availability for past date")));

        // 4. Beyond 10-day window
        UpdateAvailabilityRequestDto beyondWindow = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(today.plusDays(15), AvailabilityStatus.AVAILABLE, LocalTime.of(9, 0), LocalTime.of(18, 0))
        ));
        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beyondWindow)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("beyond the rolling 10-day window")));
    }

    @Test
    @DisplayName("AVL-004, AVL-005, AVL-006: Studio availability check matching rules and beyond-window handling")
    void testCheckAvailability_matchingRules() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate day3 = today.plusDays(2);
        LocalDate day15 = today.plusDays(15);

        // Setup: today is AVAILABLE 09:00 - 18:00, tomorrow is BUSY, day3 is NOT_SET
        UpdateAvailabilityRequestDto setupReq = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(today, AvailabilityStatus.AVAILABLE, LocalTime.of(9, 0), LocalTime.of(18, 0)),
                new UpdateAvailabilityItemDto(tomorrow, AvailabilityStatus.BUSY, null, null)
        ));
        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setupReq)))
                .andExpect(status().isOk());

        // 1. Check today with matching shoot time (10:00 - 15:00 falls inside 09:00 - 18:00) -> MATCH
        AvailabilityCheckRequestDto matchCheck = new AvailabilityCheckRequestDto(
                freelancerId, today, LocalTime.of(10, 0), LocalTime.of(15, 0)
        );
        mockMvc.perform(post("/api/freelancers/availability/check")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(matchCheck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.match").value(true))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.withinWindow").value(true));

        // 2. Check today with out-of-bounds shoot time (08:00 - 12:00 starts before 09:00) -> NO MATCH
        AvailabilityCheckRequestDto outsideHoursCheck = new AvailabilityCheckRequestDto(
                freelancerId, today, LocalTime.of(8, 0), LocalTime.of(12, 0)
        );
        mockMvc.perform(post("/api/freelancers/availability/check")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outsideHoursCheck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.match").value(false))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.reason").value(org.hamcrest.Matchers.containsString("outside freelancer's available hours")));

        // 3. AVL-005: Check tomorrow (BUSY) -> NO MATCH
        AvailabilityCheckRequestDto busyCheck = new AvailabilityCheckRequestDto(
                freelancerId, tomorrow, LocalTime.of(10, 0), LocalTime.of(15, 0)
        );
        mockMvc.perform(post("/api/freelancers/availability/check")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(busyCheck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.match").value(false))
                .andExpect(jsonPath("$.data.status").value("BUSY"));

        // 4. AVL-005: Check day3 (NOT_SET) -> NO MATCH
        AvailabilityCheckRequestDto notSetCheck = new AvailabilityCheckRequestDto(
                freelancerId, day3, LocalTime.of(10, 0), LocalTime.of(15, 0)
        );
        mockMvc.perform(post("/api/freelancers/availability/check")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notSetCheck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.match").value(false))
                .andExpect(jsonPath("$.data.status").value("NOT_SET"));

        // 5. AVL-006: Check day15 (beyond 10-day window) -> isWithinWindow = false, NOT_SET
        AvailabilityCheckRequestDto beyondCheck = new AvailabilityCheckRequestDto(
                freelancerId, day15, LocalTime.of(10, 0), LocalTime.of(15, 0)
        );
        mockMvc.perform(post("/api/freelancers/availability/check")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beyondCheck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.withinWindow").value(false))
                .andExpect(jsonPath("$.data.match").value(false))
                .andExpect(jsonPath("$.data.status").value("NOT_SET"))
                .andExpect(jsonPath("$.data.reason").value(org.hamcrest.Matchers.containsString("Beyond the rolling 10-day scheduling window")));
    }

    @Test
    @DisplayName("Reset availability back to NOT_SET across entire 10-day window")
    void testResetMyAvailability_success() throws Exception {
        LocalDate today = LocalDate.now();

        // First set a day to AVAILABLE
        UpdateAvailabilityRequestDto setupReq = new UpdateAvailabilityRequestDto(List.of(
                new UpdateAvailabilityItemDto(today, AvailabilityStatus.AVAILABLE, LocalTime.of(9, 0), LocalTime.of(18, 0))
        ));
        mockMvc.perform(put("/api/freelancers/me/availability")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setupReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableDaysCount").value(1));

        // Reset
        mockMvc.perform(post("/api/freelancers/me/availability/reset")
                        .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.availableDaysCount").value(0))
                .andExpect(jsonPath("$.data.busyDaysCount").value(0))
                .andExpect(jsonPath("$.data.notSetDaysCount").value(10));
    }

    @Test
    @DisplayName("Public/Studio access to freelancer availability window by ID")
    void testGetFreelancerAvailability_byId() throws Exception {
        mockMvc.perform(get("/api/freelancers/" + freelancerId + "/availability")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.freelancerId").value(freelancerId))
                .andExpect(jsonPath("$.data.totalDays").value(10));
    }
}
