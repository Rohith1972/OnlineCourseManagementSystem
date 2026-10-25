package com.example.ocms.event;

import com.example.ocms.entity.Course;

public class CourseUpdatedEvent {
    private final Course course;

    public CourseUpdatedEvent(Course course) {
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }
}
