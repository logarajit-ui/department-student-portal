
package com.department.demo.department_portal.Controller;

import com.department.demo.department_portal.Model.Student;
import com.department.demo.department_portal.Repository.PortalJdbcRepository;
import com.department.demo.department_portal.Service.StudentService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    private final PortalJdbcRepository portalJdbcRepository;
    private final JdbcTemplate jdbcTemplate;

    public StudentController(
            StudentService studentService,
            PortalJdbcRepository portalJdbcRepository,
            JdbcTemplate jdbcTemplate) {

        this.studentService = studentService;
        this.portalJdbcRepository = portalJdbcRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================
    // TEST
    // =========================================================

    @GetMapping("/api/students/test")
    @ResponseBody
    public ResponseEntity<?> test() {

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Student API is working"
                )
        );
    }

    // =========================================================
    // STUDENT LOGIN
    // =========================================================

    @PostMapping("/api/students/login")
    @ResponseBody
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> data) {

        try {

            String registerNo = data.get("registerNo");
            String dateOfBirth = data.get("dateOfBirth");

            if (registerNo == null ||
                    registerNo.trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Register number is required"
                                )
                        );
            }

            if (dateOfBirth == null ||
                    dateOfBirth.trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Date of birth is required"
                                )
                        );
            }

            LocalDate dob;

            try {

                dob = LocalDate.parse(
                        dateOfBirth.trim()
                );

            } catch (Exception e) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid date of birth"
                                )
                        );
            }

            Optional<Student> student =
                    studentService.login(
                            registerNo.trim(),
                            dob
                    );

            if (student.isEmpty()) {

                return ResponseEntity.status(401)
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid register number or date of birth"
                                )
                        );
            }

            return ResponseEntity.ok(
                    createStudentResponse(
                            student.get()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Login failed"
                            )
                    );
        }
    }

    // =========================================================
    // ADMIN / HOD LOGIN
    // =========================================================

    @PostMapping("/api/students/role-login")
    @ResponseBody
    public ResponseEntity<?> roleLogin(
            @RequestBody Map<String, String> data) {

        try {

            String role = data.get("role");
            String collegeId = data.get("collegeId");
            String dateOfBirth = data.get("dateOfBirth");

            // ROLE CHECK
            if (role == null ||
                    role.trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Role is required"
                                )
                        );
            }

            role = role.trim().toUpperCase();

            // ONLY ADMIN / HOD
            if (!role.equals("ADMIN") &&
                    !role.equals("HOD")) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid role"
                                )
                        );
            }

            // COLLEGE ID CHECK
            if (collegeId == null ||
                    collegeId.trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "College ID is required"
                                )
                        );
            }

            // DOB CHECK
            if (dateOfBirth == null ||
                    dateOfBirth.trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Date of birth is required"
                                )
                        );
            }

            LocalDate dob;

            try {

                dob = LocalDate.parse(
                        dateOfBirth.trim()
                );

            } catch (Exception e) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid date of birth. Use YYYY-MM-DD"
                                )
                        );
            }

            // FIND ADMIN / HOD
            String sql = """
                    SELECT
                        id,
                        college_id,
                        name,
                        date_of_birth,
                        role
                    FROM users
                    WHERE college_id = ?
                    AND date_of_birth = ?
                    AND UPPER(role) = UPPER(?)
                    """;

            List<Map<String, Object>> users =
                    jdbcTemplate.queryForList(
                            sql,
                            collegeId.trim(),
                            dob,
                            role
                    );

            // LOGIN FAILED
            if (users.isEmpty()) {

                return ResponseEntity.status(401)
                        .body(
                                Map.of(
                                        "success",
                                        false,

                                        "message",
                                        "Invalid "
                                                + role
                                                + " login"
                                )
                        );
            }

            // LOGIN SUCCESS
            Map<String, Object> user =
                    users.get(0);

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "Login successful"
            );

            response.put(
                    "id",
                    user.get("id")
            );

            response.put(
                    "collegeId",
                    user.get("college_id")
            );

            response.put(
                    "name",
                    user.get("name")
            );

            response.put(
                    "role",
                    user.get("role")
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "success",
                                    false,

                                    "message",
                                    "Admin/HOD login failed: "
                                            + e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET STUDENTS BY DEPARTMENT
    // =========================================================

    @GetMapping("/api/students/department/{department}")
    @ResponseBody
    public ResponseEntity<?> getStudentsByDepartment(
            @PathVariable String department) {

        try {

            List<Student> students =
                    studentService
                            .getStudentsByDepartment(
                                    department
                            );

            List<Map<String, Object>> response =
                    students.stream()
                            .map(this::createStudentResponse)
                            .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load students"
                            )
                    );
        }
    }

    // =========================================================
    // GET STUDENT BY ID
    // =========================================================

    @GetMapping("/api/students/{id}")
    @ResponseBody
    public ResponseEntity<?> getStudentById(
            @PathVariable Long id) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(id);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            return ResponseEntity.ok(
                    createStudentResponse(
                            student.get()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load student"
                            )
                    );
        }
    }

    // =========================================================
    // UPDATE STUDENT PROFILE
    // =========================================================

    @PutMapping("/api/students/{id}/profile")
    @ResponseBody
    public ResponseEntity<?> updateStudentProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> data) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(id);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            Student updated =
                    studentService.updateStudentProfile(
                            id,
                            data
                    );

            return ResponseEntity.ok(
                    createStudentResponse(updated)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to update student profile"
                            )
                    );
        }
    }

    // =========================================================
    // PROFILE PHOTO - UPLOAD
    // =========================================================

    @PostMapping(
            value = "/api/students/{studentId}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    public ResponseEntity<?> uploadPhoto(
            @PathVariable Long studentId,
            @RequestParam("file") MultipartFile file) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            if (file == null || file.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Photo file is required"
                                )
                        );
            }

            portalJdbcRepository.savePhoto(
                    studentId,
                    file
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Photo uploaded successfully"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to upload photo"
                            )
                    );
        }
    }

    // =========================================================
    // GET PROFILE PHOTOS
    // =========================================================

    @GetMapping("/api/students/{studentId}/photos")
    @ResponseBody
    public ResponseEntity<?> getPhotos(
            @PathVariable Long studentId) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            return ResponseEntity.ok(
                    portalJdbcRepository
                            .getPhotos(studentId)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load photos"
                            )
                    );
        }
    }

    // =========================================================
    // GET PHOTO FILE
    // =========================================================

    @GetMapping("/api/photos/{id}")
    public ResponseEntity<byte[]> getPhoto(
            @PathVariable Long id) {

        Map<String, Object> photo =
                portalJdbcRepository.getPhoto(id);

        if (photo == null) {

            return ResponseEntity.notFound()
                    .build();
        }

        byte[] data =
                (byte[]) photo.get("file_data");

        String fileType =
                (String) photo.get("file_type");

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        fileType != null
                                ? fileType
                                : "image/jpeg"
                )
                .body(data);
    }

    // =========================================================
    // GALLERY - UPLOAD
    // =========================================================

    @PostMapping(
            value = "/api/students/{studentId}/gallery",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    public ResponseEntity<?> uploadGallery(
            @PathVariable Long studentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            if (file == null || file.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Gallery image is required"
                                )
                        );
            }

            portalJdbcRepository.saveGallery(
                    studentId,
                    title,
                    description,
                    file
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Gallery image uploaded successfully"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to upload gallery image"
                            )
                    );
        }
    }

    // =========================================================
    // GET STUDENT GALLERY
    // =========================================================

    @GetMapping("/api/students/{studentId}/gallery")
    @ResponseBody
    public ResponseEntity<?> getGallery(
            @PathVariable Long studentId) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            return ResponseEntity.ok(
                    portalJdbcRepository
                            .getGallery(studentId)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load gallery"
                            )
                    );
        }
    }

    // =========================================================
    // GET GALLERY FILE
    // =========================================================

    @GetMapping("/api/gallery/{id}")
    public ResponseEntity<byte[]> getGalleryFile(
            @PathVariable Long id) {

        Map<String, Object> image =
                portalJdbcRepository
                        .getGalleryFile(id);

        if (image == null) {

            return ResponseEntity.notFound()
                    .build();
        }

        byte[] data =
                (byte[]) image.get("file_data");

        String fileType =
                (String) image.get("file_type");

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        fileType != null
                                ? fileType
                                : "image/jpeg"
                )
                .body(data);
    }

    // =========================================================
    // CERTIFICATE - UPLOAD
    // =========================================================

    @PostMapping(
            value = "/api/students/{studentId}/certificates",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    public ResponseEntity<?> uploadCertificate(
            @PathVariable Long studentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            if (file == null || file.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Certificate PDF is required"
                                )
                        );
            }

            portalJdbcRepository.saveCertificate(
                    studentId,
                    title,
                    description,
                    file
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Certificate uploaded successfully"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to upload certificate"
                            )
                    );
        }
    }

    // =========================================================
    // GET CERTIFICATES
    // =========================================================

    @GetMapping("/api/students/{studentId}/certificates")
    @ResponseBody
    public ResponseEntity<?> getCertificates(
            @PathVariable Long studentId) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            return ResponseEntity.ok(
                    portalJdbcRepository
                            .getCertificates(studentId)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load certificates"
                            )
                    );
        }
    }

    // =========================================================
    // GET CERTIFICATE FILE
    // =========================================================

    @GetMapping("/api/certificates/{id}")
    public ResponseEntity<byte[]> getCertificate(
            @PathVariable Long id) {

        Map<String, Object> certificate =
                portalJdbcRepository
                        .getCertificate(id);

        if (certificate == null) {

            return ResponseEntity.notFound()
                    .build();
        }

        byte[] data =
                (byte[]) certificate.get("file_data");

        String fileType =
                (String) certificate.get("file_type");

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        fileType != null
                                ? fileType
                                : "application/pdf"
                )
                .body(data);
    }

    // =========================================================
    // LEAVE - APPLY
    // =========================================================

    @PostMapping("/api/students/{studentId}/leave")
    @ResponseBody
    public ResponseEntity<?> applyLeave(
            @PathVariable Long studentId,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String reason) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            LocalDate from =
                    LocalDate.parse(fromDate);

            LocalDate to =
                    LocalDate.parse(toDate);

            if (to.isBefore(from)) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "To date cannot be before from date"
                                )
                        );
            }

            portalJdbcRepository.saveLeave(
                    studentId,
                    from,
                    to,
                    reason
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Leave request submitted successfully"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to submit leave request"
                            )
                    );
        }
    }

    // =========================================================
    // GET LEAVE REQUESTS
    // =========================================================

    @GetMapping("/api/students/{studentId}/leaves")
    @ResponseBody
    public ResponseEntity<?> getLeaves(
            @PathVariable Long studentId) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            return ResponseEntity.ok(
                    portalJdbcRepository
                            .getLeaves(studentId)
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load leave requests"
                            )
                    );
        }
    }

    // =========================================================
    // TODAY'S BIRTHDAYS
    // =========================================================

    @GetMapping("/api/students/birthdays/today")
    @ResponseBody
    public ResponseEntity<?> getTodaysBirthdays() {

        try {

            List<Student> students =
                    studentService.getTodaysBirthdays();

            List<Map<String, Object>> response =
                    students.stream()
                            .map(student -> {

                                Map<String, Object> data =
                                        new HashMap<>();

                                data.put(
                                        "id",
                                        student.getId()
                                );

                                data.put(
                                        "registerNo",
                                        student.getRegisterNo()
                                );

                                data.put(
                                        "name",
                                        student.getName()
                                );

                                data.put(
                                        "dateOfBirth",
                                        student.getDateOfBirth()
                                );

                                data.put(
                                        "department",
                                        student.getDepartment()
                                );

                                data.put(
                                        "year",
                                        student.getYear()
                                );

                                data.put(
                                        "section",
                                        student.getSection()
                                );

                                return data;

                            })
                            .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load today's birthdays"
                            )
                    );
        }
    }

    // =========================================================
    // SEND BIRTHDAY WISH
    // =========================================================

    @PostMapping(
            "/api/students/{receiverId}/birthday-wishes"
    )
    @ResponseBody
    public ResponseEntity<?> sendBirthdayWish(
            @PathVariable Long receiverId,
            @RequestParam Long senderId,
            @RequestParam String message) {

        try {

            if (message == null ||
                    message.trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Birthday message is required"
                                )
                        );
            }

            Optional<Student> sender =
                    studentService.getStudentById(senderId);

            if (sender.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Sender student not found"
                                )
                        );
            }

            Optional<Student> receiver =
                    studentService.getStudentById(receiverId);

            if (receiver.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            if (senderId.equals(receiverId)) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "You cannot send a birthday wish to yourself"
                                )
                        );
            }

            portalJdbcRepository.saveBirthdayWish(
                    senderId,
                    receiverId,
                    message.trim()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Birthday wish sent successfully"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to send birthday wish"
                            )
                    );
        }
    }

    // =========================================================
    // GET RECEIVED BIRTHDAY WISHES
    // =========================================================

    @GetMapping(
            "/api/students/{studentId}/birthday-wishes"
    )
    @ResponseBody
    public ResponseEntity<?> getBirthdayWishes(
            @PathVariable Long studentId) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            List<Map<String, Object>> wishes =
                    portalJdbcRepository
                            .getBirthdayWishes(studentId);

            return ResponseEntity.ok(wishes);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load birthday wishes"
                            )
                    );
        }
    }

    // =========================================================
    // UNREAD BIRTHDAY WISH COUNT
    // =========================================================

    @GetMapping(
            "/api/students/{studentId}/birthday-wishes/unread-count"
    )
    @ResponseBody
    public ResponseEntity<?> getUnreadBirthdayWishCount(
            @PathVariable Long studentId) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            int count =
                    portalJdbcRepository
                            .getUnreadBirthdayWishCount(
                                    studentId
                            );

            return ResponseEntity.ok(
                    Map.of(
                            "count",
                            count
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to load birthday wish count"
                            )
                    );
        }
    }

    // =========================================================
    // MARK BIRTHDAY WISH AS READ
    // =========================================================

    @PutMapping(
            "/api/birthday-wishes/{wishId}/read"
    )
    @ResponseBody
    public ResponseEntity<?> markBirthdayWishAsRead(
            @PathVariable Long wishId,
            @RequestParam Long studentId) {

        try {

            Optional<Student> student =
                    studentService.getStudentById(studentId);

            if (student.isEmpty()) {

                return ResponseEntity.notFound()
                        .build();
            }

            portalJdbcRepository.markBirthdayWishAsRead(
                    wishId,
                    studentId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Birthday wish marked as read"
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError()
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to update birthday wish"
                            )
                    );
        }
    }

    // =========================================================
    // COMMON STUDENT RESPONSE
    // =========================================================

    private Map<String, Object> createStudentResponse(
            Student student) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "id",
                student.getId()
        );

        data.put(
                "registerNo",
                student.getRegisterNo()
        );

        data.put(
                "name",
                student.getName()
        );

        data.put(
                "dateOfBirth",
                student.getDateOfBirth()
        );

        data.put(
                "department",
                student.getDepartment()
        );

        data.put(
                "year",
                student.getYear()
        );

        data.put(
                "section",
                student.getSection()
        );

        data.put(
                "achievements",
                student.getAchievements()
        );

        data.put(
                "activities",
                student.getActivities()
        );

        data.put(
                "certificates",
                student.getCertificates()
        );

        data.put(
                "internship",
                student.getInternship()
        );

        return data;
    }
}
