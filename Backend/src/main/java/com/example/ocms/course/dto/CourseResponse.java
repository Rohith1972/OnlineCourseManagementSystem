package com.example.ocms.course.dto;

import com.example.ocms.course.entity.Course;
import com.example.ocms.user.dto.UserResponse;

import java.time.LocalDateTime;

public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private UserResponse instructor;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CourseResponse() {
    }

    public CourseResponse(Long id, String title, String description, String category,
                          UserResponse instructor, boolean published,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.instructor = instructor;
        this.published = published;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CourseResponse fromEntity(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                course.getInstructor() != null ? UserResponse.fromEntity(course.getInstructor()) : null,
                course.isPublished(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UserResponse getInstructor() {
        return instructor;
    }

    public void setInstructor(UserResponse instructor) {
        this.instructor = instructor;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
