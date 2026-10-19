package com.rtech.mandiplus.dto;

public class AdminStatsDTO {
    private long totalUsers;
    private long totalFarmers;
    private long totalBuyers;
    private long totalProducts;
    private long pendingProducts;
    private long totalOrders;
    private long totalComplaints;

    public AdminStatsDTO() {
    }

    public AdminStatsDTO(long totalUsers, long totalFarmers, long totalBuyers, long totalProducts, long pendingProducts, long totalOrders, long totalComplaints) {
        this.totalUsers = totalUsers;
        this.totalFarmers = totalFarmers;
        this.totalBuyers = totalBuyers;
        this.totalProducts = totalProducts;
        this.pendingProducts = pendingProducts;
        this.totalOrders = totalOrders;
        this.totalComplaints = totalComplaints;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalFarmers() {
        return totalFarmers;
    }

    public void setTotalFarmers(long totalFarmers) {
        this.totalFarmers = totalFarmers;
    }

    public long getTotalBuyers() {
        return totalBuyers;
    }

    public void setTotalBuyers(long totalBuyers) {
        this.totalBuyers = totalBuyers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getPendingProducts() {
        return pendingProducts;
    }

    public void setPendingProducts(long pendingProducts) {
        this.pendingProducts = pendingProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }
}
