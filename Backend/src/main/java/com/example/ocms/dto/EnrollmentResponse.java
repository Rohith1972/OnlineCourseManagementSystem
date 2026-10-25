package com.example.ocms.dto;

import com.example.ocms.dto.CourseResponse;
import com.example.ocms.entity.Enrollment;
import com.example.ocms.entity.EnrollmentStatus;
import com.example.ocms.dto.UserResponse;

import java.time.LocalDateTime;

public class EnrollmentResponse {
    private Long id;
    private UserResponse student;
    private CourseResponse course;
    private LocalDateTime enrollmentDate;
    private EnrollmentStatus status;
    private Double progressPercentage;
    private LocalDateTime completionDate;

    public EnrollmentResponse() {
    }

    public EnrollmentResponse(Long id, UserResponse student, CourseResponse course,
                              LocalDateTime enrollmentDate, EnrollmentStatus status,
                              Double progressPercentage, LocalDateTime completionDate) {
        this.id = id;
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.completionDate = completionDate;
    }

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent() != null ? UserResponse.fromEntity(enrollment.getStudent()) : null,
                enrollment.getCourse() != null ? CourseResponse.fromEntity(enrollment.getCourse()) : null,
                enrollment.getEnrollmentDate(),
                enrollment.getStatus(),
                enrollment.getProgressPercentage(),
                enrollment.getCompletionDate()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserResponse getStudent() {
        return student;
    }

    public void setStudent(UserResponse student) {
        this.student = student;
    }

    public CourseResponse getCourse() {
        return course;
    }

    public void setCourse(CourseResponse course) {
        this.course = course;
    }

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }
}
