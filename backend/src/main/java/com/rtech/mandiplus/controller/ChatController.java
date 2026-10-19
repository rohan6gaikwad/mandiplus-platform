package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.ChatContactDTO;
import com.rtech.mandiplus.dto.ChatConversationDTO;
import com.rtech.mandiplus.dto.ChatMessageDTO;
import com.rtech.mandiplus.dto.SendMessageRequest;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.UserRepository;
import com.rtech.mandiplus.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${server.port:8082}")
    private int serverPort;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(Authentication authentication, @Valid @RequestBody SendMessageRequest request) {
        try {
            User sender = getCurrentUser(authentication);
            ChatMessageDTO dto = chatService.sendMessage(sender.getId(), request);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending message: " + e.getMessage());
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "fileType", required = false) String fileType) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Uploaded file is empty"));
            }

            String uploadsDir = "uploads/chat";
            File dir = new File(uploadsDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "media";
            String extension = "";
            int extIdx = originalName.lastIndexOf(".");
            if (extIdx > 0) {
                extension = originalName.substring(extIdx);
            }

            String savedFileName = UUID.randomUUID().toString() + extension;
            File targetFile = new File(dir, savedFileName);
            file.transferTo(targetFile.toPath());

            String detectedType = fileType;
            if (detectedType == null || detectedType.trim().isEmpty()) {
                String contentType = file.getContentType();
                if (contentType != null) {
                    if (contentType.startsWith("image/")) detectedType = "IMAGE";
                    else if (contentType.startsWith("audio/")) detectedType = "AUDIO";
                    else if (contentType.startsWith("video/")) detectedType = "VIDEO";
                    else detectedType = "DOCUMENT";
                } else {
                    detectedType = "DOCUMENT";
                }
            }

            String mediaUrl = "http://localhost:" + serverPort + "/api/chat/media/" + savedFileName;

            return ResponseEntity.ok(Map.of(
                "url", mediaUrl,
                "fileType", detectedType,
                "fileName", originalName,
                "fileSize", file.getSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/media/{fileName:.+}")
    public ResponseEntity<?> serveMedia(@PathVariable String fileName) {
        try {
            File file = new File("uploads/chat", fileName);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Path path = file.toPath();
            Resource resource = new UrlResource(path.toUri());
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history/{partnerId}")
    public ResponseEntity<?> getChatHistory(Authentication authentication, @PathVariable Long partnerId) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<ChatMessageDTO> history = chatService.getChatHistory(currentUser.getId(), partnerId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading chat history: " + e.getMessage());
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<ChatConversationDTO> conversations = chatService.getConversations(currentUser.getId());
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading conversations: " + e.getMessage());
        }
    }

    @GetMapping("/contacts")
    public ResponseEntity<?> getContacts(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<ChatContactDTO> contacts = chatService.getAvailableContacts(currentUser.getId());
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading contacts: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/read/{partnerId}", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<?> markAsRead(Authentication authentication, @PathVariable Long partnerId) {
        try {
            User currentUser = getCurrentUser(authentication);
            chatService.markAsRead(currentUser.getId(), partnerId);
            return ResponseEntity.ok(Map.of("message", "Marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating read status: " + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Long count = chatService.getTotalUnreadCount(currentUser.getId());
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error checking unread count: " + e.getMessage());
        }
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User session not found"));
    }
}
