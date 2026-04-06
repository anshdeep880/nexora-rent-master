package com.rentmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import androidx.room.Index;

@Entity(tableName = "rooms",
        foreignKeys = @ForeignKey(entity = Property.class,
                parentColumns = "id",
                childColumns = "propertyId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("propertyId"))
public class Room {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int propertyId;
    public String roomNumber;
    public String roomType;
    public double baseRent;
    public double securityDeposit;

    public Room(int propertyId, String roomNumber, String roomType, double baseRent, double securityDeposit) {
        this.propertyId = propertyId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.baseRent = baseRent;
        this.securityDeposit = securityDeposit;
    }
}
