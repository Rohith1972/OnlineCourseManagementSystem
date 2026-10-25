package com.example.ocms.controller;

import com.example.ocms.dto.CourseContentRequest;
import com.example.ocms.dto.CourseContentResponse;
import com.example.ocms.service.CourseContentService;
import com.example.ocms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Course Learning Content", description = "Endpoints for managing and accessing course materials (video, PDF, quiz metadata)")
@SecurityRequirement(name = "bearerAuth")
public class CourseContentController {

    private final CourseContentService contentService;

    public CourseContentController(CourseContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping("/courses/{courseId}/content")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Add learning content to a course (Course Instructor or Admin)")
    public ResponseEntity<CourseContentResponse> addContent(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseContentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CourseContentResponse response = contentService.addContent(courseId, request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/courses/{courseId}/content")
    @Operation(summary = "Get all learning content for a course (Enrolled students, Course Instructor, or Admin)")
    public ResponseEntity<List<CourseContentResponse>> getContentsForCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<CourseContentResponse> response = contentService.getContentsForCourse(courseId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/content/{contentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update learning content (Course Instructor or Admin)")
    public ResponseEntity<CourseContentResponse> updateContent(
            @PathVariable Long contentId,
            @Valid @RequestBody CourseContentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        CourseContentResponse response = contentService.updateContent(contentId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/content/{contentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete learning content (Course Instructor or Admin)")
    public ResponseEntity<Void> deleteContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        contentService.deleteContent(contentId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
