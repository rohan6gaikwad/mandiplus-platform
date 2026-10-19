package com.rtech.mandiplus.dto;

import java.math.BigDecimal;
import java.util.List;

public class CreatePaymentOrderRequest {

    private Long productId;
    private BigDecimal quantityQuintals;
    private List<CartItemDTO> cartItems;
    private Long storageBookingId;
    private BigDecimal amount;
    private String purpose = "PRODUCE_PURCHASE"; // PRODUCE_PURCHASE | STORAGE_RESERVATION

    public CreatePaymentOrderRequest() {}

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantityQuintals() {
        return quantityQuintals;
    }

    public void setQuantityQuintals(BigDecimal quantityQuintals) {
        this.quantityQuintals = quantityQuintals;
    }

    public List<CartItemDTO> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }

    public Long getStorageBookingId() {
        return storageBookingId;
    }

    public void setStorageBookingId(Long storageBookingId) {
        this.storageBookingId = storageBookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public static class CartItemDTO {
        private Long productId;
        private BigDecimal quantityQuintals;

        public CartItemDTO() {}

        public CartItemDTO(Long productId, BigDecimal quantityQuintals) {
            this.productId = productId;
            this.quantityQuintals = quantityQuintals;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public BigDecimal getQuantityQuintals() {
            return quantityQuintals;
        }

        public void setQuantityQuintals(BigDecimal quantityQuintals) {
            this.quantityQuintals = quantityQuintals;
        }
    }
}
