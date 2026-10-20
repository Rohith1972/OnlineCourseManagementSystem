package com.example.ocms.auth;

import com.example.ocms.auth.dto.AuthResponse;
import com.example.ocms.auth.dto.InstructorRegisterRequest;
import com.example.ocms.auth.dto.LoginRequest;
import com.example.ocms.auth.dto.StudentRegisterRequest;
import com.example.ocms.auth.service.AuthService;
import com.example.ocms.exception.DuplicateResourceException;
import com.example.ocms.security.JwtUtils;
import com.example.ocms.security.UserPrincipal;
import com.example.ocms.user.dto.UserResponse;
import com.example.ocms.user.entity.Role;
import com.example.ocms.user.entity.User;
import com.example.ocms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User studentUser;
    private User instructorUser;

    @BeforeEach
    void setUp() {
        studentUser = new User("John Doe", "john@example.com", "encodedPassword", "1234567890", Role.STUDENT);
        studentUser.setId(1L);
        studentUser.setCreatedAt(LocalDateTime.now());
        studentUser.setUpdatedAt(LocalDateTime.now());

        instructorUser = new User("Jane Smith", "jane@example.com", "encodedPassword", "0987654321", Role.INSTRUCTOR);
        instructorUser.setId(2L);
        instructorUser.setCreatedAt(LocalDateTime.now());
        instructorUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully register a new student")
    void shouldRegisterStudentSuccessfully() {
        StudentRegisterRequest request = new StudentRegisterRequest("John Doe", "john@example.com", "password123", "1234567890");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(studentUser);

        UserResponse response = authService.registerStudent(request);

        assertNotNull(response);
        assertEquals("John Doe", response.getFullName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.STUDENT, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully register a new instructor")
    void shouldRegisterInstructorSuccessfully() {
        InstructorRegisterRequest request = new InstructorRegisterRequest("Jane Smith", "jane@example.com", "password123", "0987654321");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(instructorUser);

        UserResponse response = authService.registerInstructor(request);

        assertNotNull(response);
        assertEquals("Jane Smith", response.getFullName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals(Role.INSTRUCTOR, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowWhenEmailAlreadyExists() {
        StudentRegisterRequest request = new StudentRegisterRequest("John Doe", "john@example.com", "password123", "1234567890");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.registerStudent(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate and return JWT token on login")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        UserPrincipal principal = UserPrincipal.create(studentUser);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("mocked-jwt-token");
        when(userRepository.findById(1L)).thenReturn(Optional.of(studentUser));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john@example.com", response.getUser().getEmail());
    }
}
