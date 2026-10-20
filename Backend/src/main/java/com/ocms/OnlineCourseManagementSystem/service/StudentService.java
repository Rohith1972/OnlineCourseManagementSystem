package com.ocms.OnlineCourseManagementSystem.service;


import com.ocms.OnlineCourseManagementSystem.entity.Student;
import com.ocms.OnlineCourseManagementSystem.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentService {


    StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student save(Student student){
        return studentRepository.save(student);
    }

}
