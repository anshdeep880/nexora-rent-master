package com.rentmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rentmaster.app.data.entity.Room;

import java.util.List;

@Dao
public interface RoomDao {
    @Insert
    void insert(Room room);

    @Update
    void update(Room room);

    @Delete
    void delete(Room room);

    @Query("SELECT * FROM rooms WHERE propertyId = :propertyId")
    LiveData<List<Room>> getRoomsForProperty(int propertyId);

    @Query("SELECT * FROM rooms WHERE propertyId = :propertyId")
    List<Room> getRoomsForPropertySync(int propertyId);

    @Query("SELECT * FROM rooms WHERE id = :id LIMIT 1")
    Room getRoomSync(int id);

    @Query("SELECT * FROM rooms WHERE id = :id LIMIT 1")
    LiveData<Room> getRoomById(int id);

    @Query("SELECT * FROM rooms WHERE id = :id LIMIT 1")
    Room getRoomByIdSync(int id);

    @Query("SELECT * FROM rooms")
    LiveData<List<Room>> getAllRooms();

    @Query("SELECT * FROM rooms")
    List<Room> getAllRoomsSync();

    @Query("SELECT r.roomNumber FROM rooms r INNER JOIN tenants t ON t.roomId = r.id WHERE t.id = :tenantId")
    String getRoomNumberForTenantSync(int tenantId);

    // Get rooms that have active tenants (Occupied)
    @Query("SELECT r.* FROM rooms r INNER JOIN tenants t ON t.roomId = r.id WHERE t.isActive = 1")
    LiveData<List<Room>> getOccupiedRooms();

    // Get rooms that do NOT have active tenants (Vacant)
    @Query("SELECT * FROM rooms WHERE id NOT IN (SELECT roomId FROM tenants WHERE roomId IS NOT NULL AND isActive = 1)")
    LiveData<List<Room>> getVacantRooms();
}
