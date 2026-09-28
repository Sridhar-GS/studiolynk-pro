package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.CreateWorkRequestDto;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.SendMessageRequest;
import com.studiolynk.model.dto.StudioOnboardingRequestDto;
import com.studiolynk.model.dto.WorkRequirementRequestDto;
import com.studiolynk.model.enums.DayType;
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
class ConversationControllerTests {

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
    private String freelancerToken1;
    private Long freelancerId1;

    private String freelancerToken2;
    private Long freelancerId2;

    private Long requirementId;

    @BeforeEach
    void setUp() throws Exception {
        long ts = System.currentTimeMillis();

        // 1. Studio registration & onboarding
        String studioEmail = "studio.msg." + ts + "@studiolynk.local";
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

        // 2. Freelancer 1 registration & onboarding
        String flEmail1 = "creator1.msg." + ts + "@studiolynk.local";
        freelancerToken1 = registerAndGetToken(flEmail1, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto flOnboard1 = new FreelancerOnboardingRequestDto();
        flOnboard1.setFullName("Manoj Kumar");
        flOnboard1.setPhone("+91 97890 12345");
        flOnboard1.setAddress("Mylapore, Chennai, Tamil Nadu");
        flOnboard1.setLatitude(new BigDecimal("13.0368"));
        flOnboard1.setLongitude(new BigDecimal("80.2676"));
        flOnboard1.setExperienceYears(6);
        flOnboard1.setFullDayRate(new BigDecimal("14000.00"));
        flOnboard1.setHalfDayRate(new BigDecimal("8000.00"));
        flOnboard1.setSkillIds(List.of(skillRepository.findAll().get(0).getId()));
        flOnboard1.setServiceIds(List.of(serviceRepository.findAll().get(0).getId()));
        flOnboard1.setEquipmentIds(List.of(equipmentRepository.findAll().get(0).getId()));

        mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flOnboard1)))
                .andExpect(status().isOk());

        freelancerId1 = freelancerRepository.findByUserId(
                userRepositoryFindByEmail(flEmail1).getId()).orElseThrow().getId();

        // 3. Freelancer 2 registration & onboarding (uninvited creator)
        String flEmail2 = "creator2.msg." + ts + "@studiolynk.local";
        freelancerToken2 = registerAndGetToken(flEmail2, UserRole.FREELANCER);

        FreelancerOnboardingRequestDto flOnboard2 = new FreelancerOnboardingRequestDto();
        flOnboard2.setFullName("Pooja Sundaram");
        flOnboard2.setPhone("+91 97890 67890");
        flOnboard2.setAddress("Velachery, Chennai, Tamil Nadu");
        flOnboard2.setLatitude(new BigDecimal("12.9815"));
        flOnboard2.setLongitude(new BigDecimal("80.2180"));
        flOnboard2.setExperienceYears(4);
        flOnboard2.setFullDayRate(new BigDecimal("11000.00"));
        flOnboard2.setHalfDayRate(new BigDecimal("6500.00"));
        flOnboard2.setSkillIds(List.of(skillRepository.findAll().get(0).getId()));
        flOnboard2.setServiceIds(List.of(serviceRepository.findAll().get(0).getId()));
        flOnboard2.setEquipmentIds(List.of(equipmentRepository.findAll().get(0).getId()));

        mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + freelancerToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flOnboard2)))
                .andExpect(status().isOk());

        freelancerId2 = freelancerRepository.findByUserId(
                userRepositoryFindByEmail(flEmail2).getId()).orElseThrow().getId();

        // 4. Create Work Requirement (WRK-001)
        WorkRequirementRequestDto reqDto = new WorkRequirementRequestDto();
        reqDto.setEventName("Corporate Annual Gala 2026");
        reqDto.setEventType("Corporate");
        reqDto.setEventDate(LocalDate.now().plusDays(4));
        reqDto.setStartTime(LocalTime.of(17, 0));
        reqDto.setEndTime(LocalTime.of(22, 0));
        reqDto.setLocation("ITC Grand Chola, Guindy, Chennai");
        reqDto.setDayType(DayType.HALF_DAY);
        reqDto.setBudget(new BigDecimal("15000.00"));
        reqDto.setDescription("Executive awards evening and keynote photography coverage.");
        reqDto.setStatus(com.studiolynk.model.enums.RequirementStatus.OPEN);

        MvcResult reqRes = mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode reqJson = objectMapper.readTree(reqRes.getResponse().getContentAsString());
        requirementId = reqJson.get("data").get("id").asLong();

        // 5. Send Work Request from Studio to Freelancer 1 (REQ-001)
        CreateWorkRequestDto workRequestDto = new CreateWorkRequestDto(
                requirementId, freelancerId1, new BigDecimal("15000.00"), "Inviting you for our annual gala coverage."
        );

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workRequestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("MSG-001, MSG-005: Studio can initiate requirement conversation with invited freelancer")
    void testStudioCanInitiateConversation() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/conversations/requirement/" + requirementId + "?freelancerId=" + freelancerId1)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.requirementId").value(requirementId))
                .andExpect(jsonPath("$.data.eventName").value("Corporate Annual Gala 2026"))
                .andExpect(jsonPath("$.data.freelancerId").value(freelancerId1))
                .andExpect(jsonPath("$.data.freelancerName").value("Manoj Kumar"))
                .andExpect(jsonPath("$.data.studioName").value("Aura Visual Studios"))
                .andReturn();

        JsonNode data = objectMapper.readTree(res.getResponse().getContentAsString()).get("data");
        Long convId = data.get("id").asLong();
        assertThat(convId).isNotNull();

        // Idempotency check: Calling again returns the same conversation ID
        mockMvc.perform(get("/api/conversations/requirement/" + requirementId + "?freelancerId=" + freelancerId1)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(convId));
    }

    @Test
    @DisplayName("REQ-003, MSG-005: Freelancer can open conversation after receiving request")
    void testFreelancerCanOpenConversationWithRequest() throws Exception {
        mockMvc.perform(get("/api/conversations/requirement/" + requirementId)
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.requirementId").value(requirementId))
                .andExpect(jsonPath("$.data.studioName").value("Aura Visual Studios"));
    }

    @Test
    @DisplayName("MSG-001, MSG-005: Freelancer without work request cannot access requirement conversation")
    void testFreelancerWithoutRequestForbidden() throws Exception {
        mockMvc.perform(get("/api/conversations/requirement/" + requirementId)
                        .header("Authorization", "Bearer " + freelancerToken2))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MSG-003, MSG-004, MSG-005: Send text messages, verify timestamps and message history")
    void testSendMessagesAndHistory() throws Exception {
        // 1. Studio initiates conversation
        MvcResult convRes = mockMvc.perform(get("/api/conversations/requirement/" + requirementId + "?freelancerId=" + freelancerId1)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andReturn();
        Long convId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 2. Studio sends message (MSG-003, MSG-005)
        SendMessageRequest msg1 = new SendMessageRequest("Hi Manoj, are you available for the 5 PM start at ITC Grand Chola?");
        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.content").value("Hi Manoj, are you available for the 5 PM start at ITC Grand Chola?"))
                .andExpect(jsonPath("$.data.senderRole").value("STUDIO"))
                .andExpect(jsonPath("$.data.sentAt").isNotEmpty())
                .andExpect(jsonPath("$.data.read").value(false));

        // 3. Freelancer replies with negotiation message (MSG-005)
        SendMessageRequest msg2 = new SendMessageRequest("Yes, 5 PM works! I can bring my dual Sony A7IV bodies and 24-70mm f/2.8 GM lens.");
        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + freelancerToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.senderRole").value("FREELANCER"))
                .andExpect(jsonPath("$.data.content").value("Yes, 5 PM works! I can bring my dual Sony A7IV bodies and 24-70mm f/2.8 GM lens."));

        // 4. Retrieve message history (MSG-004)
        mockMvc.perform(get("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].content").value("Hi Manoj, are you available for the 5 PM start at ITC Grand Chola?"))
                .andExpect(jsonPath("$.data[1].content").value("Yes, 5 PM works! I can bring my dual Sony A7IV bodies and 24-70mm f/2.8 GM lens."));
    }

    @Test
    @DisplayName("MSG-004: Read state tracking and mark as read")
    void testReadStateTracking() throws Exception {
        // 1. Studio opens conversation and sends message
        MvcResult convRes = mockMvc.perform(get("/api/conversations/requirement/" + requirementId + "?freelancerId=" + freelancerId1)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andReturn();
        Long convId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        SendMessageRequest msg = new SendMessageRequest("Please review the proposed shoot schedule.");
        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isCreated());

        // 2. Freelancer checks conversation details before viewing messages — unreadCount should be 1
        mockMvc.perform(get("/api/conversations/" + convId)
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(1));

        // 3. Freelancer marks as read (or retrieves messages)
        mockMvc.perform(patch("/api/conversations/" + convId + "/read")
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk());

        // 4. Unread count should now be 0
        mockMvc.perform(get("/api/conversations/" + convId)
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));
    }

    @Test
    @DisplayName("MSG-001: Non-participant third party is forbidden from accessing conversation")
    void testNonParticipantAccessForbidden() throws Exception {
        MvcResult convRes = mockMvc.perform(get("/api/conversations/requirement/" + requirementId + "?freelancerId=" + freelancerId1)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andReturn();
        Long convId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Freelancer 2 attempts to read messages
        mockMvc.perform(get("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + freelancerToken2))
                .andExpect(status().isForbidden());

        // Freelancer 2 attempts to send message
        SendMessageRequest msg = new SendMessageRequest("Attempting unauthorized message insertion.");
        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + freelancerToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MSG-003: Blank message validation returns HTTP 400")
    void testBlankMessageThrowsBadRequest() throws Exception {
        MvcResult convRes = mockMvc.perform(get("/api/conversations/requirement/" + requirementId + "?freelancerId=" + freelancerId1)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andReturn();
        Long convId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        SendMessageRequest blankMsg = new SendMessageRequest("   ");
        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankMsg)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("MSG-001: User conversations list returns active conversations with last message preview")
    void testGetUserConversationsList() throws Exception {
        // Studio initiates conversation and sends message
        MvcResult convRes = mockMvc.perform(get("/api/conversations/requirement/" + requirementId + "?freelancerId=" + freelancerId1)
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andReturn();
        Long convId = objectMapper.readTree(convRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        SendMessageRequest msg = new SendMessageRequest("Hi Manoj, let's discuss lighting gear.");
        mockMvc.perform(post("/api/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + studioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isCreated());

        // Freelancer checks conversations list
        mockMvc.perform(get("/api/conversations")
                        .header("Authorization", "Bearer " + freelancerToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(convId))
                .andExpect(jsonPath("$.data[0].lastMessage").value("Hi Manoj, let's discuss lighting gear."))
                .andExpect(jsonPath("$.data[0].studioName").value("Aura Visual Studios"));

        // Studio checks conversations list
        mockMvc.perform(get("/api/conversations")
                        .header("Authorization", "Bearer " + studioToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(convId))
                .andExpect(jsonPath("$.data[0].freelancerName").value("Manoj Kumar"));
    }

    private String registerAndGetToken(String email, UserRole role) throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail(email);
        dto.setPassword("Password123!");
        dto.setRole(role);

        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        return json.get("data").get("token").asText();
    }

    @Autowired
    private com.studiolynk.repository.UserRepository userRepository;

    private com.studiolynk.model.entity.User userRepositoryFindByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }
}
