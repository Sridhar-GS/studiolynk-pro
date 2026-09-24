package com.studiolynk;

import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.SkillRepository;
import com.studiolynk.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudioLynkApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Test
    @DisplayName("Context loads, Flyway runs V1 migration, and JPA entities validate against MySQL")
    void contextLoads() {
        assertThat(userRepository).isNotNull();
        assertThat(skillRepository).isNotNull();
    }

    @Test
    @DisplayName("Health endpoint returns status UP")
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("studiolynk-backend"));
    }

    @Test
    @DisplayName("Catalogue endpoint returns Flyway-seeded master skills")
    void catalogueSkillsReturnsSeededData() throws Exception {
        mockMvc.perform(get("/api/skills").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").exists());
    }

    @Test
    @DisplayName("User entity persistence and retrieval via Spring Data JPA")
    void testUserPersistence() {
        String testEmail = "test_" + System.currentTimeMillis() + "@studiolynk.local";
        User user = new User(testEmail, "hashed_secret_test", UserRole.STUDIO);
        user.setOnboardingCompleted(false);

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        User retrieved = userRepository.findByEmail(testEmail).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getRole()).isEqualTo(UserRole.STUDIO);
    }

    @Test
    @DisplayName("OpenAPI JSON documentation is accessible")
    void openApiDocsAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("StudioLynk Backend API"));
    }
}
