package com.rentmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rentmaster.app.data.entity.Property;
import com.rentmaster.app.data.model.PropertyWithRoomCount;

import java.util.List;

@Dao
public interface PropertyDao {
    @Insert
    void insert(Property property);

    @Update
    void update(Property property);

    @Delete
    void delete(Property property);

    @Query("SELECT * FROM properties")
    LiveData<List<Property>> getAllProperties();

    @Query("SELECT * FROM properties WHERE id = :id")
    LiveData<Property> getPropertyById(int id);

    @Query("SELECT p.*, " +
           "(SELECT COUNT(*) FROM rooms r WHERE r.propertyId = p.id) as totalRooms, " +
           "(SELECT COUNT(*) FROM rooms r JOIN tenants t ON r.id = t.roomId WHERE r.propertyId = p.id AND t.isActive = 1) as occupiedRooms " +
           "FROM properties p")
    LiveData<List<PropertyWithRoomCount>> getPropertiesWithRoomCount();
}
