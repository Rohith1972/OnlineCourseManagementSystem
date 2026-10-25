package com.example.ocms.service;

import com.example.ocms.dto.CourseContentRequest;
import com.example.ocms.dto.CourseContentResponse;
import com.example.ocms.entity.ContentType;
import com.example.ocms.entity.CourseContent;
import com.example.ocms.repository.CourseContentRepository;
import com.example.ocms.service.CourseContentService;
import com.example.ocms.entity.Course;
import com.example.ocms.service.CourseService;
import com.example.ocms.repository.EnrollmentRepository;
import com.example.ocms.exception.ForbiddenException;
import com.example.ocms.security.UserPrincipal;
import com.example.ocms.entity.Role;
import com.example.ocms.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseContentServiceTest {

    @Mock
    private CourseContentRepository contentRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseContentService contentService;

    private User instructor;
    private User student;
    private UserPrincipal instructorPrincipal;
    private UserPrincipal studentPrincipal;
    private Course course;
    private CourseContent content;

    @BeforeEach
    void setUp() {
        instructor = new User("Alice", "alice@example.com", "pass", "123", Role.INSTRUCTOR);
        instructor.setId(10L);
        instructorPrincipal = UserPrincipal.create(instructor);

        student = new User("Bob", "bob@example.com", "pass", "456", Role.STUDENT);
        student.setId(20L);
        studentPrincipal = UserPrincipal.create(student);

        course = new Course("Java Course", "Description", "Programming", instructor);
        course.setId(100L);

        content = new CourseContent(course, "Lesson 1: Intro", ContentType.VIDEO, "https://example.com/video1.mp4", 1);
        content.setId(50L);
    }

    @Test
    @DisplayName("Should successfully add course content by instructor owner")
    void shouldAddContentByInstructor() {
        CourseContentRequest request = new CourseContentRequest("Lesson 1: Intro", ContentType.VIDEO, "https://example.com/video1.mp4", 1);

        when(courseService.findCourseOrThrow(100L)).thenReturn(course);
        doNothing().when(courseService).assertCanManageCourse(course, instructorPrincipal);
        when(contentRepository.save(any(CourseContent.class))).thenReturn(content);

        CourseContentResponse response = contentService.addContent(100L, request, instructorPrincipal);

        assertNotNull(response);
        assertEquals("Lesson 1: Intro", response.getTitle());
        assertEquals(ContentType.VIDEO, response.getContentType());
        verify(contentRepository, times(1)).save(any(CourseContent.class));
    }

    @Test
    @DisplayName("Should allow enrolled student to view course contents")
    void shouldAllowEnrolledStudentToViewContents() {
        when(courseService.findCourseOrThrow(100L)).thenReturn(course);
        when(courseService.canManageCourse(course, studentPrincipal)).thenReturn(false);
        when(enrollmentRepository.existsByStudentIdAndCourseId(20L, 100L)).thenReturn(true);
        when(contentRepository.findByCourseIdOrderByPositionAsc(100L)).thenReturn(List.of(content));

        List<CourseContentResponse> list = contentService.getContentsForCourse(100L, studentPrincipal);

        assertEquals(1, list.size());
        assertEquals("Lesson 1: Intro", list.get(0).getTitle());
    }

    @Test
    @DisplayName("Should forbid unenrolled student from viewing course contents")
    void shouldForbidUnenrolledStudentFromViewingContents() {
        when(courseService.findCourseOrThrow(100L)).thenReturn(course);
        when(courseService.canManageCourse(course, studentPrincipal)).thenReturn(false);
        when(enrollmentRepository.existsByStudentIdAndCourseId(20L, 100L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> contentService.getContentsForCourse(100L, studentPrincipal));
        verify(contentRepository, never()).findByCourseIdOrderByPositionAsc(anyLong());
    }
}
