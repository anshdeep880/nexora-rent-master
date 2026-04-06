package com.rentmaster.app.data.model;

import androidx.room.Embedded;
import com.rentmaster.app.data.entity.Property;

public class PropertyWithRoomCount {
    @Embedded
    public Property property;
    
    public int totalRooms;
    public int occupiedRooms;

    public PropertyWithRoomCount() {}

    public PropertyWithRoomCount(Property property, int totalRooms, int occupiedRooms) {
        this.property = property;
        this.totalRooms = totalRooms;
        this.occupiedRooms = occupiedRooms;
    }
}
