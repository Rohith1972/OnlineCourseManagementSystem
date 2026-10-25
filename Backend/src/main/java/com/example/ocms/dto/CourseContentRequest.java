package com.example.ocms.dto;

import com.example.ocms.entity.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CourseContentRequest {

    @NotBlank(message = "Content title is required")
    @Size(min = 2, max = 200, message = "Content title must be between 2 and 200 characters")
    private String title;

    @NotNull(message = "Content type is required (VIDEO, PDF, or QUIZ)")
    private ContentType contentType;

    @NotBlank(message = "Resource URL is required")
    @Size(max = 500, message = "Resource URL must not exceed 500 characters")
    private String resourceUrl;

    private Integer position;

    public CourseContentRequest() {
    }

    public CourseContentRequest(String title, ContentType contentType, String resourceUrl, Integer position) {
        this.title = title;
        this.contentType = contentType;
        this.resourceUrl = resourceUrl;
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
