package com.rentmaster.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.entity.Landlord;
import com.rentmaster.app.data.repository.LandlordRepository;

public class LandlordViewModel extends AndroidViewModel {
    private LandlordRepository repository;
    private LiveData<Landlord> landlord;

    public LandlordViewModel(@NonNull Application application) {
        super(application);
        repository = new LandlordRepository(application);
        landlord = repository.getLandlord();
    }

    public LiveData<Landlord> getLandlord() {
        return landlord;
    }

    public void insert(Landlord landlord) {
        repository.insert(landlord);
    }

    public void update(Landlord landlord) {
        repository.update(landlord);
    }
}
