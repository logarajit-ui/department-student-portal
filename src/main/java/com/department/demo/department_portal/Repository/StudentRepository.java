package com.department.demo.department_portal.Repository;

import com.department.demo.department_portal.Model.Student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    // =========================================================
    // STUDENT LOGIN
    // =========================================================

    Optional<Student> findByRegisterNoAndDateOfBirth(
            String registerNo,
            LocalDate dateOfBirth
    );


    // =========================================================
    // GET STUDENTS BY DEPARTMENT
    // =========================================================

    List<Student> findByDepartment(
            String department
    );


    // =========================================================
    // TODAY'S BIRTHDAYS
    // =========================================================

    @Query("""
        SELECT s
        FROM Student s
        WHERE MONTH(s.dateOfBirth) = MONTH(CURRENT_DATE)
        AND DAY(s.dateOfBirth) = DAY(CURRENT_DATE)
        """)
    List<Student> findTodaysBirthdays();
}