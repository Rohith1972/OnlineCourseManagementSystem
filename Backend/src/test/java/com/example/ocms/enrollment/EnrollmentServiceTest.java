package com.example.ocms.enrollment;

import com.example.ocms.content.entity.ContentType;
import com.example.ocms.content.entity.CourseContent;
import com.example.ocms.content.repository.CourseContentRepository;
import com.example.ocms.course.entity.Course;
import com.example.ocms.course.service.CourseService;
import com.example.ocms.enrollment.dto.ContentProgressResponse;
import com.example.ocms.enrollment.dto.EnrollmentResponse;
import com.example.ocms.enrollment.service.EnrollmentService;
import com.example.ocms.enrollment.entity.ContentProgress;
import com.example.ocms.enrollment.entity.Enrollment;
import com.example.ocms.enrollment.entity.EnrollmentStatus;
import com.example.ocms.enrollment.event.StudentEnrolledEvent;
import com.example.ocms.enrollment.repository.ContentProgressRepository;
import com.example.ocms.enrollment.repository.EnrollmentRepository;
import com.example.ocms.exception.BadRequestException;
import com.example.ocms.exception.DuplicateResourceException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ContentProgressRepository contentProgressRepository;

    @Mock
    private CourseContentRepository contentRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User student;
    private User instructor;
    private UserPrincipal studentPrincipal;
    private Course course;
    private Enrollment enrollment;
    private CourseContent content1;

    @BeforeEach
    void setUp() {
        student = new User("Student Bob", "bob@example.com", "pass", "123", Role.STUDENT);
        student.setId(10L);
        studentPrincipal = UserPrincipal.create(student);

        instructor = new User("Instructor Alice", "alice@example.com", "pass", "456", Role.INSTRUCTOR);
        instructor.setId(20L);

        course = new Course("Spring Boot", "Learn Spring Boot 3", "Programming", instructor);
        course.setId(100L);
        course.setPublished(true);

        enrollment = new Enrollment(student, course);
        enrollment.setId(500L);

        content1 = new CourseContent(course, "Intro Video", ContentType.VIDEO, "https://url.com/1", 1);
        content1.setId(50L);
    }

    @Test
    @DisplayName("Should successfully enroll student in published course")
    void shouldEnrollStudentSuccessfully() {
        when(courseService.findCourseOrThrow(100L)).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 100L)).thenReturn(false);
        when(userRepository.findById(10L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.enroll(100L, studentPrincipal);

        assertNotNull(response);
        assertEquals(EnrollmentStatus.ONGOING, response.getStatus());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
        verify(eventPublisher, times(1)).publishEvent(any(StudentEnrolledEvent.class));
    }

    @Test
    @DisplayName("Should reject enrollment if course is unpublished")
    void shouldRejectEnrollmentForUnpublishedCourse() {
        course.setPublished(false);
        when(courseService.findCourseOrThrow(100L)).thenReturn(course);

        assertThrows(BadRequestException.class, () -> enrollmentService.enroll(100L, studentPrincipal));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should reject duplicate enrollment in the same course")
    void shouldRejectDuplicateEnrollment() {
        when(courseService.findCourseOrThrow(100L)).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 100L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> enrollmentService.enroll(100L, studentPrincipal));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should mark content completed and calculate 100% progress for completed course")
    void shouldUpdateProgressAndCompleteCourse() {
        when(courseService.findCourseOrThrow(100L)).thenReturn(course);
        when(contentRepository.findById(50L)).thenReturn(Optional.of(content1));
        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 100L)).thenReturn(Optional.of(enrollment));

        ContentProgress progress = new ContentProgress(enrollment, content1, true);
        progress.setId(1L);
        when(contentProgressRepository.findByEnrollmentIdAndContentId(500L, 50L)).thenReturn(Optional.of(progress));
        when(contentProgressRepository.save(any(ContentProgress.class))).thenReturn(progress);

        // Course has 1 total item, 1 completed
        when(contentRepository.countByCourseId(100L)).thenReturn(1);
        when(contentProgressRepository.countByEnrollmentIdAndCompletedTrue(500L)).thenReturn(1L);

        ContentProgressResponse response = enrollmentService.updateProgress(100L, 50L, true, studentPrincipal);

        assertNotNull(response);
        assertTrue(response.isCompleted());
        assertEquals(100.0, enrollment.getProgressPercentage());
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
        assertNotNull(enrollment.getCompletionDate());
        verify(enrollmentRepository, times(1)).save(enrollment);
    }
}
