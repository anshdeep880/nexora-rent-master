package com.rentmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "deposits",
        foreignKeys = @ForeignKey(entity = Tenant.class,
                parentColumns = "id",
                childColumns = "tenantId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("tenantId"))
public class Deposit {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int tenantId;
    public double amount;
    public long date;
    public String notes;
    public boolean refunded;

    public Deposit(int tenantId, double amount, long date, String notes, boolean refunded) {
        this.tenantId = tenantId;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.refunded = refunded;
    }
}
