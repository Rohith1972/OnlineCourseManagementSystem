package com.example.ocms.course.service;

import com.example.ocms.course.dto.CourseCreateRequest;
import com.example.ocms.course.dto.CoursePublishRequest;
import com.example.ocms.course.dto.CourseResponse;
import com.example.ocms.course.dto.CourseUpdateRequest;
import com.example.ocms.course.entity.Course;
import com.example.ocms.course.repository.CourseRepository;
import com.example.ocms.enrollment.repository.EnrollmentRepository;
import com.example.ocms.exception.BadRequestException;
import com.example.ocms.exception.ForbiddenException;
import com.example.ocms.exception.ResourceNotFoundException;
import com.example.ocms.security.UserPrincipal;
import com.example.ocms.user.entity.Role;
import com.example.ocms.user.entity.User;
import com.example.ocms.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CourseService(CourseRepository courseRepository,
                         UserRepository userRepository,
                         EnrollmentRepository enrollmentRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request, UserPrincipal currentUser) {
        if (!currentUser.getRole().equals(Role.INSTRUCTOR) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new ForbiddenException("Only instructors or administrators can create courses");
        }

        User instructor;
        if (currentUser.getRole().equals(Role.ADMIN) && request.getInstructorId() != null) {
            instructor = userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + request.getInstructorId()));
            if (!instructor.getRole().equals(Role.INSTRUCTOR)) {
                throw new BadRequestException("Assigned user must have the INSTRUCTOR role");
            }
        } else {
            instructor = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));
        }

        Course course = new Course(
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getCategory().trim(),
                instructor
        );

        Course savedCourse = courseRepository.save(course);
        return CourseResponse.fromEntity(savedCourse);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(String category, Boolean publishedOnly, Pageable pageable, UserPrincipal currentUser) {
        // If unauthenticated or STUDENT: strictly published courses only
        if (currentUser == null || currentUser.getRole().equals(Role.STUDENT)) {
            if (category != null && !category.isBlank()) {
                return courseRepository.findByPublishedTrueAndCategoryIgnoreCase(category.trim(), pageable)
                        .map(CourseResponse::fromEntity);
            }
            return courseRepository.findByPublishedTrue(pageable).map(CourseResponse::fromEntity);
        }

        // If ADMIN and publishedOnly is not explicitly true: can view all courses
        if (currentUser.getRole().equals(Role.ADMIN)) {
            if (Boolean.TRUE.equals(publishedOnly)) {
                if (category != null && !category.isBlank()) {
                    return courseRepository.findByPublishedTrueAndCategoryIgnoreCase(category.trim(), pageable)
                            .map(CourseResponse::fromEntity);
                }
                return courseRepository.findByPublishedTrue(pageable).map(CourseResponse::fromEntity);
            }
            if (category != null && !category.isBlank()) {
                return courseRepository.findByCategoryIgnoreCase(category.trim(), pageable)
                        .map(CourseResponse::fromEntity);
            }
            return courseRepository.findAll(pageable).map(CourseResponse::fromEntity);
        }

        // If INSTRUCTOR: can view their own courses or published courses
        if (currentUser.getRole().equals(Role.INSTRUCTOR)) {
            if (Boolean.TRUE.equals(publishedOnly)) {
                if (category != null && !category.isBlank()) {
                    return courseRepository.findByPublishedTrueAndCategoryIgnoreCase(category.trim(), pageable)
                            .map(CourseResponse::fromEntity);
                }
                return courseRepository.findByPublishedTrue(pageable).map(CourseResponse::fromEntity);
            }
            // By default return their own courses for instructor management
            if (category != null && !category.isBlank()) {
                return courseRepository.findByInstructorIdAndCategoryIgnoreCase(currentUser.getId(), category.trim(), pageable)
                        .map(CourseResponse::fromEntity);
            }
            return courseRepository.findByInstructorId(currentUser.getId(), pageable)
                    .map(CourseResponse::fromEntity);
        }

        return courseRepository.findByPublishedTrue(pageable).map(CourseResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long courseId, UserPrincipal currentUser) {
        Course course = findCourseOrThrow(courseId);

        // If course is published, anyone can view it
        if (course.isPublished()) {
            return CourseResponse.fromEntity(course);
        }

        // If unpublished, only course owner instructor or admin can view it
        if (currentUser == null) {
            throw new ResourceNotFoundException("Course not found or is currently not published");
        }

        if (!canManageCourse(course, currentUser)) {
            throw new ForbiddenException("You do not have permission to view this unpublished course");
        }

        return CourseResponse.fromEntity(course);
    }

    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseUpdateRequest request, UserPrincipal currentUser) {
        Course course = findCourseOrThrow(courseId);
        assertCanManageCourse(course, currentUser);

        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setCategory(request.getCategory().trim());

        Course updatedCourse = courseRepository.save(course);
        eventPublisher.publishEvent(new com.example.ocms.course.event.CourseUpdatedEvent(updatedCourse));
        return CourseResponse.fromEntity(updatedCourse);
    }

    @Transactional
    public CourseResponse publishCourse(Long courseId, CoursePublishRequest request, UserPrincipal currentUser) {
        Course course = findCourseOrThrow(courseId);
        assertCanManageCourse(course, currentUser);

        boolean newStatus = (request != null && request.getPublished() != null)
                ? request.getPublished()
                : !course.isPublished();

        course.setPublished(newStatus);
        Course updatedCourse = courseRepository.save(course);
        if (updatedCourse.isPublished()) {
            eventPublisher.publishEvent(new com.example.ocms.course.event.CoursePublishedEvent(updatedCourse));
        }
        return CourseResponse.fromEntity(updatedCourse);
    }

    @Transactional
    public void deleteCourse(Long courseId, UserPrincipal currentUser) {
        Course course = findCourseOrThrow(courseId);
        assertCanManageCourse(course, currentUser);

        // Prevent unsafe deletion of courses that have enrollment history
        if (enrollmentRepository.existsByCourseId(courseId)) {
            throw new BadRequestException("Cannot delete course: active or historical student enrollments exist for this course");
        }

        courseRepository.delete(course);
    }

    public Course findCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
    }

    public void assertCanManageCourse(Course course, UserPrincipal currentUser) {
        if (!canManageCourse(course, currentUser)) {
            throw new ForbiddenException("You do not have permission to manage this course");
        }
    }

    public boolean canManageCourse(Course course, UserPrincipal currentUser) {
        if (currentUser == null) return false;
        if (currentUser.getRole().equals(Role.ADMIN)) return true;
        return currentUser.getRole().equals(Role.INSTRUCTOR) &&
                course.getInstructor().getId().equals(currentUser.getId());
    }
}
