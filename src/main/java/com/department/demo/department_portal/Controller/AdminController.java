package com.department.demo.department_portal.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class AdminController {
    private final JdbcTemplate jdbcTemplate;
    public AdminController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    private String role(HttpSession s){ Object v=s.getAttribute("portalRole"); return v==null?"":v.toString().toUpperCase(); }
    private boolean isHod(HttpSession s){ return "HOD".equals(role(s)); }
    private boolean loggedIn(HttpSession s){ return s.getAttribute("portalUser")!=null; }
    private ResponseEntity<?> unauthorized(){ return ResponseEntity.status(401).body(Map.of("message","Please login first.")); }
    private ResponseEntity<?> forbidden(){ return ResponseEntity.status(403).body(Map.of("message","HOD access required.")); }
    @SuppressWarnings("unchecked") private Map<String,Object> user(HttpSession s){ return (Map<String,Object>)s.getAttribute("portalUser"); }

    @PostMapping(value="/api/admin/announcements", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAnnouncement(@RequestParam String title,
            @RequestParam String message,
            @RequestParam(value="file",required=false) MultipartFile file,
            HttpSession session){
        if(!isHod(session)) return forbidden();
        if(title.isBlank()||message.isBlank()) return ResponseEntity.badRequest().body(Map.of("message","Title and message are required."));
        if(file!=null&&!file.isEmpty()&&!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())) return ResponseEntity.badRequest().body(Map.of("message","Only PDF files are allowed."));
        try{
            Map<String,Object> u=user(session);
            jdbcTemplate.update("INSERT INTO announcements " +
                "                (title,message,posted_by,posted_role,pdf_name,pdf_type,pdf_data) " +
                "                VALUES (?,?,?,?,?,?,?) ", title.trim(),message.trim(),String.valueOf(u.getOrDefault("name","HOD")),"HOD",
                file==null||file.isEmpty()?null:file.getOriginalFilename(), file==null||file.isEmpty()?null:file.getContentType(), file==null||file.isEmpty()?null:file.getBytes());
            return ResponseEntity.ok(Map.of("success",true,"message","Message sent successfully."));
        }catch(Exception e){e.printStackTrace();return ResponseEntity.internalServerError().body(Map.of("message","Failed to send message: "+e.getMessage()));}
    }

    @GetMapping("/api/students/announcements")
    public ResponseEntity<?> getAnnouncements(HttpSession s){
        if(!loggedIn(s)) return unauthorized();
        return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT id,title,message,posted_by AS postedBy,posted_role AS postedRole,posted_at AS postedAt, " +
                "            pdf_name AS pdfName,pdf_type AS pdfType,CASE WHEN pdf_data IS NULL THEN FALSE ELSE TRUE END AS hasPdf " +
                "            FROM announcements ORDER BY posted_at DESC,id DESC "));
    }
    @GetMapping("/api/students/announcements/{id}/file")
    public ResponseEntity<byte[]> getAnnouncementFile(@PathVariable Long id,HttpSession s){
        if(!loggedIn(s)) return ResponseEntity.status(401).build();
        List<Map<String,Object>> r=jdbcTemplate.queryForList("SELECT pdf_name,pdf_type,pdf_data FROM announcements WHERE id=?",id);
        if(r.isEmpty()||r.get(0).get("pdf_data")==null) return ResponseEntity.notFound().build();
        Map<String,Object> x=r.get(0); String type=String.valueOf(x.getOrDefault("pdf_type",MediaType.APPLICATION_PDF_VALUE));
        return ResponseEntity.ok().header("Content-Type",type).header("Content-Disposition","inline; filename=\""+x.get("pdf_name")+"\"").body((byte[])x.get("pdf_data"));
    }

    @PostMapping(value="/api/admin/events", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createEvent(@RequestParam String title,@RequestParam String eventDate,
            @RequestParam(required=false,defaultValue="") String eventTime,@RequestParam(required=false,defaultValue="") String location,
            @RequestParam(required=false,defaultValue="") String description,@RequestParam(value="file",required=false) MultipartFile file,HttpSession s){
        if(!isHod(s)) return forbidden();
        if(title.isBlank()||eventDate.isBlank()) return ResponseEntity.badRequest().body(Map.of("message","Event title and date are required."));
        if(file!=null&&!file.isEmpty()&&!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())) return ResponseEntity.badRequest().body(Map.of("message","Only PDF files are allowed."));
        try{
            Map<String,Object> u=user(s);
            jdbcTemplate.update("INSERT INTO department_events " +
                "                (title,event_date,event_time,location,description,created_by,created_role,pdf_name,pdf_type,pdf_data) " +
                "                VALUES (?,?,?,?,?,?,?,?,?,?) ",title.trim(),Date.valueOf(eventDate),eventTime,location,description,String.valueOf(u.getOrDefault("name","HOD")),"HOD",
                file==null||file.isEmpty()?null:file.getOriginalFilename(),file==null||file.isEmpty()?null:file.getContentType(),file==null||file.isEmpty()?null:file.getBytes());
            return ResponseEntity.ok(Map.of("success",true,"message","Event added successfully."));
        }catch(Exception e){e.printStackTrace();return ResponseEntity.internalServerError().body(Map.of("message","Failed to add event: "+e.getMessage()));}
    }
    @GetMapping("/api/students/events")
    public ResponseEntity<?> getEvents(HttpSession s){
        if(!loggedIn(s)) return unauthorized();
        return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT id,title,DATE_FORMAT(event_date,'%Y-%m-%d') AS eventDate,event_time AS eventTime,location,description, " +
                "            created_by AS createdBy,created_role AS createdRole,pdf_name AS pdfName,pdf_type AS pdfType,CASE WHEN pdf_data IS NULL THEN FALSE ELSE TRUE END AS hasPdf " +
                "            FROM department_events ORDER BY event_date ASC,id DESC "));
    }
    @GetMapping("/api/students/events/{id}/file")
    public ResponseEntity<byte[]> getEventFile(@PathVariable Long id,HttpSession s){
        if(!loggedIn(s)) return ResponseEntity.status(401).build();
        List<Map<String,Object>> r=jdbcTemplate.queryForList("SELECT pdf_name,pdf_type,pdf_data FROM department_events WHERE id=?",id);
        if(r.isEmpty()||r.get(0).get("pdf_data")==null) return ResponseEntity.notFound().build();
        Map<String,Object> x=r.get(0); String type=String.valueOf(x.getOrDefault("pdf_type",MediaType.APPLICATION_PDF_VALUE));
        return ResponseEntity.ok().header("Content-Type",type).header("Content-Disposition","inline; filename=\""+x.get("pdf_name")+"\"").body((byte[])x.get("pdf_data"));
    }

    @GetMapping("/api/admin/students")
    public ResponseEntity<?> getAllStudents(HttpSession s){
        if(!isHod(s)) return forbidden();
        return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT id,register_no AS registerNo,name,date_of_birth AS dateOfBirth,department,year,section,achievements,activities,certificates,internship, " +
                "            (SELECT id FROM student_photos p WHERE p.student_id=students.id ORDER BY uploaded_at DESC,id DESC LIMIT 1) AS photoId " +
                "            FROM students ORDER BY name ASC "));
    }
    @GetMapping("/api/admin/students/{id}")
    public ResponseEntity<?> getStudent(@PathVariable Long id,HttpSession s){
        if(!isHod(s)) return forbidden();
        List<Map<String,Object>> r=jdbcTemplate.queryForList("SELECT id,register_no AS registerNo,name,date_of_birth AS dateOfBirth,department,year,section,achievements,activities,certificates,internship, " +
                "            (SELECT id FROM student_photos p WHERE p.student_id=students.id ORDER BY uploaded_at DESC,id DESC LIMIT 1) AS photoId " +
                "            FROM students WHERE id=? ",id);
        if(r.isEmpty()) return ResponseEntity.notFound().build(); return ResponseEntity.ok(r.get(0));
    }

    @GetMapping("/api/admin/leaves")
    public ResponseEntity<?> getLeaves(HttpSession s){
        if(!isHod(s)) return forbidden();
        return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT lr.id,lr.student_id AS studentId,s.name AS studentName,s.register_no AS registerNo, " +
                "            lr.from_date,lr.to_date,lr.reason,lr.status,lr.hod_reason,lr.applied_at FROM leave_requests lr JOIN students s ON s.id=lr.student_id ORDER BY lr.applied_at DESC,lr.id DESC "));
    }
    @PutMapping("/api/admin/leaves/{id}/status")
    public ResponseEntity<?> updateLeave(@PathVariable Long id,@RequestBody Map<String,String> data,HttpSession s){
        if(!isHod(s)) return forbidden(); String status=data.getOrDefault("status","").trim().toUpperCase();
        if(!Set.of("APPROVED","REJECTED","PENDING","CANCELLED").contains(status)) return ResponseEntity.badRequest().body(Map.of("message","Invalid leave status."));
        String hodReason=data.getOrDefault("hodReason","").trim();
        if("REJECTED".equals(status) && hodReason.isBlank()) return ResponseEntity.badRequest().body(Map.of("message","Rejection reason is required."));
        if("REJECTED".equals(status)) {
            int n=jdbcTemplate.update("UPDATE leave_requests SET status=?, hod_reason=? WHERE id=?",status,hodReason,id);
            if(n==0)return ResponseEntity.notFound().build();
        } else {
            int n=jdbcTemplate.update("UPDATE leave_requests SET status=?, hod_reason=NULL WHERE id=?",status,id);
            if(n==0)return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("success",true,"message","Leave request updated."));
    }

    @PostMapping(value="/api/admin/achievements",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAchievement(@RequestParam Long studentId,@RequestParam String title,@RequestParam(required=false,defaultValue="") String description,
            @RequestParam("file") MultipartFile file,HttpSession s){
        if(!isHod(s)) return forbidden(); if(file==null||file.isEmpty())return ResponseEntity.badRequest().body(Map.of("message","Achievement file is required."));
        try{Map<String,Object>u=user(s);jdbcTemplate.update("INSERT INTO student_achievements(student_id,title,description,file_name,file_type,file_data,posted_by,posted_role) VALUES(?,?,?,?,?,?,?,?) ",
            studentId,title.trim(),description.trim(),file.getOriginalFilename(),file.getContentType(),file.getBytes(),String.valueOf(u.getOrDefault("name","HOD")),"HOD");
            jdbcTemplate.update("UPDATE students SET achievements=CONCAT(CASE WHEN achievements IS NULL OR achievements='' THEN '' ELSE CONCAT(achievements,'\\n') END,?) WHERE id=?",title.trim(),studentId);
            return ResponseEntity.ok(Map.of("success",true,"message","Student achievement uploaded successfully."));
        }catch(Exception e){e.printStackTrace();return ResponseEntity.internalServerError().body(Map.of("message","Failed to upload achievement."));}
    }
    @GetMapping("/api/students/achievements")
    public ResponseEntity<?> getAchievements(HttpSession s){
        if(!loggedIn(s))return unauthorized();
        return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT a.id,a.title,a.description,a.file_name AS fileName,s.name AS studentName,s.register_no AS registerNo,s.department, " +
                "            a.posted_by AS postedBy,a.posted_role AS postedRole,a.posted_at AS postedAt FROM student_achievements a JOIN students s ON s.id=a.student_id ORDER BY a.posted_at DESC,a.id DESC "));
    }
    @GetMapping("/api/students/achievements/{id}/file")
    public ResponseEntity<byte[]> getAchievementFile(@PathVariable Long id,HttpSession s){
        if(!loggedIn(s))return ResponseEntity.status(401).build();List<Map<String,Object>>r=jdbcTemplate.queryForList("SELECT file_name,file_type,file_data FROM student_achievements WHERE id=?",id);if(r.isEmpty())return ResponseEntity.notFound().build();Map<String,Object>x=r.get(0);return ResponseEntity.ok().header("Content-Type",String.valueOf(x.getOrDefault("file_type",MediaType.APPLICATION_OCTET_STREAM_VALUE))).header("Content-Disposition","inline; filename=\""+x.get("file_name")+"\"").body((byte[])x.get("file_data"));
    }

    @PostMapping(value="/api/admin/gallery",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadGallery(@RequestParam String title,@RequestParam(required=false,defaultValue="")String description,@RequestParam("file")MultipartFile file,HttpSession s){
        if(!isHod(s))return forbidden();if(file==null||file.isEmpty())return ResponseEntity.badRequest().body(Map.of("message","Gallery image is required."));String type=file.getContentType();if(type==null||!type.startsWith("image/"))return ResponseEntity.badRequest().body(Map.of("message","Only image files are allowed."));
        try{Map<String,Object>u=user(s);jdbcTemplate.update("INSERT INTO department_gallery(title,description,file_name,file_type,file_data,uploaded_by,uploaded_role) VALUES(?,?,?,?,?,?,?)",title.trim(),description.trim(),file.getOriginalFilename(),type,file.getBytes(),String.valueOf(u.getOrDefault("name","HOD")),"HOD");return ResponseEntity.ok(Map.of("success",true,"message","Photo uploaded successfully."));}catch(Exception e){return ResponseEntity.internalServerError().body(Map.of("message","Failed to upload gallery photo."));}
    }
    @GetMapping("/api/students/gallery")
    public ResponseEntity<?> getGallery(HttpSession s){if(!loggedIn(s))return unauthorized();return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT id,title,description,file_name AS fileName,file_type AS fileType,uploaded_by AS uploadedBy,uploaded_role AS uploadedRole,uploaded_at AS uploadedAt FROM department_gallery ORDER BY uploaded_at DESC,id DESC"));}
    @GetMapping("/api/students/gallery/{id}")
    public ResponseEntity<byte[]> getGalleryFile(@PathVariable Long id,HttpSession s){if(!loggedIn(s))return ResponseEntity.status(401).build();List<Map<String,Object>>r=jdbcTemplate.queryForList("SELECT file_name,file_type,file_data FROM department_gallery WHERE id=?",id);if(r.isEmpty())return ResponseEntity.notFound().build();Map<String,Object>x=r.get(0);return ResponseEntity.ok().header("Content-Type",String.valueOf(x.getOrDefault("file_type",MediaType.APPLICATION_OCTET_STREAM_VALUE))).header("Content-Disposition","inline; filename=\""+x.get("file_name")+"\"").body((byte[])x.get("file_data"));}
    @PostMapping(value="/api/admin/study-materials", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStudyMaterial(@RequestParam String title,
            @RequestParam(required=false,defaultValue="") String subject,
            @RequestParam(required=false,defaultValue="") String description,
            @RequestParam("file") MultipartFile file, HttpSession s){
        if(!isHod(s)) return forbidden();
        if(title.isBlank()) return ResponseEntity.badRequest().body(Map.of("message","Study material title is required."));
        if(file==null||file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message","Study material PDF is required."));
        if(!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())) return ResponseEntity.badRequest().body(Map.of("message","Only PDF files are allowed."));
        try{
            Map<String,Object> u=user(s);
            jdbcTemplate.update("INSERT INTO study_materials(title,subject,description,file_name,file_type,file_data,posted_by,posted_role) VALUES(?,?,?,?,?,?,?,?)",
                title.trim(),subject.trim(),description.trim(),file.getOriginalFilename(),file.getContentType(),file.getBytes(),String.valueOf(u.getOrDefault("name","HOD")),"HOD");
            return ResponseEntity.ok(Map.of("success",true,"message","Study material uploaded successfully."));
        }catch(Exception e){e.printStackTrace();return ResponseEntity.internalServerError().body(Map.of("message","Failed to upload study material."));}
    }

    @GetMapping("/api/students/study-materials")
    public ResponseEntity<?> getStudyMaterials(HttpSession s){
        if(!loggedIn(s)) return unauthorized();
        return ResponseEntity.ok(jdbcTemplate.queryForList("SELECT id,title,subject,description,file_name AS fileName,file_type AS fileType,posted_by AS postedBy,posted_role AS postedRole,posted_at AS postedAt FROM study_materials ORDER BY posted_at DESC,id DESC"));
    }

    @GetMapping("/api/students/study-materials/{id}/file")
    public ResponseEntity<byte[]> getStudyMaterialFile(@PathVariable Long id,HttpSession s){
        if(!loggedIn(s)) return ResponseEntity.status(401).build();
        List<Map<String,Object>> r=jdbcTemplate.queryForList("SELECT file_name,file_type,file_data FROM study_materials WHERE id=?",id);
        if(r.isEmpty()||r.get(0).get("file_data")==null) return ResponseEntity.notFound().build();
        Map<String,Object> x=r.get(0);
        return ResponseEntity.ok().header("Content-Type",String.valueOf(x.getOrDefault("file_type",MediaType.APPLICATION_PDF_VALUE))).header("Content-Disposition","inline; filename=\""+x.get("file_name")+"\"").body((byte[])x.get("file_data"));
    }

}
