package com.department.demo.department_portal.Service;

import com.department.demo.department_portal.Model.Student;
import com.department.demo.department_portal.Repository.StudentRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }


    // =========================================================
    // STUDENT LOGIN
    // =========================================================

    public Optional<Student> login(
            String registerNo,
            LocalDate dateOfBirth) {

        return studentRepository
                .findByRegisterNoAndDateOfBirth(
                        registerNo,
                        dateOfBirth
                );
    }


    // =========================================================
    // GET STUDENTS BY DEPARTMENT
    // =========================================================

    public List<Student> getStudentsByDepartment(
            String department) {

        return studentRepository
                .findByDepartment(department);
    }


    // =========================================================
    // GET STUDENT BY ID
    // =========================================================

    public Optional<Student> getStudentById(
            Long id) {

        return studentRepository.findById(id);
    }


    // =========================================================
    // UPDATE STUDENT PROFILE
    // =========================================================

    public Student updateStudentProfile(
            Long id,
            Map<String, String> data) {

        Optional<Student> optionalStudent =
                studentRepository.findById(id);

        if (optionalStudent.isEmpty()) {
            throw new RuntimeException(
                    "Student not found with id: " + id
            );
        }

        Student student =
                optionalStudent.get();


        // Register Number
        if (data.containsKey("registerNo")) {

            student.setRegisterNo(
                    data.get("registerNo")
            );
        }


        // Name
        if (data.containsKey("name")) {

            student.setName(
                    data.get("name")
            );
        }


        // Date of Birth
        if (data.containsKey("dateOfBirth")
                && data.get("dateOfBirth") != null
                && !data.get("dateOfBirth").isBlank()) {

            student.setDateOfBirth(
                    LocalDate.parse(
                            data.get("dateOfBirth")
                    )
            );
        }


        // Department
        if (data.containsKey("department")) {

            student.setDepartment(
                    data.get("department")
            );
        }


        // Year
        if (data.containsKey("year")) {

            student.setYear(
                    data.get("year")
            );
        }


        // Section
        if (data.containsKey("section")) {

            student.setSection(
                    data.get("section")
            );
        }


        // Achievements
        if (data.containsKey("achievements")) {

            student.setAchievements(
                    data.get("achievements")
            );
        }


        // Activities
        if (data.containsKey("activities")) {

            student.setActivities(
                    data.get("activities")
            );
        }


        // Certificates
        if (data.containsKey("certificates")) {

            student.setCertificates(
                    data.get("certificates")
            );
        }


        // Internship
        if (data.containsKey("internship")) {

            student.setInternship(
                    data.get("internship")
            );
        }


        return studentRepository.save(student);
    }


    // =========================================================
    // DELETE STUDENT
    // =========================================================

    public void deleteStudent(Long id) {

        studentRepository.deleteById(id);
    }


    // =========================================================
    // TODAY'S BIRTHDAYS
    // =========================================================

    public List<Student> getTodaysBirthdays() {

        return studentRepository
                .findTodaysBirthdays();
    }
}