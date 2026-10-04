package com.department.demo.department_portal.Controller;

import com.department.demo.department_portal.Model.Student;
import com.department.demo.department_portal.Service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins="*")
public class AuthController {
    private final StudentService studentService; private final JdbcTemplate jdbcTemplate;
    public AuthController(StudentService studentService,JdbcTemplate jdbcTemplate){this.studentService=studentService;this.jdbcTemplate=jdbcTemplate;}
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> data,HttpSession session){
        try{
            String role=data.getOrDefault("role","STUDENT").trim().toUpperCase();String collegeId=data.get("collegeId");String dobText=data.get("dateOfBirth");
            if(collegeId==null||collegeId.isBlank()||dobText==null||dobText.isBlank())return ResponseEntity.badRequest().body(Map.of("message","College ID / Register Number and date of birth are required."));
            LocalDate dob;try{dob=LocalDate.parse(dobText.trim());}catch(Exception e){return ResponseEntity.badRequest().body(Map.of("message","Invalid date of birth. Use YYYY-MM-DD."));}
            if("STUDENT".equals(role)){
                Optional<Student> o=studentService.login(collegeId.trim(),dob);if(o.isEmpty())return ResponseEntity.status(401).body(Map.of("message","Invalid register number or date of birth"));Student s=o.get();Map<String,Object>u=new HashMap<>();
                u.put("id",s.getId());u.put("registerNo",s.getRegisterNo());u.put("name",s.getName());u.put("dateOfBirth",s.getDateOfBirth());u.put("department",s.getDepartment());u.put("year",s.getYear());u.put("section",s.getSection());u.put("achievements",s.getAchievements());u.put("activities",s.getActivities());u.put("certificates",s.getCertificates());u.put("internship",s.getInternship());u.put("role","STUDENT");session.setAttribute("portalUser",u);session.setAttribute("portalRole","STUDENT");return ResponseEntity.ok(u);
            }
            if(!"HOD".equals(role))return ResponseEntity.badRequest().body(Map.of("message","Only Student and HOD login are allowed."));
            List<Map<String,Object>> rows=jdbcTemplate.queryForList("SELECT id,college_id,name,date_of_birth,role FROM users WHERE college_id=? AND date_of_birth=? AND UPPER(role)='HOD' LIMIT 1",collegeId.trim(),dob);
            if(rows.isEmpty())return ResponseEntity.status(401).body(Map.of("message","Invalid HOD login"));Map<String,Object>r=rows.get(0);Map<String,Object>u=new HashMap<>();u.put("id",r.get("id"));u.put("collegeId",r.get("college_id"));u.put("name",r.get("name"));u.put("dateOfBirth",r.get("date_of_birth"));u.put("role","HOD");session.setAttribute("portalUser",u);session.setAttribute("portalRole","HOD");return ResponseEntity.ok(u);
        }catch(Exception e){e.printStackTrace();return ResponseEntity.internalServerError().body(Map.of("message","Login failed: "+e.getMessage()));}
    }
    @GetMapping("/me") public ResponseEntity<?> me(HttpSession s){Object u=s.getAttribute("portalUser");if(u==null)return ResponseEntity.status(401).body(Map.of("loggedIn",false));return ResponseEntity.ok(Map.of("loggedIn",true,"user",u));}
    @PostMapping("/logout") public ResponseEntity<?> logout(HttpSession s){s.invalidate();return ResponseEntity.ok(Map.of("success",true,"message","Logged out successfully"));}
}
