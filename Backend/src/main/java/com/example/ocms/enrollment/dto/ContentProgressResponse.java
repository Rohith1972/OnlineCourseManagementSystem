package com.example.ocms.enrollment.dto;

import com.example.ocms.enrollment.entity.ContentProgress;

import java.time.LocalDateTime;

public class ContentProgressResponse {
    private Long id;
    private Long enrollmentId;
    private Long contentId;
    private String contentTitle;
    private boolean completed;
    private LocalDateTime completedAt;
    private Double currentCourseProgressPercentage;
    private String enrollmentStatus;

    public ContentProgressResponse() {
    }

    public ContentProgressResponse(Long id, Long enrollmentId, Long contentId, String contentTitle,
                                   boolean completed, LocalDateTime completedAt,
                                   Double currentCourseProgressPercentage, String enrollmentStatus) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.contentId = contentId;
        this.contentTitle = contentTitle;
        this.completed = completed;
        this.completedAt = completedAt;
        this.currentCourseProgressPercentage = currentCourseProgressPercentage;
        this.enrollmentStatus = enrollmentStatus;
    }

    public static ContentProgressResponse fromEntity(ContentProgress progress) {
        return new ContentProgressResponse(
                progress.getId(),
                progress.getEnrollment() != null ? progress.getEnrollment().getId() : null,
                progress.getContent() != null ? progress.getContent().getId() : null,
                progress.getContent() != null ? progress.getContent().getTitle() : null,
                progress.isCompleted(),
                progress.getCompletedAt(),
                progress.getEnrollment() != null ? progress.getEnrollment().getProgressPercentage() : 0.0,
                progress.getEnrollment() != null ? progress.getEnrollment().getStatus().name() : null
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Double getCurrentCourseProgressPercentage() {
        return currentCourseProgressPercentage;
    }

    public void setCurrentCourseProgressPercentage(Double currentCourseProgressPercentage) {
        this.currentCourseProgressPercentage = currentCourseProgressPercentage;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }
}
