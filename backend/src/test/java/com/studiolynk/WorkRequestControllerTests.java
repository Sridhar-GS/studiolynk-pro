package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.AcceptRequestDto;
import com.studiolynk.model.dto.CancelRequestDto;
import com.studiolynk.model.dto.CreateWorkRequestDto;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.WorkRequirementRequestDto;
import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.RequestStatus;
import com.studiolynk.model.enums.RequirementStatus;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkRequestControllerTests {

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
    private com.studiolynk.repository.UserRepository userRepository;

    private String studioToken;
    private String studioEmail;

    private String freelancerToken1;
    private Long freelancerId1;

    private String freelancerToken2;
    private Long freelancerId2;

    private Long requirementId;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Studio registration & onboarding
        studioEmail = "studio.req." + ts + "@studiolynk.local";
        studioToken = registerAndGetToken(studioEmail, UserRole.STUDIO);

        StudioOnboardingRequestDto studioOnboard = new StudioOnboardingRequestDto();
        studioOnboard.setStudioName("Prism Photography Studios");
        studioOnboard.setOwnerName("Ravi Shankar");
        studioOnboard.setPhone("+91 98401 11223");
        studioOnboard.setAddress("Alwarpet, Chennai, Tamil Nadu");
        studioOnboard.setLatitude(new BigDecimal("13.0334"));
        studioOnboard.setLongitude(new BigDecimal("80.2520"));
        studioOnboard.setYearsOfOperation(7);

        mockMvc.perform(post("/api/onboarding/studio")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studioOnboard)))
                .andExpect(status().isOk());

        // 2. Freelancer 1 registration & onboarding
        String freelancerEmail1 = "freelancer1." + ts + "@studiolynk.local";
        freelancerToken1 = registerAndGetToken(freelancerEmail1, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto fl1 = new FreelancerOnboardingRequestDto();
        fl1.setFullName("Karthik Raman");
        fl1.setPhone("+91 98402 33445");
        fl1.setAddress("T. Nagar, Chennai, Tamil Nadu");
        fl1.setLatitude(new BigDecimal("13.0418"));
        fl1.setLongitude(new BigDecimal("80.2341"));
        fl1.setExperienceYears(4);
        fl1.setFullDayRate(new BigDecimal("12000.00"));
        fl1.setHalfDayRate(new BigDecimal("7000.00"));

        mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fl1)))
                .andExpect(status().isOk());

        Long userId1 = userRepository.findByEmail(freelancerEmail1).orElseThrow().getId();
        freelancerId1 = freelancerRepository.findByUserId(userId1).orElseThrow().getId();

        // 3. Freelancer 2 registration & onboarding
        String freelancerEmail2 = "freelancer2." + ts + "@studiolynk.local";
        freelancerToken2 = registerAndGetToken(freelancerEmail2, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto fl2 = new FreelancerOnboardingRequestDto();
        fl2.setFullName("Deepa Sundaram");
        fl2.setPhone("+91 98403 66778");
        fl2.setAddress("Velachery, Chennai, Tamil Nadu");
        fl2.setLatitude(new BigDecimal("12.9815"));
        fl2.setLongitude(new BigDecimal("80.2180"));
        fl2.setExperienceYears(6);
        fl2.setFullDayRate(new BigDecimal("15000.00"));
        fl2.setHalfDayRate(new BigDecimal("9000.00"));

        mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fl2)))
                .andExpect(status().isOk());

        Long userId2 = userRepository.findByEmail(freelancerEmail2).orElseThrow().getId();
        freelancerId2 = freelancerRepository.findByUserId(userId2).orElseThrow().getId();

        // 4. Create Studio Work Requirement with confidential client contact details (REQ-002, REQ-006)
        WorkRequirementRequestDto req = new WorkRequirementRequestDto();
        req.setEventName("Traditional Wedding Ceremony — Ananya & Karthik");
        req.setEventType("Wedding");
        req.setEventDate(LocalDate.now().plusDays(5));
        req.setStartTime(LocalTime.of(8, 0));
        req.setEndTime(LocalTime.of(16, 0));
        req.setLocation("Leela Palace, MRC Nagar, Chennai");
        req.setDayType(DayType.FULL_DAY);
        req.setBudget(new BigDecimal("22000.00"));
        req.setDescription("Traditional South Indian wedding coverage. Looking for candid photographer.");
        req.setStatus(RequirementStatus.OPEN);
        req.setEventContactName("Mr. V. Sundaram (Bride's Father)");
        req.setEventContactPhone("+91 98840 99887");

        MvcResult reqResult = mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(reqResult.getResponse().getContentAsString());
        requirementId = jsonNode.path("data").path("id").asLong();
    }

    @Test
    @DisplayName("REQ-001 & WRK-006: Studio can dispatch a work request to a freelancer")
    void testSendWorkRequestSuccess() throws Exception {
        CreateWorkRequestDto dto = new CreateWorkRequestDto(
                requirementId,
                freelancerId1,
                new BigDecimal("20000.00"),
                "We loved your portfolio and would like you to lead wedding photography for this event."
        );

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.freelancerId").value(freelancerId1))
                .andExpect(jsonPath("$.data.freelancerName").value("Karthik Raman"))
                .andExpect(jsonPath("$.data.agreedPrice").value(20000.00))
                .andExpect(jsonPath("$.data.eventName").value("Traditional Wedding Ceremony — Ananya & Karthik"))
                // Studio owner sees client contact
                .andExpect(jsonPath("$.data.privateDetailsRevealed").value(true))
                .andExpect(jsonPath("$.data.eventContactName").value("Mr. V. Sundaram (Bride's Father)"));
    }

    @Test
    @DisplayName("REQ-001: Cannot send duplicate active request to the same freelancer for the same requirement")
    void testSendDuplicateWorkRequestFails() throws Exception {
        CreateWorkRequestDto dto = new CreateWorkRequestDto(requirementId, freelancerId1, new BigDecimal("18000.00"), "First invite");

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Duplicate attempt
        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("REQ-002: Freelancer initially sees event details but NOT private client contact details")
    void testFreelancerSeesRequestWithMaskedPrivateContact() throws Exception {
        // Studio sends request
        CreateWorkRequestDto dto = new CreateWorkRequestDto(requirementId, freelancerId1, new BigDecimal("20000.00"), "Please check this shoot");
        MvcResult sendRes = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        long requestId = objectMapper.readTree(sendRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Freelancer 1 queries incoming requests list
        mockMvc.perform(get("/api/requests/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        // Freelancer 1 inspects single request details
        mockMvc.perform(get("/api/requests/" + requestId)
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.eventName").value("Traditional Wedding Ceremony — Ananya & Karthik"))
                .andExpect(jsonPath("$.data.location").value("Leela Palace, MRC Nagar, Chennai"))
                // REQ-002 Privacy Guarantee: Private contact must be masked (null)
                .andExpect(jsonPath("$.data.hasPrivateContactDetails").value(true))
                .andExpect(jsonPath("$.data.privateDetailsRevealed").value(false))
                .andExpect(jsonPath("$.data.eventContactName").isEmpty())
                .andExpect(jsonPath("$.data.eventContactPhone").isEmpty());
    }

    @Test
    @DisplayName("REQ-004: Freelancer can accept or reject a request")
    void testFreelancerAcceptAndRejectRequest() throws Exception {
        // Send request to Freelancer 1
        CreateWorkRequestDto dto1 = new CreateWorkRequestDto(requirementId, freelancerId1, new BigDecimal("20000.00"), "Invite 1");
        MvcResult res1 = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated())
                .andReturn();
        long reqId1 = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Send request to Freelancer 2 (WRK-006: Multiple candidates)
        CreateWorkRequestDto dto2 = new CreateWorkRequestDto(requirementId, freelancerId2, new BigDecimal("21000.00"), "Invite 2");
        MvcResult res2 = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated())
                .andReturn();
        long reqId2 = objectMapper.readTree(res2.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Freelancer 1 accepts with agreed price (REQ-004, REQ-008)
        AcceptRequestDto acceptDto = new AcceptRequestDto(new BigDecimal("21500.00"));
        mockMvc.perform(patch("/api/requests/" + reqId1 + "/accept")
                        .header("Authorization", "Bearer " + freelancerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.agreedPrice").value(21500.00))
                // REQ-006: Still masked because Studio has not confirmed yet!
                .andExpect(jsonPath("$.data.privateDetailsRevealed").value(false))
                .andExpect(jsonPath("$.data.eventContactName").isEmpty());

        // Freelancer 2 declines/rejects (REQ-004)
        mockMvc.perform(patch("/api/requests/" + reqId2 + "/reject")
                        .header("Authorization", "Bearer " + freelancerToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "Already booked for personal commitment."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.cancellationReason").value("Already booked for personal commitment."));
    }

    @Test
    @DisplayName("REQ-005 & REQ-006 & WRK-008: Studio confirmation unlocks private client details and auto-rejects other candidates")
    void testStudioConfirmFreelancerRevealsPrivateContactAndClosesOthers() throws Exception {
        // Send request to Freelancer 1
        CreateWorkRequestDto dto1 = new CreateWorkRequestDto(requirementId, freelancerId1, new BigDecimal("20000.00"), "Offer 1");
        MvcResult res1 = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated())
                .andReturn();
        long reqId1 = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Send request to Freelancer 2
        CreateWorkRequestDto dto2 = new CreateWorkRequestDto(requirementId, freelancerId2, new BigDecimal("22000.00"), "Offer 2");
        MvcResult res2 = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated())
                .andReturn();
        long reqId2 = objectMapper.readTree(res2.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Freelancer 1 accepts
        mockMvc.perform(patch("/api/requests/" + reqId1 + "/accept")
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk());

        // Studio confirms Freelancer 1 (REQ-005, WRK-007)
        mockMvc.perform(patch("/api/requests/" + reqId1 + "/confirm")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.privateDetailsRevealed").value(true));

        // WRK-008: Candidate 2 should be automatically rejected/closed!
        mockMvc.perform(get("/api/requests/" + reqId2)
                        .header("Authorization", "Bearer " + freelancerToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.cancellationReason").value("Studio confirmed another creator for this assignment."));

        // REQ-006: Confirmed Freelancer 1 can now see private client contact info!
        mockMvc.perform(get("/api/requests/" + reqId1)
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.privateDetailsRevealed").value(true))
                .andExpect(jsonPath("$.data.eventContactName").value("Mr. V. Sundaram (Bride's Father)"))
                .andExpect(jsonPath("$.data.eventContactPhone").value("+91 98840 99887"));
    }

    @Test
    @DisplayName("REQ-009: Freelancer double booking is prevented for overlapping confirmed shoot times")
    void testDoubleBookingPrevention() throws Exception {
        // Confirm Freelancer 1 for Requirement 1 (Shoot date: today + 5 days, 08:00 - 16:00)
        CreateWorkRequestDto dto1 = new CreateWorkRequestDto(requirementId, freelancerId1, new BigDecimal("20000.00"), "Offer 1");
        MvcResult res1 = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated())
                .andReturn();
        long reqId1 = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/requests/" + reqId1 + "/accept")
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/requests/" + reqId1 + "/confirm")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk());

        // Studio creates Requirement 2 on the SAME DATE with OVERLAPPING TIME (10:00 - 18:00)
        WorkRequirementRequestDto req2 = new WorkRequirementRequestDto();
        req2.setEventName("Corporate Summit Video Coverage");
        req2.setEventType("Corporate Event");
        req2.setEventDate(LocalDate.now().plusDays(5)); // Same date!
        req2.setStartTime(LocalTime.of(10, 0)); // Overlaps with 08:00 - 16:00
        req2.setEndTime(LocalTime.of(18, 0));
        req2.setLocation("ITC Grand Chola, Chennai");
        req2.setDayType(DayType.FULL_DAY);
        req2.setBudget(new BigDecimal("18000.00"));
        req2.setStatus(RequirementStatus.OPEN);

        MvcResult reqResult2 = mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andReturn();

        long requirementId2 = objectMapper.readTree(reqResult2.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Attempting to send request to Freelancer 1 for overlapping shoot must fail with double-booking error!
        CreateWorkRequestDto dtoOverlap = new CreateWorkRequestDto(requirementId2, freelancerId1, new BigDecimal("18000.00"), "Overlap invite");
        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoOverlap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Freelancer already has a confirmed shoot")));
    }

    @Test
    @DisplayName("REQ-007: Either party can cancel confirmed work with a reason")
    void testCancelConfirmedWorkWithReason() throws Exception {
        CreateWorkRequestDto dto = new CreateWorkRequestDto(requirementId, freelancerId1, new BigDecimal("20000.00"), "Offer");
        MvcResult res = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
        long reqId = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(patch("/api/requests/" + reqId + "/accept")
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/requests/" + reqId + "/confirm")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk());

        // Cancellation requires reason (REQ-007)
        CancelRequestDto cancelDto = new CancelRequestDto("Client shifted event date due to family emergency.");

        mockMvc.perform(patch("/api/requests/" + reqId + "/cancel")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancellationReason").value("Client shifted event date due to family emergency."));
    }

    @Test
    @DisplayName("Security: Freelancer cannot send a work request (403 Forbidden)")
    void testFreelancerCannotSendWorkRequest() throws Exception {
        CreateWorkRequestDto dto = new CreateWorkRequestDto(requirementId, freelancerId2, new BigDecimal("10000.00"), "Invite");

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + freelancerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private String registerAndGetToken(String email, UserRole role) throws Exception {
        RegisterRequestDto reg = new RegisterRequestDto();
        reg.setEmail(email);
        reg.setPassword("StudioLynk@2026#Sec");
        reg.setRole(role);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }
}
