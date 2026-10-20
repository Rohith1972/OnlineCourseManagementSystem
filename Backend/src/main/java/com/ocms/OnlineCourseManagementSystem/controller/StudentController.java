package com.ocms.OnlineCourseManagementSystem.controller;


import com.ocms.OnlineCourseManagementSystem.entity.Student;
import com.ocms.OnlineCourseManagementSystem.service.StudentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {

    StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/saveStudent")
    public Student save(@RequestBody Student student){
        return studentService.save(student);
    }

}
