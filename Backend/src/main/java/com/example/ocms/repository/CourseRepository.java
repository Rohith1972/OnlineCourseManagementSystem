package com.example.ocms.repository;

import com.example.ocms.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByPublishedTrue(Pageable pageable);
    Page<Course> findByPublishedTrueAndCategoryIgnoreCase(String category, Pageable pageable);
    Page<Course> findByCategoryIgnoreCase(String category, Pageable pageable);
    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);
    Page<Course> findByInstructorIdAndCategoryIgnoreCase(Long instructorId, String category, Pageable pageable);
    Page<Course> findByInstructorIdAndPublishedTrue(Long instructorId, Pageable pageable);
    Page<Course> findByInstructorIdAndPublishedTrueAndCategoryIgnoreCase(Long instructorId, String category, Pageable pageable);
}
