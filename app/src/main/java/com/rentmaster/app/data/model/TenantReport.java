package com.rentmaster.app.data.model;

public class TenantReport {
    private String tenantName;
    private String roomNumber;
    private double totalPaid;

    public TenantReport(String tenantName, String roomNumber, double totalPaid) {
        this.tenantName = tenantName;
        this.roomNumber = roomNumber;
        this.totalPaid = totalPaid;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public double getTotalPaid() {
        return totalPaid;
    }
}
