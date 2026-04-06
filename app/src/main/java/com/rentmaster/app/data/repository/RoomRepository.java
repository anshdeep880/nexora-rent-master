package com.rentmaster.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.dao.RentRecordDao;
import com.rentmaster.app.data.dao.RoomDao;
import com.rentmaster.app.data.dao.TenantDao;
import com.rentmaster.app.data.entity.Room;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.data.model.RoomReport;

import java.util.ArrayList;
import java.util.List;

public class RoomRepository {
    private AppDatabase db;
    private RoomDao roomDao;
    private TenantDao tenantDao;
    private RentRecordDao rentRecordDao;

    public RoomRepository(Application application) {
        db = AppDatabase.getDatabase(application);
        roomDao = db.roomDao();
        tenantDao = db.tenantDao();
        rentRecordDao = db.rentRecordDao();
    }

    public LiveData<List<Room>> getRoomsForProperty(int propertyId) {
        return roomDao.getRoomsForProperty(propertyId);
    }

    public LiveData<Room> getRoomById(int id) {
        return roomDao.getRoomById(id);
    }

    public LiveData<List<Room>> getOccupiedRooms() {
        return roomDao.getOccupiedRooms();
    }

    public LiveData<List<RoomReport>> getRoomsWithOutstandingRent() {
        MediatorLiveData<List<RoomReport>> roomReports = new MediatorLiveData<>();

        Runnable refreshReports = () -> {
            PropertyRepository.databaseWriteExecutor.execute(() -> {
                List<Room> rooms = roomDao.getAllRoomsSync();
                List<RoomReport> reports = new ArrayList<>();
                for (Room room : rooms) {
                    double outstandingRent = rentRecordDao.getOutstandingRentForRoomSync(room.id);
                    String tenantName = tenantDao.getTenantNameForRoomSync(room.id);
                    reports.add(new RoomReport(room.roomNumber, tenantName, outstandingRent));
                }
                roomReports.postValue(reports);
            });
        };

        roomReports.addSource(roomDao.getAllRooms(), rooms -> refreshReports.run());
        roomReports.addSource(rentRecordDao.getTotalPaid(), total -> refreshReports.run());

        return roomReports;
    }

    public void insert(Room room) {
        PropertyRepository.databaseWriteExecutor.execute(() -> roomDao.insert(room));
    }

    public void update(Room room) {
        PropertyRepository.databaseWriteExecutor.execute(() -> roomDao.update(room));
    }

    public void delete(Room room) {
        PropertyRepository.databaseWriteExecutor.execute(() -> {
            db.runInTransaction(() -> {
                Tenant tenant = tenantDao.getTenantForRoomSync(room.id);
                if (tenant != null) {
                    tenantDao.delete(tenant);
                }
                roomDao.delete(room);
            });
        });
    }
}
