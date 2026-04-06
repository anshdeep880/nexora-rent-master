package com.rentmaster.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.entity.Property;
import com.rentmaster.app.data.model.PropertyWithRoomCount;
import com.rentmaster.app.data.repository.PropertyRepository;
import java.util.List;

public class PropertyViewModel extends AndroidViewModel {
    private PropertyRepository repository;
    private LiveData<List<Property>> allProperties;
    private LiveData<List<PropertyWithRoomCount>> allPropertiesWithRoomCount;

    public PropertyViewModel(@NonNull Application application) {
        super(application);
        repository = new PropertyRepository(application);
        allProperties = repository.getAllProperties();
        allPropertiesWithRoomCount = repository.getPropertiesWithRoomCount();
    }

    public LiveData<List<Property>> getAllProperties() {
        return allProperties;
    }

    public LiveData<Property> getPropertyById(int id) {
        return repository.getPropertyById(id);
    }

    public LiveData<List<PropertyWithRoomCount>> getPropertiesWithRoomCount() {
        return allPropertiesWithRoomCount;
    }

    public void insert(Property property) {
        repository.insert(property);
    }

    public void update(Property property) {
        repository.update(property);
    }

    public void delete(Property property) {
        repository.delete(property);
    }
}
