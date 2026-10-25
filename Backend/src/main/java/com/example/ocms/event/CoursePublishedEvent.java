package com.example.ocms.event;

import com.example.ocms.entity.Course;

public class CoursePublishedEvent {
    private final Course course;

    public CoursePublishedEvent(Course course) {
        this.course = course;
    }

    public Course getCourse() {
        return course;
    }
}
