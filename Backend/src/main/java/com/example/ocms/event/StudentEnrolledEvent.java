package com.example.ocms.event;

import com.example.ocms.entity.Course;
import com.example.ocms.entity.User;

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
