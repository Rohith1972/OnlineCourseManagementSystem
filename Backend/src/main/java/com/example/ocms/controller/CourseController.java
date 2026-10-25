package com.example.ocms.controller;

import com.example.ocms.dto.CourseCreateRequest;
import com.example.ocms.dto.CoursePublishRequest;
import com.example.ocms.dto.CourseResponse;
import com.example.ocms.dto.CourseUpdateRequest;
import com.example.ocms.service.CourseService;
import com.example.ocms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Management", description = "Endpoints for creating, managing, browsing, and publishing courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new course (Instructor or Admin)")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CourseResponse response = courseService.createCourse(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Browse courses with optional category, instructor filter and pagination")
    public ResponseEntity<Page<CourseResponse>> getCourses(
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean publishedOnly,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (instructorId != null) {
            Page<CourseResponse> response = courseService.getPublishedCoursesByInstructor(instructorId, category, pageable);
            return ResponseEntity.ok(response);
        }
        Page<CourseResponse> response = courseService.getCourses(category, publishedOnly, pageable, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/instructor/{instructorId}", "/instructor/{instructorId}/published"})
    @Operation(summary = "Get all courses published by a specific instructor")
    public ResponseEntity<Page<CourseResponse>> getPublishedCoursesByInstructor(
            @PathVariable Long instructorId,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CourseResponse> response = courseService.getPublishedCoursesByInstructor(instructorId, category, pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{courseId}")
    @Operation(summary = "Get course details by ID (Public for published; Instructor/Admin for unpublished)")
    public ResponseEntity<CourseResponse> getCourseById(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CourseResponse response = courseService.getCourseById(courseId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update course details (Course Instructor or Admin)")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CourseResponse response = courseService.updateCourse(courseId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{courseId}/publish")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publish or unpublish course (Course Instructor or Admin)")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable Long courseId,
            @RequestBody(required = false) CoursePublishRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CourseResponse response = courseService.publishCourse(courseId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete course (Course Instructor or Admin; fails if course has enrollments)")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        courseService.deleteCourse(courseId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
