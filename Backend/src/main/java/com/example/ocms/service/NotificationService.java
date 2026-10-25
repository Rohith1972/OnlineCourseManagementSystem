package com.example.ocms.service;

import com.example.ocms.entity.Course;
import com.example.ocms.event.CoursePublishedEvent;
import com.example.ocms.event.CourseUpdatedEvent;
import com.example.ocms.entity.Enrollment;
import com.example.ocms.event.StudentEnrolledEvent;
import com.example.ocms.repository.EnrollmentRepository;
import com.example.ocms.exception.ForbiddenException;
import com.example.ocms.exception.ResourceNotFoundException;
import com.example.ocms.dto.NotificationResponse;
import com.example.ocms.entity.Notification;
import com.example.ocms.repository.NotificationRepository;
import com.example.ocms.security.UserPrincipal;
import com.example.ocms.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EnrollmentRepository enrollmentRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               EnrollmentRepository enrollmentRepository) {
        this.notificationRepository = notificationRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public Notification createNotification(User recipient, String title, String message) {
        Notification notification = new Notification(recipient, title, message);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UserPrincipal currentUser, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not have permission to access another user's notifications");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(updated);
    }

    @EventListener
    @Transactional
    public void onStudentEnrolled(StudentEnrolledEvent event) {
        Course course = event.getCourse();
        User instructor = course.getInstructor();
        User student = event.getStudent();

        String title = "New Student Enrollment";
        String message = String.format("Student %s has enrolled in your course '%s'.",
                student.getFullName(), course.getTitle());

        createNotification(instructor, title, message);
        logger.info("Sent enrollment notification to instructor: {}", instructor.getEmail());
    }

    @EventListener
    @Transactional
    public void onCoursePublished(CoursePublishedEvent event) {
        Course course = event.getCourse();
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());

        String title = "Course Published: " + course.getTitle();
        String message = String.format("The course '%s' has now been published and is active.", course.getTitle());

        for (Enrollment enrollment : enrollments) {
            createNotification(enrollment.getStudent(), title, message);
        }
        logger.info("Sent course published notification to {} enrolled students", enrollments.size());
    }

    @EventListener
    @Transactional
    public void onCourseUpdated(CourseUpdatedEvent event) {
        Course course = event.getCourse();
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());

        String title = "Course Updated: " + course.getTitle();
        String message = String.format("The course '%s' has been updated by the instructor.", course.getTitle());

        for (Enrollment enrollment : enrollments) {
            createNotification(enrollment.getStudent(), title, message);
        }
        logger.info("Sent course updated notification to {} enrolled students", enrollments.size());
    }
}
