package com.example.ocms.enrollment.controller;

import com.example.ocms.enrollment.dto.ContentProgressRequest;
import com.example.ocms.enrollment.dto.ContentProgressResponse;
import com.example.ocms.enrollment.dto.EnrollmentResponse;
import com.example.ocms.enrollment.service.EnrollmentService;
import com.example.ocms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Enrollment & Progress Tracking", description = "Endpoints for course enrollment, progress updates, and enrollment history")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Enroll current student in a published course")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        EnrollmentResponse response = enrollmentService.enroll(courseId, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/enrollments/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get current student's active ongoing enrollments")
    public ResponseEntity<Page<EnrollmentResponse>> getMyOngoingEnrollments(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<EnrollmentResponse> response = enrollmentService.getMyOngoingEnrollments(pageable, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/enrollments/my/history")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Get current student's full enrollment history (all statuses)")
    public ResponseEntity<Page<EnrollmentResponse>> getMyEnrollmentHistory(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<EnrollmentResponse> response = enrollmentService.getMyEnrollmentHistory(pageable, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses/{courseId}/students")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Get enrolled students for a course (Course Instructor or Admin)")
    public ResponseEntity<Page<EnrollmentResponse>> getCourseEnrolledStudents(
            @PathVariable Long courseId,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Page<EnrollmentResponse> response = enrollmentService.getCourseEnrolledStudents(courseId, pageable, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/courses/{courseId}/content/{contentId}/progress")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @Operation(summary = "Mark a learning item completed or incomplete and recalculate progress")
    public ResponseEntity<ContentProgressResponse> updateContentProgress(
            @PathVariable Long courseId,
            @PathVariable Long contentId,
            @RequestBody(required = false) ContentProgressRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Boolean completed = (request != null) ? request.getCompleted() : true;
        ContentProgressResponse response = enrollmentService.updateProgress(courseId, contentId, completed, currentUser);
        return ResponseEntity.ok(response);
    }
}
