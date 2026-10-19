package com.rtech.mandiplus.service;

import com.rtech.mandiplus.dto.OrderDTO;
import com.rtech.mandiplus.entity.Buyer;
import com.rtech.mandiplus.entity.Order;
import com.rtech.mandiplus.entity.Product;
import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.entity.OrderStatus;
import com.rtech.mandiplus.repository.BuyerRepository;
import com.rtech.mandiplus.repository.OrderRepository;
import com.rtech.mandiplus.repository.ProductRepository;
import com.rtech.mandiplus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired(required = false)
    private DeliveryService deliveryService;

    public List<OrderDTO> getBuyerOrders(String buyerEmail) {
        return orderRepository.findByBuyerUserEmail(buyerEmail).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getFarmerOrders(String farmerEmail) {
        return orderRepository.findByFarmerUserEmail(farmerEmail).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO placeOrder(OrderDTO dto, String buyerEmail) {
        Buyer buyer = buyerRepository.findByUserEmail(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Error: Buyer profile not found!"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Error: Product listing not found!"));

        // Deduct quantity using Product entity encapsulation
        product.deductQuantity(dto.getQuantityQuintals());
        productRepository.save(product);

        // Place order
        Order order = new Order();
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantityQuintals(dto.getQuantityQuintals());
        
        BigDecimal totalCost = product.getPricePerQuintal().multiply(dto.getQuantityQuintals());
        order.setTotalPrice(totalCost);
        order.setStatus("PENDING");

        order = orderRepository.save(order);

        // Notify Farmer
        notificationService.sendNotification(
            NotificationService.getUserIdSafely(product.getFarmer().getUser()),
            "New Incoming Order",
            String.format("Buyer %s ordered %s qtl of %s.", buyer.getCompanyName(), dto.getQuantityQuintals().toString(), product.getCropName()),
            "ORDER"
        );

        // Initialize Delivery Consignment in DELIVERY_PENDING status
        if (deliveryService != null) {
            try {
                deliveryService.createOrFindTaskForOrder(order.getId());
            } catch (Exception ignored) {}
        }

        return convertToDTO(order);
    }

    public OrderDTO createOrder(OrderDTO dto, String buyerEmail) {
        return placeOrder(dto, buyerEmail);
    }

    public OrderDTO cancelOrder(Long id, String email) {
        return updateOrderStatus(id, "CANCELLED", null, email);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status, String estimatedDeliveryDateStr, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Error: Order not found!"));

        // Authentication checks: Farmer can ship/approve, Buyer can complete
        String farmerEmail = order.getProduct().getFarmer().getUser().getEmail();
        String buyerEmail = order.getBuyer().getUser().getEmail();

        if (!userEmail.equals(farmerEmail) && !userEmail.equals(buyerEmail)) {
            throw new RuntimeException("Error: Unauthorized to modify this order!");
        }

        String upperStatus = status.toUpperCase();

        // Convert target status to OrderStatus enum values
        OrderStatus targetStatusEnum;
        try {
            targetStatusEnum = OrderStatus.valueOf(upperStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error: Invalid order status value specified!");
        }

        // Handle estimated delivery date when status is APPROVED
        if (targetStatusEnum == OrderStatus.APPROVED) {
            if (estimatedDeliveryDateStr == null || estimatedDeliveryDateStr.trim().isEmpty()) {
                throw new RuntimeException("Error: Estimated delivery date is required when approving an order.");
            }
            try {
                order.setEstimatedDeliveryDate(java.time.LocalDate.parse(estimatedDeliveryDateStr));
            } catch (Exception e) {
                throw new RuntimeException("Error: Invalid estimated delivery date format! Expected YYYY-MM-DD.");
            }
        }

        // Fetch user role
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Error: Authenticated user credentials not found in DB!"));
        String role = user.getRole().toUpperCase();

        // Delegate to the Order entity's state machine for validation & transition
        order.transitionTo(targetStatusEnum, role);
        
        // Save side-effects
        productRepository.save(order.getProduct());
        order = orderRepository.save(order);

        // Notify respective user
        if ("APPROVED".equals(upperStatus) || "SHIPPED".equals(upperStatus) || "REJECTED".equals(upperStatus) || "DELIVERED".equals(upperStatus)) {
            // Notify Buyer
            String msg;
            if ("REJECTED".equals(upperStatus)) {
                msg = String.format("Order #AGR-%d for %s has been rejected by the farmer.", order.getId(), order.getProduct().getCropName());
            } else if ("APPROVED".equals(upperStatus)) {
                msg = String.format("Order #AGR-%d for %s has been approved by the farmer. Estimated delivery date: %s.", order.getId(), order.getProduct().getCropName(), order.getEstimatedDeliveryDate().toString());
            } else if ("DELIVERED".equals(upperStatus)) {
                msg = String.format("Order #AGR-%d for %s has been delivered by the farmer.", order.getId(), order.getProduct().getCropName());
            } else {
                msg = String.format("Order #AGR-%d for %s has been %s by the farmer.", order.getId(), order.getProduct().getCropName(), status.toLowerCase());
            }
            notificationService.sendNotification(
                NotificationService.getUserIdSafely(order.getBuyer().getUser()),
                "Order Status Updated",
                msg,
                "ORDER"
            );
        }

        if ("DELIVERED".equals(upperStatus)) {
            // Notify Farmer
            notificationService.sendNotification(
                NotificationService.getUserIdSafely(order.getProduct().getFarmer().getUser()),
                "Payment Escrow Cleared",
                String.format("Order #AGR-%d has been marked delivered. Funds are credited to your UPI ID %s.", order.getId(), order.getProduct().getFarmer().getUpiId()),
                "PAYMENT"
            );
        } else if ("CANCELLED".equals(upperStatus)) {
            // Notify Farmer
            notificationService.sendNotification(
                NotificationService.getUserIdSafely(order.getProduct().getFarmer().getUser()),
                "Order Cancelled",
                String.format("Order #AGR-%d for %s was cancelled by the buyer.", order.getId(), order.getProduct().getCropName()),
                "ORDER"
            );
        }

        return convertToDTO(order);
    }

    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setBuyerId(order.getBuyer().getId());
        dto.setBuyerCompanyName(order.getBuyer().getCompanyName());
        dto.setBuyerRepresentativeName(order.getBuyer().getRepresentativeName());
        dto.setBuyerPhone(order.getBuyer().getPhone());
        dto.setBuyerAddress(order.getBuyer().getAddress());
        if (order.getBuyer().getUser() != null) {
            dto.setBuyerProfilePhoto(order.getBuyer().getUser().getProfilePhoto());
        }
        dto.setProductId(order.getProduct().getId());
        dto.setCropName(order.getProduct().getCropName());
        dto.setVariety(order.getProduct().getVariety());
        dto.setPricePerQuintal(order.getProduct().getPricePerQuintal());
        dto.setQuantityQuintals(order.getQuantityQuintals());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
        dto.setFarmerName(order.getProduct().getFarmer().getName());
        dto.setFarmerPhone(order.getProduct().getFarmer().getPhone());
        if (order.getProduct().getFarmer().getUser() != null) {
            dto.setFarmerProfilePhoto(order.getProduct().getFarmer().getUser().getProfilePhoto());
        }
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setRazorpayOrderId(order.getRazorpayOrderId());
        dto.setRazorpayPaymentId(order.getRazorpayPaymentId());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
