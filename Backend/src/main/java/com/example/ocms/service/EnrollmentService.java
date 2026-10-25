package com.example.ocms.service;

import com.example.ocms.entity.CourseContent;
import com.example.ocms.repository.CourseContentRepository;
import com.example.ocms.entity.Course;
import com.example.ocms.service.CourseService;
import com.example.ocms.dto.ContentProgressResponse;
import com.example.ocms.dto.EnrollmentResponse;
import com.example.ocms.entity.ContentProgress;
import com.example.ocms.entity.Enrollment;
import com.example.ocms.entity.EnrollmentStatus;
import com.example.ocms.event.StudentEnrolledEvent;
import com.example.ocms.repository.ContentProgressRepository;
import com.example.ocms.repository.EnrollmentRepository;
import com.example.ocms.exception.BadRequestException;
import com.example.ocms.exception.DuplicateResourceException;
import com.example.ocms.exception.ForbiddenException;
import com.example.ocms.exception.ResourceNotFoundException;
import com.example.ocms.security.UserPrincipal;
import com.example.ocms.entity.Role;
import com.example.ocms.entity.User;
import com.example.ocms.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ContentProgressRepository contentProgressRepository;
    private final CourseContentRepository contentRepository;
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             ContentProgressRepository contentProgressRepository,
                             CourseContentRepository contentRepository,
                             CourseService courseService,
                             UserRepository userRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.enrollmentRepository = enrollmentRepository;
        this.contentProgressRepository = contentProgressRepository;
        this.contentRepository = contentRepository;
        this.courseService = courseService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public EnrollmentResponse enroll(Long courseId, UserPrincipal currentUser) {
        if (currentUser.getRole().equals(Role.INSTRUCTOR)) {
            throw new ForbiddenException("Instructors cannot enroll in courses as students");
        }

        Course course = courseService.findCourseOrThrow(courseId);

        if (!course.isPublished()) {
            throw new BadRequestException("Cannot enroll in an unpublished course");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(currentUser.getId(), courseId)) {
            throw new DuplicateResourceException("You are already enrolled in this course");
        }

        User student = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found"));

        Enrollment enrollment = new Enrollment(student, course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // Publish event for notification trigger
        eventPublisher.publishEvent(new StudentEnrolledEvent(student, course));

        return EnrollmentResponse.fromEntity(savedEnrollment);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getMyOngoingEnrollments(Pageable pageable, UserPrincipal currentUser) {
        return enrollmentRepository.findByStudentIdAndStatus(currentUser.getId(), EnrollmentStatus.ONGOING, pageable)
                .map(EnrollmentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getMyEnrollmentHistory(Pageable pageable, UserPrincipal currentUser) {
        return enrollmentRepository.findByStudentId(currentUser.getId(), pageable)
                .map(EnrollmentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getCourseEnrolledStudents(Long courseId, Pageable pageable, UserPrincipal currentUser) {
        Course course = courseService.findCourseOrThrow(courseId);
        courseService.assertCanManageCourse(course, currentUser);

        return enrollmentRepository.findByCourseId(courseId, pageable)
                .map(EnrollmentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getCourseEnrolledStudentsForInstructor(
            Long instructorId, Long courseId, Pageable pageable, UserPrincipal currentUser) {
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + instructorId));

        if (!instructor.getRole().equals(Role.INSTRUCTOR)) {
            throw new BadRequestException("User with id " + instructorId + " is not an instructor");
        }

        Course course = courseService.findCourseOrThrow(courseId);

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new BadRequestException("Course with id " + courseId + " does not belong to instructor with id " + instructorId);
        }

        courseService.assertCanManageCourse(course, currentUser);

        return enrollmentRepository.findByCourseId(courseId, pageable)
                .map(EnrollmentResponse::fromEntity);
    }


    @Transactional
    public ContentProgressResponse updateProgress(Long courseId, Long contentId, Boolean completed, UserPrincipal currentUser) {
        Course course = courseService.findCourseOrThrow(courseId);

        CourseContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        if (!content.getCourse().getId().equals(course.getId())) {
            throw new BadRequestException("The specified content item does not belong to this course");
        }

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new ForbiddenException("You must be enrolled in this course to record progress"));

        ContentProgress progress = contentProgressRepository
                .findByEnrollmentIdAndContentId(enrollment.getId(), content.getId())
                .orElseGet(() -> new ContentProgress(enrollment, content, false));

        boolean markCompleted = (completed != null) ? completed : true;
        progress.setCompleted(markCompleted);
        ContentProgress savedProgress = contentProgressRepository.save(progress);

        // Recalculate course progress
        int totalItems = contentRepository.countByCourseId(courseId);
        long completedItems = contentProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());

        double percentage = (totalItems > 0)
                ? Math.min(100.0, Math.round(((double) completedItems / totalItems * 100.0) * 10.0) / 10.0)
                : 100.0;

        enrollment.setProgressPercentage(percentage);
        if (percentage >= 100.0) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletionDate(LocalDateTime.now());
        } else {
            enrollment.setStatus(EnrollmentStatus.ONGOING);
            enrollment.setCompletionDate(null);
        }

        enrollmentRepository.save(enrollment);

        return ContentProgressResponse.fromEntity(savedProgress);
    }
}
