package com.rtech.mandiplus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtech.mandiplus.config.LiveFeedWebSocketHandler;
import com.rtech.mandiplus.dto.ChatContactDTO;
import com.rtech.mandiplus.dto.ChatConversationDTO;
import com.rtech.mandiplus.dto.ChatMessageDTO;
import com.rtech.mandiplus.dto.SendMessageRequest;
import com.rtech.mandiplus.entity.Buyer;
import com.rtech.mandiplus.entity.ChatMessage;
import com.rtech.mandiplus.entity.ColdStorage;
import com.rtech.mandiplus.entity.Farmer;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.BuyerRepository;
import com.rtech.mandiplus.repository.ChatMessageRepository;
import com.rtech.mandiplus.repository.ColdStorageRepository;
import com.rtech.mandiplus.repository.FarmerRepository;
import com.rtech.mandiplus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ColdStorageRepository coldStorageRepository;

    @Autowired
    private LiveFeedWebSocketHandler webSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Chat Privacy Rule: Only admin can chat with other users; regular users can ONLY chat with Admin/Platform
        if (!"ADMIN".equals(sender.getRole()) && !"ADMIN".equals(receiver.getRole())) {
            throw new RuntimeException("Access Denied: Regular users can only send messages to the Mandi+ Platform Admin Support.");
        }

        ChatMessage message = new ChatMessage(sender, receiver, request.getContent(), request.getMessageType());
        ChatMessage saved = chatMessageRepository.save(message);

        ChatMessageDTO dto = toDTO(saved);

        // Send real-time notification over WebSocket to both receiver and sender
        try {
            Map<String, Object> wsPayload = new HashMap<>();
            wsPayload.put("type", "CHAT_MESSAGE");
            wsPayload.put("message", dto);
            String json = objectMapper.writeValueAsString(wsPayload);

            webSocketHandler.sendToUser(receiver.getId(), json);
            webSocketHandler.sendToUser(sender.getId(), json);
        } catch (Exception e) {
            System.err.println("Failed to broadcast chat WebSocket message: " + e.getMessage());
        }

        return dto;
    }

    @Transactional
    public List<ChatMessageDTO> getChatHistory(Long userId, Long partnerId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Chat Privacy Rule: Non-admin users can ONLY fetch chat history with Admin
        if (!"ADMIN".equals(currentUser.getRole())) {
            User partner = userRepository.findById(partnerId)
                    .orElseThrow(() -> new RuntimeException("Partner not found"));
            if (!"ADMIN".equals(partner.getRole())) {
                throw new RuntimeException("Access Denied: You are only authorized to read your own conversation with Mandi+ Platform Support.");
            }
        }

        // Mark all incoming messages from partner as read
        chatMessageRepository.markMessagesAsRead(partnerId, userId);

        List<ChatMessage> messages = chatMessageRepository.findChatHistory(userId, partnerId);
        List<ChatMessageDTO> result = new ArrayList<>();
        for (ChatMessage m : messages) {
            result.add(toDTO(m));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ChatConversationDTO> getConversations(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        // Find primary Admin user
        User adminUser = null;
        List<User> admins = userRepository.findByRole("ADMIN");
        if (!admins.isEmpty()) {
            adminUser = admins.get(0);
        }

        Set<Long> partnerIds = new LinkedHashSet<>();

        if (isAdmin) {
            // Admin sees conversations from all users across the platform
            partnerIds.addAll(chatMessageRepository.findDistinctPartnerIds(currentUserId));
            List<User> allUsers = userRepository.findAll();
            for (User u : allUsers) {
                if (!u.getId().equals(currentUserId)) {
                    partnerIds.add(u.getId());
                }
            }
        } else {
            // Regular user ONLY has their private chat channel with Mandi+ Admin / Platform Support
            if (adminUser != null) {
                partnerIds.add(adminUser.getId());
            }
        }

        List<ChatConversationDTO> conversations = new ArrayList<>();

        for (Long partnerId : partnerIds) {
            Optional<User> partnerOpt = userRepository.findById(partnerId);
            if (partnerOpt.isEmpty()) continue;
            User partner = partnerOpt.get();

            ChatConversationDTO conv = new ChatConversationDTO();
            conv.setPartnerId(partner.getId());
            conv.setPartnerEmail(partner.getEmail());
            conv.setPartnerRole(partner.getRole());
            conv.setPartnerPhoto(partner.getProfilePhoto());
            conv.setPartnerName(resolveDisplayName(partner));

            // Is this the official Mandi+ Support / Admin?
            boolean isAdminChat = "ADMIN".equals(partner.getRole());
            conv.setIsPinned(isAdminChat);

            // Fetch latest message between current user and partner
            List<ChatMessage> latestList = chatMessageRepository.findLatestMessageBetween(currentUserId, partner.getId());
            if (!latestList.isEmpty()) {
                ChatMessage latest = latestList.get(0);
                conv.setLastMessage(latest.getContent());
                conv.setLastMessageTime(latest.getCreatedAt());
                conv.setLastMessageSenderId(latest.getSender().getId());
            } else {
                if (isAdminChat) {
                    conv.setLastMessage("Hello! How can Mandi+ Support help you today?");
                    conv.setLastMessageTime(LocalDateTime.now().minusHours(1));
                } else {
                    conv.setLastMessage("Click to open platform support desk");
                    conv.setLastMessageTime(null);
                }
            }

            Long unread = chatMessageRepository.countUnreadFromSender(partner.getId(), currentUserId);
            conv.setUnreadCount(unread != null ? unread : 0L);
            conv.setIsOnline(true); // Active on platform

            conversations.add(conv);
        }

        // Sort: Pinned first, then by lastMessageTime descending
        conversations.sort((a, b) -> {
            if (Boolean.TRUE.equals(a.getIsPinned()) && !Boolean.TRUE.equals(b.getIsPinned())) return -1;
            if (!Boolean.TRUE.equals(a.getIsPinned()) && Boolean.TRUE.equals(b.getIsPinned())) return 1;

            if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return conversations;
    }

    @Transactional(readOnly = true)
    public List<ChatContactDTO> getAvailableContacts(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatContactDTO> contacts = new ArrayList<>();

        if ("ADMIN".equals(currentUser.getRole())) {
            // Admin can view and contact all users on the platform
            List<User> users = userRepository.findAll();
            for (User u : users) {
                if (u.getId().equals(currentUserId)) continue;

                ChatContactDTO dto = new ChatContactDTO();
                dto.setId(u.getId());
                dto.setEmail(u.getEmail());
                dto.setRole(u.getRole());
                dto.setProfilePhoto(u.getProfilePhoto());
                dto.setName(resolveDisplayName(u));
                dto.setSubtitle(resolveSubtitle(u));
                dto.setIsOnline(true);
                contacts.add(dto);
            }

            contacts.sort((a, b) -> {
                if ("ADMIN".equals(a.getRole()) && !"ADMIN".equals(b.getRole())) return -1;
                if (!"ADMIN".equals(a.getRole()) && "ADMIN".equals(b.getRole())) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });
        } else {
            // Regular user (Farmer, Buyer, Cold Storage): ONLY see the Admin Support contact
            List<User> admins = userRepository.findByRole("ADMIN");
            for (User admin : admins) {
                ChatContactDTO dto = new ChatContactDTO();
                dto.setId(admin.getId());
                dto.setEmail(admin.getEmail());
                dto.setRole("ADMIN");
                dto.setProfilePhoto(admin.getProfilePhoto());
                dto.setName("Mandi+ Admin Support");
                dto.setSubtitle("Official Platform Helpdesk & Moderation");
                dto.setIsOnline(true);
                contacts.add(dto);
            }
        }

        return contacts;
    }

    @Transactional
    public void markAsRead(Long currentUserId, Long partnerId) {
        chatMessageRepository.markMessagesAsRead(partnerId, currentUserId);
    }

    @Transactional(readOnly = true)
    public Long getTotalUnreadCount(Long currentUserId) {
        Long count = chatMessageRepository.countTotalUnread(currentUserId);
        return count != null ? count : 0L;
    }

    private String resolveDisplayName(User user) {
        if ("ADMIN".equals(user.getRole())) {
            return "Mandi+ Admin Support";
        } else if ("FARMER".equals(user.getRole())) {
            Optional<Farmer> farmerOpt = farmerRepository.findByUser(user);
            if (farmerOpt.isPresent() && farmerOpt.get().getName() != null) {
                return farmerOpt.get().getName() + " (Farmer)";
            }
        } else if ("BUYER".equals(user.getRole())) {
            Optional<Buyer> buyerOpt = buyerRepository.findByUser(user);
            if (buyerOpt.isPresent()) {
                Buyer b = buyerOpt.get();
                if (b.getCompanyName() != null && !b.getCompanyName().isEmpty()) {
                    return b.getCompanyName() + " (Buyer)";
                } else if (b.getRepresentativeName() != null) {
                    return b.getRepresentativeName() + " (Buyer)";
                }
            }
        } else if ("COLD_STORAGE_MANAGER".equals(user.getRole())) {
            Optional<ColdStorage> storageOpt = coldStorageRepository.findByManager(user);
            if (storageOpt.isPresent() && storageOpt.get().getName() != null) {
                return storageOpt.get().getName() + " (Cold Storage)";
            }
        }

        String emailPrefix = user.getEmail().split("@")[0];
        return emailPrefix.substring(0, 1).toUpperCase() + emailPrefix.substring(1) + " (" + user.getRole() + ")";
    }

    private String resolveSubtitle(User user) {
        if ("ADMIN".equals(user.getRole())) {
            return "Official Platform Helpdesk & Support";
        } else if ("FARMER".equals(user.getRole())) {
            Optional<Farmer> f = farmerRepository.findByUser(user);
            return f.map(farmer -> farmer.getDistrict() + ", " + farmer.getState() + " • " + farmer.getFarmSizeAcres() + " Acres").orElse("Verified Crop Producer");
        } else if ("BUYER".equals(user.getRole())) {
            Optional<Buyer> b = buyerRepository.findByUser(user);
            return b.map(buyer -> "Procurement • " + buyer.getAddress()).orElse("Wholesale Trader");
        } else if ("COLD_STORAGE_MANAGER".equals(user.getRole())) {
            Optional<ColdStorage> s = coldStorageRepository.findByManager(user);
            return s.map(storage -> storage.getLocation() + " • " + storage.getTotalCapacityTons() + " Tons").orElse("Temperature Controlled Storage");
        }
        return "Mandi+ Member";
    }

    private ChatMessageDTO toDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderEmail(message.getSender().getEmail());
        dto.setSenderName(resolveDisplayName(message.getSender()));
        dto.setSenderRole(message.getSender().getRole());

        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverEmail(message.getReceiver().getEmail());
        dto.setReceiverName(resolveDisplayName(message.getReceiver()));
        dto.setReceiverRole(message.getReceiver().getRole());

        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setIsRead(message.getIsRead());
        dto.setMessageType(message.getMessageType());
        return dto;
    }
}
