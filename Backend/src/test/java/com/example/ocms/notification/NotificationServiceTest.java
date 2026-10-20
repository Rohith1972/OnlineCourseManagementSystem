package com.example.ocms.notification;

import com.example.ocms.course.entity.Course;
import com.example.ocms.enrollment.entity.Enrollment;
import com.example.ocms.enrollment.event.StudentEnrolledEvent;
import com.example.ocms.enrollment.repository.EnrollmentRepository;
import com.example.ocms.exception.ForbiddenException;
import com.example.ocms.notification.dto.NotificationResponse;
import com.example.ocms.notification.entity.Notification;
import com.example.ocms.notification.repository.NotificationRepository;
import com.example.ocms.notification.service.NotificationService;
import com.example.ocms.security.UserPrincipal;
import com.example.ocms.user.entity.Role;
import com.example.ocms.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User recipient;
    private User otherUser;
    private UserPrincipal recipientPrincipal;
    private UserPrincipal otherUserPrincipal;
    private Notification notification;

    @BeforeEach
    void setUp() {
        recipient = new User("Recipient", "rec@example.com", "pass", "123", Role.STUDENT);
        recipient.setId(10L);
        recipientPrincipal = UserPrincipal.create(recipient);

        otherUser = new User("Other", "other@example.com", "pass", "456", Role.STUDENT);
        otherUser.setId(20L);
        otherUserPrincipal = UserPrincipal.create(otherUser);

        notification = new Notification(recipient, "Welcome", "Welcome to OCMS");
        notification.setId(100L);
    }

    @Test
    @DisplayName("Should successfully mark notification as read for its recipient")
    void shouldMarkNotificationAsRead() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(100L, recipientPrincipal);

        assertNotNull(response);
        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    @DisplayName("Should forbid user from reading another user's notification")
    void shouldForbidReadingAnotherUserNotification() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        assertThrows(ForbiddenException.class, () -> notificationService.markAsRead(100L, otherUserPrincipal));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should generate notification for instructor upon student enrollment event")
    void shouldGenerateNotificationOnEnrollmentEvent() {
        User instructor = new User("Alice", "alice@example.com", "pass", "123", Role.INSTRUCTOR);
        instructor.setId(50L);
        Course course = new Course("Java", "Desc", "Prog", instructor);

        StudentEnrolledEvent event = new StudentEnrolledEvent(recipient, course);
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification(instructor, "Title", "Msg"));

        notificationService.onStudentEnrolled(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
