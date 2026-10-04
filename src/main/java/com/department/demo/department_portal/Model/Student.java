package com.department.demo.department_portal.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "register_no", nullable = false, unique = true)
    private String registerNo;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "department")
    private String department;

    @Column(name = "year")
    private String year;

    @Column(name = "section")
    private String section;

    // =====================================================
    // STUDENT DETAILS
    // =====================================================

    @Column(name = "achievements", columnDefinition = "LONGTEXT")
    private String achievements;

    @Column(name = "activities", columnDefinition = "LONGTEXT")
    private String activities;

    @Column(name = "certificates", columnDefinition = "LONGTEXT")
    private String certificates;

    @Column(name = "internship", columnDefinition = "LONGTEXT")
    private String internship;

    // =====================================================
    // PHOTO GALLERY
    // Base64 / image information
    // =====================================================

    @Column(name = "photo_gallery", columnDefinition = "LONGTEXT")
    private String photoGallery;

    // =====================================================
    // CERTIFICATE FILES
    // PDF / IMAGE Base64 information
    // =====================================================

    @Column(name = "certificate_files", columnDefinition = "LONGTEXT")
    private String certificateFiles;

    // =====================================================
    // LEAVE MANAGEMENT
    // =====================================================

    @Column(name = "leave_management", columnDefinition = "LONGTEXT")
    private String leaveManagement;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public Student() {
    }


    // =====================================================
    // ID
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // =====================================================
    // REGISTER NUMBER
    // =====================================================

    public String getRegisterNo() {
        return registerNo;
    }

    public void setRegisterNo(String registerNo) {
        this.registerNo = registerNo;
    }


    // =====================================================
    // NAME
    // =====================================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // =====================================================
    // DATE OF BIRTH
    // =====================================================

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    // =====================================================
    // DEPARTMENT
    // =====================================================

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    // =====================================================
    // YEAR
    // =====================================================

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }


    // =====================================================
    // SECTION
    // =====================================================

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }


    // =====================================================
    // ACHIEVEMENTS
    // =====================================================

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }


    // =====================================================
    // ACTIVITIES
    // =====================================================

    public String getActivities() {
        return activities;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }


    // =====================================================
    // CERTIFICATES
    // =====================================================

    public String getCertificates() {
        return certificates;
    }

    public void setCertificates(String certificates) {
        this.certificates = certificates;
    }


    // =====================================================
    // INTERNSHIP
    // =====================================================

    public String getInternship() {
        return internship;
    }

    public void setInternship(String internship) {
        this.internship = internship;
    }


    // =====================================================
    // PHOTO GALLERY
    // =====================================================

    public String getPhotoGallery() {
        return photoGallery;
    }

    public void setPhotoGallery(String photoGallery) {
        this.photoGallery = photoGallery;
    }


    // =====================================================
    // CERTIFICATE FILES
    // =====================================================

    public String getCertificateFiles() {
        return certificateFiles;
    }

    public void setCertificateFiles(String certificateFiles) {
        this.certificateFiles = certificateFiles;
    }


    // =====================================================
    // LEAVE MANAGEMENT
    // =====================================================

    public String getLeaveManagement() {
        return leaveManagement;
    }

    public void setLeaveManagement(String leaveManagement) {
        this.leaveManagement = leaveManagement;
    }
}