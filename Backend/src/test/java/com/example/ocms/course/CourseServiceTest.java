package com.example.ocms.course;

import com.example.ocms.course.dto.CourseCreateRequest;
import com.example.ocms.course.dto.CourseResponse;
import com.example.ocms.course.dto.CourseUpdateRequest;
import com.example.ocms.course.entity.Course;
import com.example.ocms.course.repository.CourseRepository;
import com.example.ocms.course.service.CourseService;
import com.example.ocms.enrollment.repository.EnrollmentRepository;
import com.example.ocms.exception.BadRequestException;
import com.example.ocms.exception.ForbiddenException;
import com.example.ocms.security.UserPrincipal;
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
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CourseService courseService;

    private User instructor;
    private User otherInstructor;
    private UserPrincipal instructorPrincipal;
    private UserPrincipal otherInstructorPrincipal;
    private UserPrincipal studentPrincipal;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        instructor = new User("Instructor Alice", "alice@example.com", "pass", "123", Role.INSTRUCTOR);
        instructor.setId(10L);
        instructor.setCreatedAt(LocalDateTime.now());
        instructor.setUpdatedAt(LocalDateTime.now());

        otherInstructor = new User("Instructor Bob", "bob@example.com", "pass", "456", Role.INSTRUCTOR);
        otherInstructor.setId(20L);
        otherInstructor.setCreatedAt(LocalDateTime.now());
        otherInstructor.setUpdatedAt(LocalDateTime.now());

        instructorPrincipal = UserPrincipal.create(instructor);
        otherInstructorPrincipal = UserPrincipal.create(otherInstructor);

        User student = new User("Student Charlie", "charlie@example.com", "pass", "789", Role.STUDENT);
        student.setId(30L);
        studentPrincipal = UserPrincipal.create(student);

        testCourse = new Course("Java Masterclass", "Learn Java 21", "Programming", instructor);
        testCourse.setId(100L);
        testCourse.setCreatedAt(LocalDateTime.now());
        testCourse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully create a course by instructor")
    void shouldCreateCourseByInstructor() {
        CourseCreateRequest request = new CourseCreateRequest("Java Masterclass", "Learn Java 21", "Programming");
        when(userRepository.findById(10L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        CourseResponse response = courseService.createCourse(request, instructorPrincipal);

        assertNotNull(response);
        assertEquals("Java Masterclass", response.getTitle());
        assertEquals("Programming", response.getCategory());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("Should forbid student from creating a course")
    void shouldForbidStudentFromCreatingCourse() {
        CourseCreateRequest request = new CourseCreateRequest("Java Masterclass", "Learn Java 21", "Programming");

        assertThrows(ForbiddenException.class, () -> courseService.createCourse(request, studentPrincipal));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should forbid non-owner instructor from modifying course")
    void shouldForbidNonOwnerFromModifyingCourse() {
        CourseUpdateRequest updateRequest = new CourseUpdateRequest("Updated Title", "Updated Desc", "Programming");
        when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));

        assertThrows(ForbiddenException.class, () -> courseService.updateCourse(100L, updateRequest, otherInstructorPrincipal));
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should allow course owner instructor to update course")
    void shouldAllowOwnerToUpdateCourse() {
        CourseUpdateRequest updateRequest = new CourseUpdateRequest("Updated Title", "Updated Desc", "Programming");
        when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        CourseResponse response = courseService.updateCourse(100L, updateRequest, instructorPrincipal);

        assertNotNull(response);
        verify(courseRepository, times(1)).save(testCourse);
    }

    @Test
    @DisplayName("Should prevent course deletion when course has enrollment history")
    void shouldPreventCourseDeletionWithEnrollmentHistory() {
        when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByCourseId(100L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> courseService.deleteCourse(100L, instructorPrincipal));
        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    @DisplayName("Should allow course deletion when course has no enrollment history")
    void shouldAllowCourseDeletionWithoutEnrollmentHistory() {
        when(courseRepository.findById(100L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByCourseId(100L)).thenReturn(false);

        assertDoesNotThrow(() -> courseService.deleteCourse(100L, instructorPrincipal));
        verify(courseRepository, times(1)).delete(testCourse);
    }
}
