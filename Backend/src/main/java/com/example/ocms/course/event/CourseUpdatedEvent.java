package com.example.ocms.course.event;

import com.example.ocms.course.entity.Course;

public class CourseUpdatedEvent {
    private final Course course;

    public CourseUpdatedEvent(Course course) {
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }
}
