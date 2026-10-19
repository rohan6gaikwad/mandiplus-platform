package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.dto.CreatePaymentOrderRequest;
import com.rtech.mandiplus.dto.OrderDTO;
import com.rtech.mandiplus.dto.PaymentDTO;
import com.rtech.mandiplus.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/buyer/payment", "/api/payment"})
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Create Razorpay Order
     * Accepts either a JSON body with product IDs/quantities (for server-side pricing validation)
     * or a fallback amount query parameter for compatibility.
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createRazorpayOrder(
            @RequestBody(required = false) CreatePaymentOrderRequest bodyRequest,
            @RequestParam(required = false) BigDecimal amount,
            Authentication authentication) {
        try {
            CreatePaymentOrderRequest request = bodyRequest != null ? bodyRequest : new CreatePaymentOrderRequest();
            if (request.getAmount() == null && amount != null) {
                request.setAmount(amount);
            }
            Map<String, Object> response = paymentService.createRazorpayOrder(request, authentication.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Razorpay order creation failed: " + e.getMessage());
        }
    }

    /**
     * Verify Payment and Fulfill Orders with Real-Time WebSocket Alerts
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody PaymentVerificationRequest request,
            Authentication authentication) {
        try {
            OrderDTO placedOrder = paymentService.verifyAndProcessPayment(
                    request.getRazorpayPaymentId(),
                    request.getRazorpayOrderId(),
                    request.getRazorpaySignature(),
                    request.getPaymentMethod(),
                    request.getOrderDTO(),
                    request.getOrders(),
                    authentication.getName()
            );
            return ResponseEntity.ok(placedOrder);
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body("Payment security check failed: " + se.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Payment processing failed: " + e.getMessage());
        }
    }

    /**
     * Buyer's Complete Transaction and Escrow Ledger
     */
    @GetMapping("/history")
    public ResponseEntity<List<PaymentDTO>> getPaymentHistory(Authentication authentication) {
        return ResponseEntity.ok(paymentService.getBuyerPaymentHistory(authentication.getName()));
    }

    /**
     * Payment record lookup for a specific order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsForOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsForOrder(orderId));
    }

    public static class PaymentVerificationRequest {
        private String razorpayPaymentId;
        private String razorpayOrderId;
        private String razorpaySignature;
        private String paymentMethod; // "RAZORPAY" | "COD"
        private OrderDTO orderDTO;
        private List<OrderDTO> orders;

        public PaymentVerificationRequest() {}

        public String getRazorpayPaymentId() {
            return razorpayPaymentId;
        }

        public void setRazorpayPaymentId(String razorpayPaymentId) {
            this.razorpayPaymentId = razorpayPaymentId;
        }

        public String getRazorpayOrderId() {
            return razorpayOrderId;
        }

        public void setRazorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
        }

        public String getRazorpaySignature() {
            return razorpaySignature;
        }

        public void setRazorpaySignature(String razorpaySignature) {
            this.razorpaySignature = razorpaySignature;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public OrderDTO getOrderDTO() {
            return orderDTO;
        }

        public void setOrderDTO(OrderDTO orderDTO) {
            this.orderDTO = orderDTO;
        }

        public List<OrderDTO> getOrders() {
            return orders;
        }

        public void setOrders(List<OrderDTO> orders) {
            this.orders = orders;
        }
    }
}
