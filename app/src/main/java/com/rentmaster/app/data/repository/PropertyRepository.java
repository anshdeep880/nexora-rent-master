package com.rentmaster.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.dao.PropertyDao;
import com.rentmaster.app.data.dao.RoomDao;
import com.rentmaster.app.data.dao.TenantDao;
import com.rentmaster.app.data.entity.Property;
import com.rentmaster.app.data.entity.Room;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.data.model.PropertyWithRoomCount;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PropertyRepository {
    private AppDatabase db;
    private PropertyDao propertyDao;
    private RoomDao roomDao;
    private TenantDao tenantDao;
    private LiveData<List<Property>> allProperties;
    private LiveData<List<PropertyWithRoomCount>> allPropertiesWithRoomCount;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public PropertyRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        propertyDao = db.propertyDao();
        roomDao = db.roomDao();
        tenantDao = db.tenantDao();
        allProperties = propertyDao.getAllProperties();
        allPropertiesWithRoomCount = propertyDao.getPropertiesWithRoomCount();
    }

    public LiveData<List<Property>> getAllProperties() {
        return allProperties;
    }

    public LiveData<Property> getPropertyById(int id) {
        return propertyDao.getPropertyById(id);
    }

    public LiveData<List<PropertyWithRoomCount>> getPropertiesWithRoomCount() {
        return allPropertiesWithRoomCount;
    }

    public void insert(Property property) {
        databaseWriteExecutor.execute(() -> {
            propertyDao.insert(property);
        });
    }

    public void update(Property property) {
        databaseWriteExecutor.execute(() -> {
            propertyDao.update(property);
        });
    }

    public void delete(Property property) {
        databaseWriteExecutor.execute(() -> {
            db.runInTransaction(() -> {
                List<Room> rooms = roomDao.getRoomsForPropertySync(property.id);
                for (Room room : rooms) {
                    Tenant tenant = tenantDao.getTenantForRoomSync(room.id);
                    if (tenant != null) {
                        tenantDao.delete(tenant);
                    }
                    roomDao.delete(room);
                }
                propertyDao.delete(property);
            });
        });
    }
}
