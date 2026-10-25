package com.example.ocms.dto;

import com.example.ocms.entity.ContentType;
import com.example.ocms.entity.CourseContent;

import java.time.LocalDateTime;

public class CourseContentResponse {
    private Long id;
    private Long courseId;
    private String title;
    private ContentType contentType;
    private String resourceUrl;
    private Integer position;
    private LocalDateTime createdAt;

    public CourseContentResponse() {
    }

    public CourseContentResponse(Long id, Long courseId, String title, ContentType contentType,
                                 String resourceUrl, Integer position, LocalDateTime createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.title = title;
        this.contentType = contentType;
        this.resourceUrl = resourceUrl;
        this.position = position;
        this.createdAt = createdAt;
    }

    public static CourseContentResponse fromEntity(CourseContent content) {
        return new CourseContentResponse(
                content.getId(),
                content.getCourse() != null ? content.getCourse().getId() : null,
                content.getTitle(),
                content.getContentType(),
                content.getResourceUrl(),
                content.getPosition(),
                content.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
