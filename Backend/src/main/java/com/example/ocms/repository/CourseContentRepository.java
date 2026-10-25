package com.example.ocms.repository;

import com.example.ocms.entity.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    List<CourseContent> findByCourseIdOrderByPositionAsc(Long courseId);
    int countByCourseId(Long courseId);
}
