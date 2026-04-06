package com.rentmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "landlords")
public class Landlord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String name;
    public String phone1;
    public String phone2;

    public Landlord() {}

    public Landlord(String name, String phone1, String phone2) {
        this.name = name;
        this.phone1 = phone1;
        this.phone2 = phone2;
    }
}
