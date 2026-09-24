package com.studiolynk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studiolynk.model.dto.CreateCategoryRequestDto;
import com.studiolynk.model.dto.FreelancerOnboardingRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.ReorderItemsRequestDto;
import com.studiolynk.model.dto.UpdateCategoryRequestDto;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.PortfolioCategoryRepository;
import com.studiolynk.repository.PortfolioImageRepository;
import com.studiolynk.repository.PortfolioRepository;
import com.studiolynk.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PortfolioControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FreelancerRepository freelancerRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioCategoryRepository categoryRepository;

    @Autowired
    private PortfolioImageRepository imageRepository;

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

    private Long onboardFreelancer(String token, String name) throws Exception {
        FreelancerOnboardingRequestDto request = new FreelancerOnboardingRequestDto();
        request.setFullName(name);
        request.setPhone("+91 94444 88888");
        request.setAddress("Mylapore, Chennai");
        request.setFullDayRate(new BigDecimal("15000.00"));
        request.setHalfDayRate(new BigDecimal("8000.00"));

        MvcResult result = mockMvc.perform(post("/api/onboarding/freelancer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    @Test
    @DisplayName("POR-001: Auto-create and Retrieve Portfolio via /api/portfolio/me")
    void testGetOrCreatePortfolio() throws Exception {
        String email = "portfolio.user." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);
        onboardFreelancer(token, "Vikram Prabhu");

        mockMvc.perform(get("/api/portfolio/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.freelancerName").value("Vikram Prabhu"))
                .andExpect(jsonPath("$.data.categories").isArray());
    }

    @Test
    @DisplayName("POR-002, POR-003, POR-005: Create, Update, and Delete Portfolio Category")
    void testCategoryCrudOperations() throws Exception {
        String email = "cat.user." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);
        onboardFreelancer(token, "Siddharth Venkat");

        // 1. Create Category (POR-002, POR-003)
        CreateCategoryRequestDto createDto = new CreateCategoryRequestDto("Traditional Weddings", 0);
        MvcResult createResult = mockMvc.perform(post("/api/portfolio/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Traditional Weddings"))
                .andReturn();

        JsonNode root = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long categoryId = root.path("data").path("id").asLong();

        // 2. Update Category Name (POR-005)
        UpdateCategoryRequestDto updateDto = new UpdateCategoryRequestDto("Royal South Indian Weddings", 1);
        mockMvc.perform(put("/api/portfolio/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Royal South Indian Weddings"))
                .andExpect(jsonPath("$.data.sortOrder").value(1));

        // 3. Delete Category (POR-005)
        mockMvc.perform(delete("/api/portfolio/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    @DisplayName("POR-004, POR-006, POR-008: Upload Images, Reorder (POR-006), and Delete Image")
    void testImageUploadReorderAndDelete() throws Exception {
        String email = "img.user." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);
        onboardFreelancer(token, "Ananya Shankar");

        // Create Category
        CreateCategoryRequestDto catDto = new CreateCategoryRequestDto("Pre-Wedding Shoot", 0);
        MvcResult catResult = mockMvc.perform(post("/api/portfolio/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catDto)))
                .andExpect(status().isCreated())
                .andReturn();
        long categoryId = objectMapper.readTree(catResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Mock image files (POR-004, POR-008)
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "photo1.jpg", "image/jpeg", "test-image-binary-data-1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "photo2.png", "image/png", "test-image-binary-data-2".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/portfolio/categories/" + categoryId + "/images")
                        .file(file1)
                        .file(file2)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andReturn();

        JsonNode uploadRoot = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        long img1Id = uploadRoot.path("data").get(0).path("id").asLong();
        long img2Id = uploadRoot.path("data").get(1).path("id").asLong();

        // Reorder Images (POR-006)
        ReorderItemsRequestDto reorderDto = new ReorderItemsRequestDto(List.of(img2Id, img1Id));
        mockMvc.perform(put("/api/portfolio/categories/" + categoryId + "/reorder")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reorderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(img2Id))
                .andExpect(jsonPath("$.data[0].sortOrder").value(0))
                .andExpect(jsonPath("$.data[1].id").value(img1Id))
                .andExpect(jsonPath("$.data[1].sortOrder").value(1));

        // Delete an Image
        mockMvc.perform(delete("/api/portfolio/images/" + img1Id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertThat(imageRepository.findById(img1Id)).isEmpty();
        assertThat(imageRepository.findById(img2Id)).isPresent();
    }

    @Test
    @DisplayName("POR-007: Studio / Public View of Freelancer Portfolio")
    void testPublicPortfolioViewing() throws Exception {
        String freelancerEmail = "freelancer.public." + System.currentTimeMillis() + "@studiolynk.local";
        String freelancerToken = registerAndGetToken(freelancerEmail, UserRole.FREELANCER);
        Long freelancerId = onboardFreelancer(freelancerToken, "Public Creator");

        // Create category
        CreateCategoryRequestDto catDto = new CreateCategoryRequestDto("Cinematic Portraits", 0);
        mockMvc.perform(post("/api/portfolio/categories")
                        .header("Authorization", "Bearer " + freelancerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catDto)))
                .andExpect(status().isCreated());

        // A studio or public client views this portfolio
        mockMvc.perform(get("/api/portfolio/freelancer/" + freelancerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.freelancerId").value(freelancerId))
                .andExpect(jsonPath("$.data.categories.length()").value(1))
                .andExpect(jsonPath("$.data.categories[0].name").value("Cinematic Portraits"));
    }

    @Test
    @DisplayName("General Profile Image Upload (/api/upload/image)")
    void testProfileImageUpload() throws Exception {
        String email = "upload.user." + System.currentTimeMillis() + "@studiolynk.local";
        String token = registerAndGetToken(email, UserRole.FREELANCER);

        MockMultipartFile avatarFile = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", "avatar-bytes-content".getBytes()
        );

        mockMvc.perform(multipart("/api/upload/image")
                        .file(avatarFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imageUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.s3Key").isNotEmpty());
    }
}
