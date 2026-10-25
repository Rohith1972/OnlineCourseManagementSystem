package com.example.ocms.service;

import com.example.ocms.dto.CourseContentRequest;
import com.example.ocms.dto.CourseContentResponse;
import com.example.ocms.entity.CourseContent;
import com.example.ocms.repository.CourseContentRepository;
import com.example.ocms.entity.Course;
import com.example.ocms.service.CourseService;
import com.example.ocms.repository.EnrollmentRepository;
import com.example.ocms.exception.ForbiddenException;
import com.example.ocms.exception.ResourceNotFoundException;
import com.example.ocms.security.UserPrincipal;
import com.example.ocms.entity.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseContentService {

    private final CourseContentRepository contentRepository;
    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepository;

    public CourseContentService(CourseContentRepository contentRepository,
                                CourseService courseService,
                                EnrollmentRepository enrollmentRepository) {
        this.contentRepository = contentRepository;
        this.courseService = courseService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public CourseContentResponse addContent(Long courseId, CourseContentRequest request, UserPrincipal currentUser) {
        Course course = courseService.findCourseOrThrow(courseId);
        courseService.assertCanManageCourse(course, currentUser);

        int position = (request.getPosition() != null && request.getPosition() > 0)
                ? request.getPosition()
                : contentRepository.countByCourseId(courseId) + 1;

        CourseContent content = new CourseContent(
                course,
                request.getTitle().trim(),
                request.getContentType(),
                request.getResourceUrl().trim(),
                position
        );

        CourseContent saved = contentRepository.save(content);
        return CourseContentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseContentResponse> getContentsForCourse(Long courseId, UserPrincipal currentUser) {
        Course course = courseService.findCourseOrThrow(courseId);

        if (currentUser == null) {
            throw new ForbiddenException("Authentication is required to view course contents");
        }

        // Course owner or Admin can always view contents
        boolean isOwnerOrAdmin = courseService.canManageCourse(course, currentUser);

        if (!isOwnerOrAdmin) {
            // Must be an enrolled student
            boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(currentUser.getId(), courseId);
            if (!isEnrolled) {
                throw new ForbiddenException("You must be enrolled in this course to access its learning content");
            }
        }

        return contentRepository.findByCourseIdOrderByPositionAsc(courseId)
                .stream()
                .map(CourseContentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseContentResponse updateContent(Long contentId, CourseContentRequest request, UserPrincipal currentUser) {
        CourseContent content = findContentOrThrow(contentId);
        courseService.assertCanManageCourse(content.getCourse(), currentUser);

        content.setTitle(request.getTitle().trim());
        content.setContentType(request.getContentType());
        content.setResourceUrl(request.getResourceUrl().trim());
        if (request.getPosition() != null && request.getPosition() > 0) {
            content.setPosition(request.getPosition());
        }

        CourseContent updated = contentRepository.save(content);
        return CourseContentResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteContent(Long contentId, UserPrincipal currentUser) {
        CourseContent content = findContentOrThrow(contentId);
        courseService.assertCanManageCourse(content.getCourse(), currentUser);

        contentRepository.delete(content);
    }

    public CourseContent findContentOrThrow(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Course content not found with id: " + contentId));
    }
}
