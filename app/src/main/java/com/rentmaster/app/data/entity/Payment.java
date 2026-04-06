package com.rentmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import androidx.room.Index;

@Entity(tableName = "payments",
        foreignKeys = {
            @ForeignKey(entity = RentRecord.class,
                parentColumns = "id",
                childColumns = "rentRecordId",
                onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Tenant.class,
                parentColumns = "id",
                childColumns = "tenantId",
                onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("rentRecordId"), @Index("tenantId")})
public class Payment {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int rentRecordId;
    public int tenantId;
    public double amount;
    public long date;
    public String mode; // Cash, Bank Transfer
    public String reference;

    public Payment(int rentRecordId, int tenantId, double amount, long date, String mode, String reference) {
        this.rentRecordId = rentRecordId;
        this.tenantId = tenantId;
        this.amount = amount;
        this.date = date;
        this.mode = mode;
        this.reference = reference;
    }
}
