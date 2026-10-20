package com.example.ocms.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CourseUpdateRequest {

    @NotBlank(message = "Course title is required")
    @Size(min = 3, max = 200, message = "Course title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Course description is required")
    @Size(min = 10, max = 5000, message = "Course description must be between 10 and 5000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 100, message = "Category must be between 2 and 100 characters")
    private String category;

    public CourseUpdateRequest() {
    }

    public CourseUpdateRequest(String title, String description, String category) {
        this.title = title;
        this.description = description;
        this.category = category;
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
}
