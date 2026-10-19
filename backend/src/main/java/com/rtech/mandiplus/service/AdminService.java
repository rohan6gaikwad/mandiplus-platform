package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.AdminStatsDTO;
import com.rtech.mandiplus.entity.Complaint;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ProductService productService;

    public AdminStatsDTO getGlobalMetrics() {
        AdminStatsDTO dto = new AdminStatsDTO();
        dto.setTotalUsers(userRepository.count());
        dto.setTotalFarmers(userRepository.countByRole("FARMER"));
        dto.setTotalBuyers(userRepository.countByRole("BUYER"));
        dto.setTotalProducts(productRepository.count());
        dto.setPendingProducts(productRepository.countByStatus("PENDING_APPROVAL"));
        dto.setTotalOrders(orderRepository.count());
        dto.setTotalComplaints(complaintRepository.count());
        return dto;
    }

    public AdminStatsDTO getAdminMetrics() {
        return getGlobalMetrics();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found!"));

        user.toggleActive();
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserActive(Long userId) {
        return toggleUserStatus(userId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Complaint resolveComplaint(Long complaintId, String status) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Error: Complaint case not found!"));

        complaint.resolve(status);
        Complaint saved = complaintRepository.save(complaint);

        // Notify the specific user who submitted the complaint
        notificationService.sendNotification(
            NotificationService.getUserIdSafely(saved.getUser()),
            "Dispute Case Update",
            String.format("Your dispute ticket #COMP-%d has been marked as %s.", saved.getId(), status.toUpperCase()),
            "SYSTEM"
        );

        return saved;
    }

    public void updateSystemParameters(double commission, double insurance) {
        notificationService.sendNotification(
            null,
            "System Configuration Modified",
            String.format("Platform escrow commission adjusted to %.1f%% and logistics transit insurance set to ₹%,.0f.", commission, insurance),
            "SYSTEM"
        );
    }

    public List<com.rtech.mandiplus.dto.ProductDTO> getAllProducts(String status) {
        List<com.rtech.mandiplus.entity.Product> products;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            products = productRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        } else {
            products = productRepository.findAllByOrderByCreatedAtDesc();
        }
        return products.stream().map(productService::convertToDTO).toList();
    }

    @Transactional
    public com.rtech.mandiplus.dto.ProductDTO approveProduct(Long id, com.rtech.mandiplus.dto.ProductDTO approvalData) {
        com.rtech.mandiplus.entity.Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product listing not found!"));

        if (approvalData != null) {
            if (approvalData.getPricePerQuintal() != null) {
                product.setPricePerQuintal(approvalData.getPricePerQuintal());
            }
            if (approvalData.getCropName() != null && !approvalData.getCropName().isBlank()) {
                product.setCropName(approvalData.getCropName());
            }
            if (approvalData.getVariety() != null && !approvalData.getVariety().isBlank()) {
                product.setVariety(approvalData.getVariety());
            }
            if (approvalData.getQuantityQuintals() != null) {
                product.setQuantityQuintals(approvalData.getQuantityQuintals());
            }
            if (approvalData.getDescription() != null) {
                product.setDescription(approvalData.getDescription());
            }
            if (approvalData.getImageUrl() != null && !approvalData.getImageUrl().isBlank()) {
                product.setImageUrl(approvalData.getImageUrl());
            }
            if (approvalData.getLocation() != null && !approvalData.getLocation().isBlank()) {
                product.setLocation(approvalData.getLocation());
            }
            if (approvalData.getAdminFeedback() != null && !approvalData.getAdminFeedback().isBlank()) {
                product.setAdminFeedback(approvalData.getAdminFeedback());
            } else {
                product.setAdminFeedback("Approved by Mandi+ Admin Desk");
            }
        } else {
            product.setAdminFeedback("Approved by Mandi+ Admin Desk");
        }

        product.setStatus("AVAILABLE");
        com.rtech.mandiplus.entity.Product saved = productRepository.save(product);

        // Notify farmer that listing is approved
        if (saved.getFarmer() != null && saved.getFarmer().getUser() != null) {
            notificationService.sendNotification(
                saved.getFarmer().getUser().getId(),
                "Listing Approved & Live!",
                String.format("Congratulations! Your produce %s (%s) has been approved at ₹%s/qtl and is now visible to buyers.",
                    saved.getCropName(), saved.getVariety(), saved.getPricePerQuintal()),
                "MARKETPLACE"
            );
        }

        // Broadcast to marketplace
        notificationService.sendNotification(
            null,
            "New Produce Available",
            String.format("%s (%s) listed at ₹%s/qtl from %s.",
                saved.getCropName(), saved.getVariety(), saved.getPricePerQuintal(), saved.getLocation()),
            "MARKETPLACE"
        );

        return productService.convertToDTO(saved);
    }

    @Transactional
    public com.rtech.mandiplus.dto.ProductDTO rejectProduct(Long id, String reason) {
        com.rtech.mandiplus.entity.Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product listing not found!"));

        product.setStatus("REJECTED");
        String feedback = (reason != null && !reason.trim().isEmpty()) 
                ? reason 
                : "Listing rejected by administrator. Please update photo or suggested rate.";
        product.setAdminFeedback(feedback);

        com.rtech.mandiplus.entity.Product saved = productRepository.save(product);

        // Notify farmer with the rejection feedback
        if (saved.getFarmer() != null && saved.getFarmer().getUser() != null) {
            notificationService.sendNotification(
                saved.getFarmer().getUser().getId(),
                "Listing Returned for Revision",
                String.format("Your listing for %s (%s) was returned by Admin. Reason: %s",
                    saved.getCropName(), saved.getVariety(), feedback),
                "MARKETPLACE"
            );
        }

        return productService.convertToDTO(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        com.rtech.mandiplus.entity.Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product listing not found!"));
        productRepository.delete(product);
    }
}
