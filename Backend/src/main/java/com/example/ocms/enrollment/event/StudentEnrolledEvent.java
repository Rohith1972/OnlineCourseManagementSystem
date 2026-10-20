package com.example.ocms.enrollment.event;

import com.example.ocms.course.entity.Course;
import com.example.ocms.user.entity.User;

public class StudentEnrolledEvent {
    private final User student;
    private final Course course;

    public StudentEnrolledEvent(User student, Course course) {
        this.student = student;
        this.course = course;
    }

    public User getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }
}
