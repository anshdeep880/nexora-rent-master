package com.rentmaster.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.entity.Room;
import com.rentmaster.app.data.model.RoomReport;
import com.rentmaster.app.data.repository.RoomRepository;
import java.util.List;

public class RoomViewModel extends AndroidViewModel {
    private RoomRepository repository;

    public RoomViewModel(@NonNull Application application) {
        super(application);
        repository = new RoomRepository(application);
    }

    public LiveData<List<Room>> getRoomsForProperty(int propertyId) {
        return repository.getRoomsForProperty(propertyId);
    }

    public LiveData<Room> getRoomById(int id) {
        return repository.getRoomById(id);
    }

    public LiveData<List<Room>> getOccupiedRooms() {
        return repository.getOccupiedRooms();
    }

    public LiveData<List<RoomReport>> getRoomsWithOutstandingRent() {
        return repository.getRoomsWithOutstandingRent();
    }

    public void insert(Room room) {
        repository.insert(room);
    }

    public void update(Room room) {
        repository.update(room);
    }

    public void delete(Room room) {
        repository.delete(room);
    }
}
