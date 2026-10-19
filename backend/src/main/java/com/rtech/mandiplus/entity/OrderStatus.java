package com.rtech.mandiplus.entity;

public enum OrderStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    /**
     * Domain logic to validate if an order can transit from current status to target status 
     * based on the security role of the authenticated caller.
     */
    public static boolean isValidTransition(OrderStatus current, OrderStatus target, String role) {
        if (current == null || target == null) {
            return false;
        }

        if (current == target) {
            return true;
        }

        // Final terminal states cannot be changed by any role
        if (current == DELIVERED || current == REJECTED || current == CANCELLED) {
            return false;
        }

        if ("ADMIN".equals(role)) {
            if (target == CANCELLED) {
                return true;
            }
            if (current == PENDING) {
                return target == APPROVED || target == REJECTED;
            }
            return false;
        }

        if ("FARMER".equals(role)) {
            if (current == PENDING) {
                return target == APPROVED || target == REJECTED;
            }
            if (current == APPROVED) {
                return target == SHIPPED;
            }
            return false;
        }

        if ("BUYER".equals(role)) {
            if (current == PENDING) {
                return target == CANCELLED;
            }
            return false;
        }

        if ("DELIVERY_PARTNER".equals(role)) {
            if (current == SHIPPED) {
                return target == OUT_FOR_DELIVERY;
            }
            if (current == OUT_FOR_DELIVERY) {
                return target == DELIVERED;
            }
            return false;
        }

        return false;
    }
}
