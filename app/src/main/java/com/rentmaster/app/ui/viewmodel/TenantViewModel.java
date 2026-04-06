package com.rentmaster.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.rentmaster.app.data.entity.Tenant;
import com.rentmaster.app.data.model.TenantReport;
import com.rentmaster.app.data.repository.TenantRepository;
import java.util.List;

public class TenantViewModel extends AndroidViewModel {
    private TenantRepository repository;
    private LiveData<List<Tenant>> allActiveTenants;

    public TenantViewModel(@NonNull Application application) {
        super(application);
        repository = new TenantRepository(application);
        allActiveTenants = repository.getAllActiveTenants();
    }

    public LiveData<List<Tenant>> getAllActiveTenants() {
        return allActiveTenants;
    }

    public LiveData<List<Tenant>> getTenantsByCategory(boolean active) {
        return repository.getTenantsByCategory(active);
    }

    public LiveData<Tenant> getTenantForRoom(int roomId) {
        return repository.getTenantForRoom(roomId);
    }

    public LiveData<Tenant> getTenantById(int id) {
        return repository.getTenantById(id);
    }

    public LiveData<List<TenantReport>> getTenantsWithTotalPayments() {
        return repository.getTenantsWithTotalPayments();
    }

    public void insert(Tenant tenant) {
        repository.insert(tenant);
    }

    public void insertWithInitialRent(Tenant tenant, double baseRent) {
        repository.insertWithInitialRent(tenant, baseRent);
    }

    public void update(Tenant tenant) {
        repository.update(tenant);
    }

    public void delete(Tenant tenant) {
        repository.delete(tenant);
    }
}
