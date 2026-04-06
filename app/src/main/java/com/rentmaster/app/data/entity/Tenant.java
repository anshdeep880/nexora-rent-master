package com.rentmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import androidx.room.Index;

@Entity(tableName = "tenants",
        foreignKeys = @ForeignKey(entity = Room.class,
                parentColumns = "id",
                childColumns = "roomId",
                onDelete = ForeignKey.SET_NULL),
        indices = {@Index("roomId")})
public class Tenant {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public Integer roomId; // Nullable if vacated
    public String name;
    public String phone;
    public String notes;
    public double securityDeposit;
    public boolean isActive;

    public String rentFrequency; // "Weekly" or "Fortnightly"
    public long startDate; // Timestamp when tenant started

    public Tenant(String name, String phone, String notes, double securityDeposit, boolean isActive, String rentFrequency, long startDate) {
        this.name = name;
        this.phone = phone;
        this.notes = notes;
        this.securityDeposit = securityDeposit;
        this.isActive = isActive;
        this.rentFrequency = rentFrequency;
        this.startDate = startDate;
    }
}
