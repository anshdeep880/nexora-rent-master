package com.rentmaster.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.rentmaster.app.data.entity.Landlord;
import java.util.List;

@Dao
public interface LandlordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Landlord landlord);

    @Update
    void update(Landlord landlord);

    @Delete
    void delete(Landlord landlord);

    @Query("SELECT * FROM landlords LIMIT 1")
    LiveData<Landlord> getLandlord();

    @Query("SELECT * FROM landlords LIMIT 1")
    Landlord getLandlordSync();
}
