package com.example.ocms.entity;

import com.example.ocms.entity.CourseContent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uk_enrollment_content", columnNames = {"enrollment_id", "content_id"})
})
public class ContentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private CourseContent content;

    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime completedAt;

    public ContentProgress() {
    }

    public ContentProgress(Enrollment enrollment, CourseContent content, boolean completed) {
        this.enrollment = enrollment;
        this.content = content;
        this.completed = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public CourseContent getContent() {
        return content;
    }

    public void setContent(CourseContent content) {
        this.content = content;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
