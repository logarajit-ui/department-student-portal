package com.department.demo.department_portal.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class PortalJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public PortalJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================
    // PHOTO
    // =========================

    public void savePhoto(
            Long studentId,
            MultipartFile file
    ) throws Exception {

        String sql = """
                INSERT INTO student_photos
                (student_id, file_name, file_type, file_data)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                studentId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );
    }

    public List<Map<String, Object>> getPhotos(Long studentId) {

        String sql = """
                SELECT id, file_name, file_type, uploaded_at
                FROM student_photos
                WHERE student_id = ?
                ORDER BY uploaded_at DESC
                """;

        return jdbcTemplate.queryForList(sql, studentId);
    }

    public Map<String, Object> getPhoto(Long id) {

        String sql = """
                SELECT file_name, file_type, file_data
                FROM student_photos
                WHERE id = ?
                """;

        List<Map<String, Object>> result =
                jdbcTemplate.queryForList(sql, id);

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    // =========================
    // GALLERY
    // =========================

    public void saveGallery(
            Long studentId,
            String title,
            String description,
            MultipartFile file
    ) throws Exception {

        String sql = """
                INSERT INTO gallery
                (student_id, title, description, file_name, file_type, file_data)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                studentId,
                title,
                description,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );
    }

    public List<Map<String, Object>> getGallery(Long studentId) {

        String sql = """
                SELECT id, title, description,
                       file_name, file_type, uploaded_at
                FROM gallery
                WHERE student_id = ?
                ORDER BY uploaded_at DESC
                """;

        return jdbcTemplate.queryForList(sql, studentId);
    }

    public Map<String, Object> getGalleryFile(Long id) {

        String sql = """
                SELECT file_name, file_type, file_data
                FROM gallery
                WHERE id = ?
                """;

        List<Map<String, Object>> result =
                jdbcTemplate.queryForList(sql, id);

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    // =========================
    // CERTIFICATE
    // =========================

    public void saveCertificate(
            Long studentId,
            String title,
            String description,
            MultipartFile file
    ) throws Exception {

        String sql = """
                INSERT INTO certificates
                (student_id, title, description, file_name, file_type, file_data)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                studentId,
                title,
                description,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
        );
    }

    public List<Map<String, Object>> getCertificates(Long studentId) {

        String sql = """
                SELECT id, title, description,
                       file_name, file_type, uploaded_at
                FROM certificates
                WHERE student_id = ?
                ORDER BY uploaded_at DESC
                """;

        return jdbcTemplate.queryForList(sql, studentId);
    }

    public Map<String, Object> getCertificate(Long id) {

        String sql = """
                SELECT file_name, file_type, file_data
                FROM certificates
                WHERE id = ?
                """;

        List<Map<String, Object>> result =
                jdbcTemplate.queryForList(sql, id);

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    // =========================
    // LEAVE
    // =========================

    public void saveLeave(
            Long studentId,
            LocalDate fromDate,
            LocalDate toDate,
            String reason
    ) {

        String sql = """
                INSERT INTO leave_requests
                (student_id, from_date, to_date, reason, status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """;

        jdbcTemplate.update(
                sql,
                studentId,
                Date.valueOf(fromDate),
                Date.valueOf(toDate),
                reason
        );
    }

    public List<Map<String, Object>> getLeaves(Long studentId) {

        String sql = """
                SELECT id, from_date, to_date,
                       reason, status, hod_reason, applied_at
                FROM leave_requests
                WHERE student_id = ?
                ORDER BY applied_at DESC
                """;

        return jdbcTemplate.queryForList(sql, studentId);
    }

    // =========================
    // BIRTHDAY WISHES
    // =========================

    // Send birthday wish
    public void saveBirthdayWish(
            Long senderId,
            Long receiverId,
            String message
    ) {

        String sql = """
                INSERT INTO birthday_wishes
                (sender_id, receiver_id, message, is_read)
                VALUES (?, ?, ?, FALSE)
                """;

        jdbcTemplate.update(
                sql,
                senderId,
                receiverId,
                message
        );
    }

    // Get all birthday wishes received by a student
    public List<Map<String, Object>> getBirthdayWishes(
            Long receiverId
    ) {

        String sql = """
                SELECT
                    bw.id,
                    bw.sender_id,
                    s.name AS sender_name,
                    s.department AS sender_department,
                    bw.message,
                    bw.is_read,
                    bw.sent_at
                FROM birthday_wishes bw
                JOIN students s
                    ON s.id = bw.sender_id
                WHERE bw.receiver_id = ?
                ORDER BY bw.sent_at DESC
                """;

        return jdbcTemplate.queryForList(
                sql,
                receiverId
        );
    }

    // Get unread birthday wish count
    public int getUnreadBirthdayWishCount(
            Long receiverId
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM birthday_wishes
                WHERE receiver_id = ?
                AND is_read = FALSE
                """;

        Integer count =
                jdbcTemplate.queryForObject(
                        sql,
                        Integer.class,
                        receiverId
                );

        return count == null ? 0 : count;
    }

    // Mark birthday wish as read
    public void markBirthdayWishAsRead(
            Long wishId,
            Long receiverId
    ) {

        String sql = """
                UPDATE birthday_wishes
                SET is_read = TRUE
                WHERE id = ?
                AND receiver_id = ?
                """;

        jdbcTemplate.update(
                sql,
                wishId,
                receiverId
        );
    }
}