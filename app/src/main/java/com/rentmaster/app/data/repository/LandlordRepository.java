package com.rentmaster.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.AppDatabase;
import com.rentmaster.app.data.dao.LandlordDao;
import com.rentmaster.app.data.entity.Landlord;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LandlordRepository {
    private LandlordDao landlordDao;
    private LiveData<Landlord> landlord;
    private ExecutorService executorService;

    public LandlordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        landlordDao = db.landlordDao();
        landlord = landlordDao.getLandlord();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Landlord> getLandlord() {
        return landlord;
    }

    public Landlord getLandlordSync() {
        return landlordDao.getLandlordSync();
    }

    public void insert(Landlord landlord) {
        executorService.execute(() -> landlordDao.insert(landlord));
    }

    public void update(Landlord landlord) {
        executorService.execute(() -> landlordDao.update(landlord));
    }
}
