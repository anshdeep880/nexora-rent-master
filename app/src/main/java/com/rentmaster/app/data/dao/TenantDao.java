package com.rentmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rentmaster.app.data.entity.Tenant;

import java.util.List;

@Dao
public interface TenantDao {
    @Insert
    long insert(Tenant tenant);

    @Update
    void update(Tenant tenant);

    @Delete
    void delete(Tenant tenant);

    @Query("SELECT * FROM tenants WHERE isActive = 1")
    LiveData<List<Tenant>> getAllActiveTenants();

    @Query("SELECT * FROM tenants")
    LiveData<List<Tenant>> getAllTenants();

    @Query("SELECT * FROM tenants")
    List<Tenant> getAllTenantsSync();

    @Query("SELECT * FROM tenants WHERE isActive = 1")
    List<Tenant> getAllActiveTenantsSync();

    @Query("SELECT * FROM tenants WHERE roomId = :roomId AND isActive = 1 LIMIT 1")
    LiveData<Tenant> getTenantForRoom(int roomId);

    @Query("SELECT * FROM tenants WHERE roomId = :roomId AND isActive = 1 LIMIT 1")
    Tenant getTenantForRoomSync(int roomId);

    @Query("SELECT name FROM tenants WHERE roomId = :roomId AND isActive = 1 LIMIT 1")
    String getTenantNameForRoomSync(int roomId);

    @Query("SELECT * FROM tenants WHERE id = :id LIMIT 1")
    LiveData<Tenant> getTenantById(int id);

    @Query("SELECT * FROM tenants WHERE id = :id LIMIT 1")
    Tenant getTenantByIdSync(int id);

    @Query("SELECT * FROM tenants WHERE isActive = :active")
    LiveData<List<Tenant>> getTenantsByCategory(boolean active);
}
