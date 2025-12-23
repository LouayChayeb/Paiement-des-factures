package tn.pi.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pi.dtos.LoginRequest;
import tn.pi.entities.Account;
import tn.pi.entities.Admin;
import tn.pi.service.AdminService;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private  AdminService adminService;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Admin request, HttpServletRequest servletRequest) {

        Admin admin = adminService.login(request.getEmail(), request.getPassword());


        HttpSession session = servletRequest.getSession();
        session.setAttribute("admin", admin);


        String sessionId = session.getId();


        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "account", admin,
                        "sessionId", sessionId
                )
        );
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {

        Admin loggedAdmin = (Admin) session.getAttribute("admin");

        if (loggedAdmin == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not logged in"));
        }

        return ResponseEntity.ok(loggedAdmin);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
