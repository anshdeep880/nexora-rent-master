package com.rentmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "properties")
public class Property {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String address;
    public String type; // House, Building, etc.

    public Property(String name, String address, String type) {
        this.name = name;
        this.address = address;
        this.type = type;
    }
}
