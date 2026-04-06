package com.rentmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses",
        foreignKeys = @ForeignKey(entity = Property.class,
                parentColumns = "id",
                childColumns = "propertyId",
                onDelete = ForeignKey.CASCADE))
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int propertyId;
    public String description;
    public double amount;
    public long date;
    public String category;

    public Expense(int propertyId, String description, double amount, long date, String category) {
        this.propertyId = propertyId;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }
}
