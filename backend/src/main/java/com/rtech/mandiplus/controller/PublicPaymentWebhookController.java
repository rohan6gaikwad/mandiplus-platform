package com.rtech.mandiplus.controller;

import com.rtech.mandiplus.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/public/payment", "/api/public/payment/webhook", "/api/webhook", "/webhook"})
public class PublicPaymentWebhookController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Razorpay Asynchronous Real-Time Webhook Endpoint
     * Verifies X-Razorpay-Signature header to authenticate gateway requests.
     */
    @PostMapping({"/webhook", ""})
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signatureHeader) {
        try {
            if (signatureHeader == null || signatureHeader.isEmpty()) {
                return ResponseEntity.badRequest().body("Missing webhook signature header");
            }
            paymentService.processWebhookEvent(rawPayload, signatureHeader);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (SecurityException se) {
            System.err.println("Webhook signature validation failed: " + se.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature: " + se.getMessage());
        } catch (Exception e) {
            System.err.println("Webhook processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed: " + e.getMessage());
        }
    }
}
