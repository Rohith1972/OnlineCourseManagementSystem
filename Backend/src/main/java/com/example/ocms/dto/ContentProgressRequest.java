package com.example.ocms.dto;

public class ContentProgressRequest {
    private Boolean completed;

    public ContentProgressRequest() {
    }

    public ContentProgressRequest(Boolean completed) {
        this.completed = completed;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
