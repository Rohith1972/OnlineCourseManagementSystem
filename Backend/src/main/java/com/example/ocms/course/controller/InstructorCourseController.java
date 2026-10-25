package com.example.ocms.course.controller;

import com.example.ocms.course.dto.CourseResponse;
import com.example.ocms.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/instructors")
@Tag(name = "Instructor Courses", description = "Endpoints for viewing courses published by instructors")
public class InstructorCourseController {

    private final CourseService courseService;

    public InstructorCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping({"/{instructorId}/courses", "/{instructorId}/courses/published"})
    @Operation(summary = "Get all courses published by a specific instructor")
    public ResponseEntity<Page<CourseResponse>> getPublishedCoursesByInstructor(
            @PathVariable Long instructorId,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CourseResponse> response = courseService.getPublishedCoursesByInstructor(instructorId, category, pageable);
        return ResponseEntity.ok(response);
    }
}
