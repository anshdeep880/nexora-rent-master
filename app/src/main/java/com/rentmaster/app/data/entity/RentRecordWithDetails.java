package com.rentmaster.app.data.entity;

import androidx.room.Embedded;

public class RentRecordWithDetails {
    @Embedded
    public RentRecord rentRecord;

    public String tenantName;
    public String roomNumber;
    public boolean isActive;
    public int propertyId;
    public String propertyName;
}
