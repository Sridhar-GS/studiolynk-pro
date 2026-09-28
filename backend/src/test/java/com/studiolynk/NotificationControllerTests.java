package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.*;
import com.studiolynk.model.enums.DayType;
import com.studiolynk.model.enums.NotificationType;
import com.studiolynk.model.enums.RequirementStatus;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.EquipmentRepository;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.ServiceRepository;
import com.studiolynk.repository.SkillRepository;
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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationControllerTests {

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

    private String studioToken;
    private String freelancerToken;
    private Long freelancerId;
    private Long requirementId;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Studio registration & onboarding
        String studioEmail = "studio.notif." + ts + "@studiolynk.local";
        studioToken = registerAndGetToken(studioEmail, UserRole.STUDIO);

        StudioOnboardingRequestDto studioOnboard = new StudioOnboardingRequestDto();
        studioOnboard.setStudioName("Aura Visual Studios");
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

        // 2. Freelancer registration & onboarding
        String freelancerEmail = "creator.notif." + ts + "@studiolynk.local";
        freelancerToken = registerAndGetToken(freelancerEmail, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto freelancerOnboard = new FreelancerOnboardingRequestDto();
        freelancerOnboard.setFullName("Manoj Kumar");
        freelancerOnboard.setPhone("+91 98402 77889");
        freelancerOnboard.setAddress("Mylapore, Chennai, Tamil Nadu");
        freelancerOnboard.setLatitude(new BigDecimal("13.0420"));
        freelancerOnboard.setLongitude(new BigDecimal("80.2350"));
        freelancerOnboard.setBio("Experienced cinematic photographer");
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
                .filter(f -> f.getFullName().equals("Manoj Kumar"))
                .findFirst()
                .orElseThrow()
                .getId();

        // 3. Create work requirement
        WorkRequirementRequestDto reqDto = new WorkRequirementRequestDto();
        reqDto.setEventName("Corporate Annual Gala 2026");
        reqDto.setEventType("Corporate");
        reqDto.setEventDate(LocalDate.now().plusDays(2));
        reqDto.setStartTime(LocalTime.of(10, 0));
        reqDto.setEndTime(LocalTime.of(18, 0));
        reqDto.setLocation("Leela Palace, MRC Nagar, Chennai");
        reqDto.setDayType(DayType.FULL_DAY);
        reqDto.setBudget(new BigDecimal("15000"));
        reqDto.setDescription("Executive coverage with highlight reel");
        reqDto.setRequiredSkillIds(List.of(skillId));
        reqDto.setRequiredServiceIds(List.of(serviceId));
        reqDto.setRequiredEquipmentIds(List.of(equipmentIdVal));

        MvcResult reqResult = mockMvc.perform(post("/api/requirements")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode reqJson = objectMapper.readTree(reqResult.getResponse().getContentAsString());
        requirementId = reqJson.get("data").get("id").asLong();
    }

    @Test
    void testWorkRequestGeneratesNotificationForFreelancer() throws Exception {
        // Studio dispatches request
        CreateWorkRequestDto createDto = new CreateWorkRequestDto();
        createDto.setRequirementId(requirementId);
        createDto.setFreelancerId(freelancerId);
        createDto.setOfferedPrice(new BigDecimal("12000"));
        createDto.setMessage("We would love to collaborate with you!");

        mockMvc.perform(post("/api/requests")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        // Freelancer checks notifications (NOT-001, NOT-002)
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("REQUEST_RECEIVED"))
                .andExpect(jsonPath("$.data[0].isRead").value(false))
                .andExpect(jsonPath("$.data[0].title").value("New Shoot Request: Corporate Annual Gala 2026"));

        // Freelancer unread count check (NOT-003)
        mockMvc.perform(get("/api/notifications/unread-count")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void testFreelancerAcceptGeneratesNotificationForStudio() throws Exception {
        Long requestId = sendRequestToFreelancer();

        // Freelancer accepts
        AcceptRequestDto acceptDto = new AcceptRequestDto(new BigDecimal("12500"));
        mockMvc.perform(patch("/api/requests/" + requestId + "/accept")
                .header("Authorization", "Bearer " + freelancerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptDto)))
                .andExpect(status().isOk());

        // Studio checks notifications (NOT-002: REQUEST_ACCEPTED)
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("REQUEST_ACCEPTED"))
                .andExpect(jsonPath("$.data[0].title").value("Request Accepted: Corporate Annual Gala 2026"));
    }

    @Test
    void testFreelancerRejectGeneratesNotificationForStudio() throws Exception {
        Long requestId = sendRequestToFreelancer();

        // Freelancer rejects
        mockMvc.perform(patch("/api/requests/" + requestId + "/reject")
                .header("Authorization", "Bearer " + freelancerToken)
                .param("reason", "Already booked on that date"))
                .andExpect(status().isOk());

        // Studio checks notifications (NOT-002: REQUEST_REJECTED)
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("REQUEST_REJECTED"))
                .andExpect(jsonPath("$.data[0].title").value("Request Declined: Corporate Annual Gala 2026"));
    }

    @Test
    void testStudioConfirmationGeneratesNotificationForFreelancer() throws Exception {
        Long requestId = sendRequestToFreelancer();

        // Accept
        mockMvc.perform(patch("/api/requests/" + requestId + "/accept")
                .header("Authorization", "Bearer " + freelancerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AcceptRequestDto(new BigDecimal("12000")))))
                .andExpect(status().isOk());

        // Studio confirms
        mockMvc.perform(patch("/api/requests/" + requestId + "/confirm")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk());

        // Freelancer checks notifications (NOT-002: STUDIO_CONFIRMATION)
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("STUDIO_CONFIRMATION"))
                .andExpect(jsonPath("$.data[0].title").value("Booking Confirmed: Corporate Annual Gala 2026!"));
    }

    @Test
    void testNewMessageGeneratesNotificationForRecipient() throws Exception {
        sendRequestToFreelancer();

        // Get or create conversation
        MvcResult convResult = mockMvc.perform(get("/api/conversations/requirement/" + requirementId)
                .header("Authorization", "Bearer " + studioToken)
                .param("freelancerId", freelancerId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        long conversationId = objectMapper.readTree(convResult.getResponse().getContentAsString())
                .get("data").get("id").asLong();

        // Studio sends message
        SendMessageRequest msgReq = new SendMessageRequest("Can you confirm your camera equipment kit for the shoot?");
        mockMvc.perform(post("/api/conversations/" + conversationId + "/messages")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msgReq)))
                .andExpect(status().isCreated());

        // Freelancer receives NEW_MESSAGE notification (NOT-002)
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("NEW_MESSAGE"))
                .andExpect(jsonPath("$.data[0].title").value("New Message from Aura Visual Studios"));
    }

    @Test
    void testMarkSingleNotificationAsRead() throws Exception {
        sendRequestToFreelancer();

        // Retrieve notification ID
        MvcResult notifResult = mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode notifications = objectMapper.readTree(notifResult.getResponse().getContentAsString()).get("data");
        assertThat(notifications.size()).isGreaterThan(0);
        long notifId = notifications.get(0).get("id").asLong();

        // Mark as read via POST (NOT-003)
        mockMvc.perform(post("/api/notifications/" + notifId + "/read")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isRead").value(true));

        // Mark as read via PATCH (NOT-003)
        mockMvc.perform(patch("/api/notifications/" + notifId + "/read")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    void testMarkAllNotificationsAsRead() throws Exception {
        // Generate two notifications for freelancer
        sendRequestToFreelancer();

        // Check unread count >= 1
        mockMvc.perform(get("/api/notifications/unread-count")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count", greaterThanOrEqualTo(1)));

        // Mark all as read (NOT-004)
        mockMvc.perform(post("/api/notifications/read-all")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk());

        // Verify unread count is now 0
        mockMvc.perform(get("/api/notifications/unread-count")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count", is(0)));
    }

    @Test
    void testWorkLifecycleNotifications() throws Exception {
        Long requestId = sendRequestToFreelancer();

        // Accept and Confirm
        mockMvc.perform(patch("/api/requests/" + requestId + "/accept")
                .header("Authorization", "Bearer " + freelancerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AcceptRequestDto(new BigDecimal("12000")))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/requests/" + requestId + "/confirm")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk());

        // Studio marks requirement IN_PROGRESS
        mockMvc.perform(patch("/api/requirements/" + requirementId + "/status")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RequirementStatusUpdateDto(RequirementStatus.IN_PROGRESS))))
                .andExpect(status().isOk());

        // Freelancer should have WORK_STARTED notification
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("WORK_STARTED"));

        // Studio marks requirement COMPLETED
        mockMvc.perform(patch("/api/requirements/" + requirementId + "/status")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RequirementStatusUpdateDto(RequirementStatus.COMPLETED))))
                .andExpect(status().isOk());

        // Both parties get rating reminders (NOT-002, RAT-001, RAT-002)
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("RATING_REMINDER"));

        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("RATING_REMINDER"));
    }

    @Test
    void testUserCannotAccessAnotherUsersNotification() throws Exception {
        sendRequestToFreelancer();

        // Get freelancer's notification
        MvcResult notifResult = mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + freelancerToken))
                .andExpect(status().isOk())
                .andReturn();

        long freelancerNotifId = objectMapper.readTree(notifResult.getResponse().getContentAsString())
                .get("data").get(0).get("id").asLong();

        // Studio tries to mark freelancer's notification as read -> should fail with 404
        mockMvc.perform(post("/api/notifications/" + freelancerNotifId + "/read")
                .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isNotFound());
    }

    private Long sendRequestToFreelancer() throws Exception {
        CreateWorkRequestDto createDto = new CreateWorkRequestDto();
        createDto.setRequirementId(requirementId);
        createDto.setFreelancerId(freelancerId);
        createDto.setOfferedPrice(new BigDecimal("12000"));
        createDto.setMessage("Join our corporate shoot coverage");

        MvcResult result = mockMvc.perform(post("/api/requests")
                .header("Authorization", "Bearer " + studioToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asLong();
    }

    private String registerAndGetToken(String email, UserRole role) throws Exception {
        RegisterRequestDto reg = new RegisterRequestDto();
        reg.setEmail(email);
        reg.setPassword("SecretPass123!");
        reg.setRole(role);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("data").get("token").asText();
    }
}
