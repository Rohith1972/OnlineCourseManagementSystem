package com.example.ocms.security;

import com.example.ocms.auth.dto.LoginRequest;
import com.example.ocms.auth.dto.StudentRegisterRequest;
import com.example.ocms.user.entity.Role;
import com.example.ocms.user.entity.User;
import com.example.ocms.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SecurityAndAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private User student1;
    private User student2;
    private User admin;
    private String student1Token;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        student1 = new User("Student One", "student1@example.com", passwordEncoder.encode("Password123"), "1111111111", Role.STUDENT);
        student1 = userRepository.save(student1);

        student2 = new User("Student Two", "student2@example.com", passwordEncoder.encode("Password123"), "2222222222", Role.STUDENT);
        student2 = userRepository.save(student2);

        admin = new User("Admin User", "admin_sec@example.com", passwordEncoder.encode("Password123"), "9999999999", Role.ADMIN);
        admin = userRepository.save(admin);

        student1Token = jwtUtils.generateTokenFromUsername(student1.getEmail(), student1.getId(), student1.getRole().name());
        adminToken = jwtUtils.generateTokenFromUsername(admin.getEmail(), admin.getId(), admin.getRole().name());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when unauthenticated request accesses protected endpoint")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 200 OK when authenticated student accesses their own profile")
    void shouldReturn200ForAuthenticatedProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student1@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Should return 403 Forbidden when student tries to access admin-only endpoint")
    void shouldReturn403WhenStudentAccessesAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 200 OK when admin accesses admin-only user listing")
    void shouldReturn200WhenAdminAccessesUserListing() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should return 403 Forbidden when a student tries to view another user's profile by ID")
    void shouldReturn403WhenStudentViewsAnotherUserProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + student2.getId())
                        .header("Authorization", "Bearer " + student1Token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow student registration and return 201 Created")
    void shouldRegisterStudentViaRestApi() throws Exception {
        StudentRegisterRequest request = new StudentRegisterRequest("New Student", "newstudent@example.com", "Password123", "3333333333");

        mockMvc.perform(post("/api/v1/auth/register/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newstudent@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("Should authenticate valid credentials and return JWT token")
    void shouldLoginAndReturnJwt() throws Exception {
        LoginRequest loginRequest = new LoginRequest("student1@example.com", "Password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("student1@example.com"));
    }
}
