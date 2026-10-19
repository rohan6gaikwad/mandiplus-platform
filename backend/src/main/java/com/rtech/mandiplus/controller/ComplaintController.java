package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.entity.Complaint;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.ComplaintRepository;
import com.rtech.mandiplus.repository.UserRepository;
import com.rtech.mandiplus.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    // Submit a dispute complaint
    @PostMapping("/complaints")
    public ResponseEntity<?> submitComplaint(@RequestBody Complaint complaint, Authentication authentication) {
        try {
            if (complaint.getTitle() == null || complaint.getTitle().trim().isEmpty()) {
                throw new RuntimeException("Error: Title is required for submitting complaints.");
            }
            if (complaint.getDescription() == null || complaint.getDescription().trim().isEmpty()) {
                throw new RuntimeException("Error: Description is required for submitting complaints.");
            }
            
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Error: Authenticated user not found."));

            complaint.setUser(user);
            complaint.setStatus("PENDING");
            Complaint saved = complaintRepository.save(complaint);

            notificationService.sendToAdmin(
                "New Dispute Complaint",
                String.format("User %s has filed a new dispute: \"%s\".", user.getEmail(), complaint.getTitle()),
                "SYSTEM"
            );

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all complaints submitted by the authenticated user
    @GetMapping({"/complaints", "/complaints/my"})
    public ResponseEntity<?> getMyComplaints(Authentication authentication) {
        try {
            return ResponseEntity.ok(complaintRepository.findByUserEmail(authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
