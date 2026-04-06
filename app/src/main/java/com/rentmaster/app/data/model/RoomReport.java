package com.rentmaster.app.data.model;

public class RoomReport {
    private String roomNumber;
    private String tenantName;
    private double outstandingRent;

    public RoomReport(String roomNumber, String tenantName, double outstandingRent) {
        this.roomNumber = roomNumber;
        this.tenantName = tenantName;
        this.outstandingRent = outstandingRent;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getTenantName() {
        return tenantName;
    }

    public double getOutstandingRent() {
        return outstandingRent;
    }
}
