package com.rentmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import androidx.room.Index;

@Entity(tableName = "rent_records",
        foreignKeys = {
                @ForeignKey(entity = Tenant.class,
                        parentColumns = "id",
                        childColumns = "tenantId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Room.class,
                        parentColumns = "id",
                        childColumns = "roomId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("tenantId"), @Index("roomId")})
public class RentRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int tenantId;
    public int roomId;
    public int month;
    public int year;
    public double amountDue;
    public double amountPaid;
    public String status; // Paid, Unpaid, Partial
    public long dueDate;

    public long periodStart;
    public long periodEnd;

    public RentRecord(int tenantId, int roomId, int month, int year, double amountDue, long dueDate, long periodStart, long periodEnd) {
        this.tenantId = tenantId;
        this.roomId = roomId;
        this.month = month;
        this.year = year;
        this.amountDue = amountDue;
        this.dueDate = dueDate;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.amountPaid = 0;
        this.status = "Unpaid";
    }

    // Legacy constructor for migration or backward compatibility (optional)
    // You might want to remove this if you strictly migrate everything
    @androidx.room.Ignore
    public RentRecord(int tenantId, int roomId, int month, int year, double amountDue, long dueDate) {
        this(tenantId, roomId, month, year, amountDue, dueDate, 0, 0);
    }
}
