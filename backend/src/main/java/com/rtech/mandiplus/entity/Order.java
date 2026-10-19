package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_quintals", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityQuintals;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 50)
    private String status = "PENDING"; // 'PENDING', 'APPROVED', 'SHIPPED', 'DELIVERED', 'CANCELLED'

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "RAZORPAY";

    @Column(name = "payment_status", length = 50)
    private String paymentStatus = "ESCROW_HOLD";

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Order() {
    }

    public Order(Long id, Buyer buyer, Product product, BigDecimal quantityQuintals,
                 BigDecimal totalPrice, String status, LocalDate estimatedDeliveryDate,
                 LocalDateTime createdAt) {
        this.id = id;
        this.buyer = buyer;
        this.product = product;
        this.quantityQuintals = quantityQuintals;
        this.totalPrice = totalPrice;
        this.status = status;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getQuantityQuintals() {
        return quantityQuintals;
    }

    public void setQuantityQuintals(BigDecimal quantityQuintals) {
        this.quantityQuintals = quantityQuintals;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public void transitionTo(OrderStatus targetStatus, String role) {
        OrderStatus currentStatusEnum = OrderStatus.valueOf(this.status);
        if (!OrderStatus.isValidTransition(currentStatusEnum, targetStatus, role)) {
            throw new RuntimeException(String.format("Error: Unauthorized or invalid state transition from %s to %s for role %s", 
                this.status, targetStatus, role));
        }
        
        // Handle inventory side-effects of transition
        if ((targetStatus == OrderStatus.REJECTED || targetStatus == OrderStatus.CANCELLED) && 
                currentStatusEnum != OrderStatus.REJECTED && currentStatusEnum != OrderStatus.CANCELLED) {
            if (this.product != null && this.quantityQuintals != null) {
                this.product.restoreQuantity(this.quantityQuintals);
            }
        }
        
        this.status = targetStatus.name();
    }

    public static Order createOrder(Buyer buyer, Product product, BigDecimal quantityQuintals) {
        product.deductQuantity(quantityQuintals);
        BigDecimal total = product.getPricePerQuintal().multiply(quantityQuintals);
        Order order = new Order();
        order.setBuyer(buyer);
        order.setProduct(product);
        order.setQuantityQuintals(quantityQuintals);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.PENDING.name());
        return order;
    }

    public void cancelOrder() {
        if ("CANCELLED".equals(this.status)) {
            throw new IllegalStateException("Order is already cancelled!");
        }
        transitionTo(OrderStatus.CANCELLED, "BUYER");
    }
}
