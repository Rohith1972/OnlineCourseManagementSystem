package com.example.ocms.dto;

public class CoursePublishRequest {
    private Boolean published;

    public CoursePublishRequest() {
    }

    public CoursePublishRequest(Boolean published) {
        this.published = published;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }
}
