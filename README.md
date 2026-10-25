# Online Course Management System (OCMS) — Spring Boot Backend

A production-ready, clean, and beginner-friendly RESTful backend API for an **Online Course Management System (OCMS)** built with **Java 21**, **Spring Boot 3.3.5**, **Spring Security with JWT**, **Spring Data JPA / Hibernate**, and **MySQL 8**.

---

## 1. Technology Stack

- **Java Version:** 21 (LTS)
- **Framework:** Spring Boot 3.3.5
- **Build Tool:** Apache Maven
- **Persistence:** Spring Data JPA, Hibernate ORM
- **Database:** MySQL 8 (Production/Local), H2 (In-memory for automated tests)
- **Security:** Spring Security 6 with stateless JWT authentication & BCrypt password hashing
- **Validation:** Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`, etc.)
- **Monitoring & Metrics:** Spring Boot Actuator
- **API Documentation & Testing:** Springdoc OpenAPI 3 / Swagger UI (`v2.6.0`)
- **Testing:** JUnit 5, Mockito, Spring Boot Test (`MockMvc`)

---

## 2. Layered Package Architecture

The project strictly follows a clean, feature-driven layered architecture:

```text
com.example.ocms
├── OcmsApplication.java           # Spring Boot application entry point
├── config                         # Configuration (OpenAPI, DataInitializer)
├── security                       # JWT utils, filters, user details, SecurityConfig
├── exception                      # Global exception handler, error responses
├── auth                           # Authentication (Student/Instructor registration, login)
│   ├── controller
│   ├── dto
│   └── service
├── user                           # User entity, profile viewing and editing, admin user management
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── course                         # Course entity, CRUD, ownership, publishing, filtering
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── event
│   ├── repository
│   └── service
├── content                        # Course learning materials (Video, PDF, Quiz metadata)
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── enrollment                     # Enrollment, ContentProgress, completion tracking
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── event
│   ├── repository
│   └── service
└── notification                   # In-app notifications & decoupled event triggers
    ├── controller
    ├── dto
    ├── entity
    ├── repository
    └── service
```

---

## 3. Database Schema

The system uses 6 core relational tables in MySQL (`ocms_db`):

1. **`users`**: User records with roles (`STUDENT`, `INSTRUCTOR`, `ADMIN`), unique email index, hashed passwords, timestamps.
2. **`courses`**: Course records with `instructor_id` foreign key, title, description, category, `published` status, timestamps.
3. **`course_contents`**: Course learning materials with `course_id` foreign key, `content_type` (`VIDEO`, `PDF`, `QUIZ`), resource URL, position order.
4. **`enrollments`**: Student enrollments with composite unique constraint `(student_id, course_id)`, enrollment date, status (`ONGOING`, `COMPLETED`), progress percentage, completion date.
5. **`content_progress`**: Granular progress records per student and content item with composite unique constraint `(enrollment_id, content_id)`, completed flag, completedAt timestamp.
6. **`notifications`**: In-app notifications for users with `recipient_id` foreign key, title, message, `is_read` flag, createdAt timestamp.

---

## 4. Getting Started

### Prerequisites

- Java 21 JDK installed (`java -version`)
- MySQL 8+ Server running locally on port `3306`

### Database Configuration

Create the MySQL database if it doesn't already exist:

```sql
CREATE DATABASE IF NOT EXISTS ocms_db;
```

Update your database credentials in `Backend/src/main/resources/application.properties` or provide environment variables:

| Environment Variable | Description | Default |
|---|---|---|
| `SERVER_PORT` | HTTP Server Port | `8080` |
| `DB_URL` | JDBC MySQL connection URL | `jdbc:mysql://localhost:3306/ocms_db?...` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | `rohith` |
| `JWT_SECRET` | 256-bit secret key for signing tokens | (Configured default in properties) |
| `JWT_EXPIRATION_MS` | JWT expiration duration in ms | `86400000` (24 hours) |
| `ADMIN_EMAIL` | Default administrator email | `admin@ocms.com` |
| `ADMIN_PASSWORD` | Default administrator password | `Admin@123456` |

### Building and Running the Application

Navigate to the `Backend` directory:

```bash
cd Backend
./mvnw clean spring-boot:run
```

Or on Windows PowerShell:

```powershell
cd Backend
.\mvnw.cmd clean spring-boot:run
```

### Running Tests

Execute the automated test suite (runs with isolated H2 in-memory DB):

```powershell
cd Backend
.\mvnw.cmd clean test
```

### Interactive Swagger UI / OpenAPI Documentation

Once the backend is running, open your browser:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI v3 JSON:** `http://localhost:8080/v3/api-docs`
- **Actuator Health:** `http://localhost:8080/actuator/health`

---

## 5. Seeded Administrator Account

On initial startup, `DataInitializer` automatically ensures a default administrator account exists:

- **Email:** `admin@ocms.com`
- **Password:** `Admin@123456`
- **Role:** `ADMIN`

---

## 6. Detailed API Endpoints Reference

### 6.1 Authentication (`/api/v1/auth`)

#### 1. Register Student
- **Endpoint:** `POST /api/v1/auth/register/student`
- **Auth:** Public
- **Request Body:**
```json
{
  "fullName": "Rohith Kumar",
  "email": "rohith.student@example.com",
  "password": "Password@123",
  "phoneNumber": "+919876543210"
}
```
- **Response:** `201 Created`
```json
{
  "id": 2,
  "fullName": "Rohith Kumar",
  "email": "rohith.student@example.com",
  "phoneNumber": "+919876543210",
  "role": "STUDENT",
  "createdAt": "2026-10-20T22:00:00",
  "updatedAt": "2026-10-20T22:00:00"
}
```

#### 2. Register Instructor
- **Endpoint:** `POST /api/v1/auth/register/instructor`
- **Auth:** Public
- **Request Body:**
```json
{
  "fullName": "Dr. Sarah Connor",
  "email": "sarah.instructor@example.com",
  "password": "Password@123",
  "phoneNumber": "+919876543211"
}
```
- **Response:** `201 Created` (Role is automatically set to `INSTRUCTOR`)

#### 3. User Login
- **Endpoint:** `POST /api/v1/auth/login`
- **Auth:** Public
- **Request Body:**
```json
{
  "email": "rohith.student@example.com",
  "password": "Password@123"
}
```
- **Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb2hpdGguc3R1ZGVudEBleGFtcGxlLmNvbSIs...",
  "tokenType": "Bearer",
  "user": {
    "id": 2,
    "fullName": "Rohith Kumar",
    "email": "rohith.student@example.com",
    "phoneNumber": "+919876543210",
    "role": "STUDENT",
    "createdAt": "2026-10-20T22:00:00",
    "updatedAt": "2026-10-20T22:00:00"
  }
}
```

#### 4. Get Current Authenticated User
- **Endpoint:** `GET /api/v1/auth/me`
- **Auth:** Bearer Token
- **Response:** `200 OK`

---

### 6.2 User & Profile Management (`/api/v1/users`)

#### 1. Get My Profile
- **Endpoint:** `GET /api/v1/users/profile`
- **Auth:** Bearer Token (`STUDENT`, `INSTRUCTOR`, `ADMIN`)
- **Response:** `200 OK`

#### 2. Update My Profile
- **Endpoint:** `PUT /api/v1/users/profile`
- **Auth:** Bearer Token (`STUDENT`, `INSTRUCTOR`, `ADMIN`)
- **Request Body:**
```json
{
  "fullName": "Rohith Kumar Updated",
  "phoneNumber": "+919999888877"
}
```
- **Response:** `200 OK` *(Note: Role, email, and password cannot be modified through this endpoint)*

#### 3. List All Users (Admin Only)
- **Endpoint:** `GET /api/v1/users?role=STUDENT&page=0&size=10`
- **Auth:** Bearer Token (`ADMIN`)
- **Response:** `200 OK` (Paginated list of users)

#### 4. Get User By ID
- **Endpoint:** `GET /api/v1/users/{userId}`
- **Auth:** Bearer Token (`ADMIN` or Self)
- **Response:** `200 OK`

---

### 6.3 Course Management (`/api/v1/courses`)

#### 1. Create Course
- **Endpoint:** `POST /api/v1/courses`
- **Auth:** Bearer Token (`INSTRUCTOR`, `ADMIN`)
- **Request Body:**
```json
{
  "title": "Mastering Spring Boot 3 & Java 21",
  "description": "Comprehensive course covering modern Spring Boot 3, JPA, Security, and REST APIs.",
  "category": "Computer Science"
}
```
- **Response:** `201 Created`

#### 2. Browse Courses
- **Endpoint:** `GET /api/v1/courses?category=Computer Science&instructorId=2&page=0&size=10`
- **Auth:** Public / Optional Token
- **Rules:**
  - Unauthenticated visitors and Students receive **only published courses**.
  - Instructors can view published courses or their own courses.
  - Administrators can view all courses across the platform.
  - Optional `instructorId` parameter filters published courses by a specific instructor.
- **Response:** `200 OK` (Paginated list of courses)

#### 3. Get All Published Courses by an Instructor
- **Endpoint:** `GET /api/v1/instructors/{instructorId}/courses?category=Computer Science&page=0&size=10`
- **Alternative:** `GET /api/v1/courses/instructor/{instructorId}?page=0&size=10`
- **Auth:** Public
- **Rules:**
  - Validates that `instructorId` exists and has `INSTRUCTOR` role.
  - Returns only published courses created by that instructor.
  - Supports optional `category` filter and pagination.
- **Response:** `200 OK` (Paginated list of published courses)

#### 4. Get Course Details
- **Endpoint:** `GET /api/v1/courses/{courseId}`
- **Auth:** Public for published courses; Owner Instructor / Admin for unpublished courses
- **Response:** `200 OK`

#### 5. Update Course Details
- **Endpoint:** `PUT /api/v1/courses/{courseId}`
- **Auth:** Bearer Token (`Course Instructor Owner` or `ADMIN`)
- **Request Body:**
```json
{
  "title": "Mastering Spring Boot 3 & Java 21 - Edition 2026",
  "description": "Updated syllabus with microservice architecture best practices.",
  "category": "Computer Science"
}
```
- **Response:** `200 OK`

#### 6. Publish or Unpublish Course
- **Endpoint:** `PATCH /api/v1/courses/{courseId}/publish`
- **Auth:** Bearer Token (`Course Instructor Owner` or `ADMIN`)
- **Request Body (Optional):**
```json
{
  "published": true
}
```
- **Response:** `200 OK` *(Triggers an in-app notification to all enrolled students)*

#### 7. Delete Course
- **Endpoint:** `DELETE /api/v1/courses/{courseId}`
- **Auth:** Bearer Token (`Course Instructor Owner` or `ADMIN`)
- **Rules:** Enforces safe deletion. If student enrollment history exists, returns `400 Bad Request`.
- **Response:** `204 No Content`

---

### 6.4 Course Learning Content (`/api/v1`)

#### 1. Add Content to Course
- **Endpoint:** `POST /api/v1/courses/{courseId}/content`
- **Auth:** Bearer Token (`Course Instructor Owner` or `ADMIN`)
- **Request Body:**
```json
{
  "title": "01. Introduction to Spring Boot 3",
  "contentType": "VIDEO",
  "resourceUrl": "https://example.com/videos/intro-spring-boot-3.mp4",
  "position": 1
}
```
*(Content types supported: `VIDEO`, `PDF`, `QUIZ`)*
- **Response:** `201 Created`

#### 2. Get Learning Content for Course
- **Endpoint:** `GET /api/v1/courses/{courseId}/content`
- **Auth:** Bearer Token (`Enrolled Students`, `Course Instructor Owner`, or `ADMIN`)
- **Response:** `200 OK` (List of content items sorted by position ascending)

#### 3. Update Learning Content
- **Endpoint:** `PUT /api/v1/content/{contentId}`
- **Auth:** Bearer Token (`Course Instructor Owner` or `ADMIN`)
- **Request Body:**
```json
{
  "title": "01. Introduction to Spring Boot 3 (Updated Audio)",
  "contentType": "VIDEO",
  "resourceUrl": "https://example.com/videos/intro-spring-boot-3-v2.mp4",
  "position": 1
}
```
- **Response:** `200 OK`

#### 4. Delete Learning Content
- **Endpoint:** `DELETE /api/v1/content/{contentId}`
- **Auth:** Bearer Token (`Course Instructor Owner` or `ADMIN`)
- **Response:** `204 No Content`

---

### 6.5 Enrollment & Progress Tracking (`/api/v1`)

#### 1. Enroll in a Course
- **Endpoint:** `POST /api/v1/courses/{courseId}/enroll`
- **Auth:** Bearer Token (`STUDENT`, `ADMIN`)
- **Rules:**
  - Course must be published (`published = true`).
  - Prevents duplicate enrollments (`409 Conflict`).
  - Automatically notifies the course instructor upon enrollment.
- **Response:** `201 Created`
```json
{
  "id": 1,
  "student": {
    "id": 2,
    "fullName": "Rohith Kumar",
    "email": "rohith.student@example.com"
  },
  "course": {
    "id": 1,
    "title": "Mastering Spring Boot 3 & Java 21"
  },
  "enrollmentDate": "2026-10-20T22:30:00",
  "status": "ONGOING",
  "progressPercentage": 0.0,
  "completionDate": null
}
```

#### 2. Get My Ongoing Enrollments
- **Endpoint:** `GET /api/v1/enrollments/my?page=0&size=10`
- **Auth:** Bearer Token (`STUDENT`, `ADMIN`)
- **Response:** `200 OK` (Paginated list of active ongoing courses)

#### 3. Get My Full Enrollment History
- **Endpoint:** `GET /api/v1/enrollments/my/history?page=0&size=10`
- **Auth:** Bearer Token (`STUDENT`, `ADMIN`)
- **Response:** `200 OK` (Paginated list of all enrollments)

#### 4. Get Enrolled Students for a Course
- **Endpoint:** `GET /api/v1/courses/{courseId}/students?page=0&size=10`
- **Auth:** Bearer Token (`Course Instructor Owner` or `ADMIN`)
- **Response:** `200 OK` (Paginated list of enrolled students)

#### 5. Get Enrolled Students for a Specific Course by a Specific Instructor
- **Endpoint:** `GET /api/v1/instructors/{instructorId}/courses/{courseId}/students?page=0&size=10`
- **Auth:** Bearer Token (`Specific Course Instructor` or `ADMIN`)
- **Rules:**
  - Validates that `instructorId` exists and has `INSTRUCTOR` role.
  - Validates that `courseId` belongs to the specified instructor.
  - Returns paginated list of registered students along with `totalElements` (total registered students).
- **Response:** `200 OK` (Paginated list of enrolled students)

#### 6. Mark Learning Item Completed / Incomplete
- **Endpoint:** `PATCH /api/v1/courses/{courseId}/content/{contentId}/progress`
- **Auth:** Bearer Token (`STUDENT`, `ADMIN`)
- **Request Body (Optional):**
```json
{
  "completed": true
}
```
- **Rules:**
  - Progress % is calculated dynamically: `(completed items / total course items) * 100`.
  - When 100% is reached, enrollment status updates to `COMPLETED` and `completionDate` is set.
  - If a completed item is marked `false`, status reverts back to `ONGOING` and `completionDate` becomes `null`.
- **Response:** `200 OK`
```json
{
  "id": 1,
  "enrollmentId": 1,
  "contentId": 1,
  "contentTitle": "01. Introduction to Spring Boot 3",
  "completed": true,
  "completedAt": "2026-10-20T22:35:00",
  "currentCourseProgressPercentage": 50.0,
  "enrollmentStatus": "ONGOING"
}
```

---

### 6.6 In-App Notifications (`/api/v1/notifications`)

#### 1. Get My Notifications
- **Endpoint:** `GET /api/v1/notifications/my?page=0&size=10`
- **Auth:** Bearer Token (`STUDENT`, `INSTRUCTOR`, `ADMIN`)
- **Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "New Student Enrollment",
      "message": "Student Rohith Kumar has enrolled in your course 'Mastering Spring Boot 3 & Java 21'.",
      "read": false,
      "createdAt": "2026-10-20T22:30:00"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

#### 2. Mark Notification as Read
- **Endpoint:** `PATCH /api/v1/notifications/{notificationId}/read`
- **Auth:** Bearer Token (Must belong to recipient; returns `403 Forbidden` if accessed by another user)
- **Response:** `200 OK`

---

## 7. Production Deployment & Database Migration Strategy

For local development, `spring.jpa.hibernate.ddl-auto=update` is utilized. For production environments, it is recommended to:
1. Set `spring.jpa.hibernate.ddl-auto=validate` in production configuration.
2. Adopt a database versioning tool such as **Flyway** (`flyway-core`, `flyway-mysql`) or **Liquibase**.
3. Manage migration scripts versioned under `src/main/resources/db/migration/V1__initial_schema.sql` to ensure deterministic, auditable database evolution across staging and production clusters.
