package com.example.ocms.enrollment.repository;

import com.example.ocms.enrollment.entity.ContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentProgressRepository extends JpaRepository<ContentProgress, Long> {
    Optional<ContentProgress> findByEnrollmentIdAndContentId(Long enrollmentId, Long contentId);
    long countByEnrollmentIdAndCompletedTrue(Long enrollmentId);
    List<ContentProgress> findByEnrollmentId(Long enrollmentId);
}
