package com.rentmaster.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.rentmaster.app.data.dao.ExpenseDao;
import com.rentmaster.app.data.dao.PaymentDao;
import com.rentmaster.app.data.dao.PropertyDao;
import com.rentmaster.app.data.dao.RentRecordDao;
import com.rentmaster.app.data.dao.RoomDao;
import com.rentmaster.app.data.dao.TenantDao;
import com.rentmaster.app.data.dao.LandlordDao;
import com.rentmaster.app.data.entity.Expense;
import com.rentmaster.app.data.entity.Landlord;
import com.rentmaster.app.data.entity.Payment;
import com.rentmaster.app.data.entity.Property;
import com.rentmaster.app.data.entity.RentRecord;
import com.rentmaster.app.data.entity.Tenant;

@Database(entities = { Property.class, com.rentmaster.app.data.entity.Room.class, Tenant.class, RentRecord.class,
        Payment.class, Expense.class, com.rentmaster.app.data.entity.Deposit.class,
        Landlord.class }, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract PropertyDao propertyDao();

    public abstract RoomDao roomDao();

    public abstract TenantDao tenantDao();

    public abstract RentRecordDao rentRecordDao();

    public abstract PaymentDao paymentDao();

    public abstract ExpenseDao expenseDao();

    public abstract com.rentmaster.app.data.dao.DepositDao depositDao();

    public abstract LandlordDao landlordDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "rent_master_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void resetDatabase(final Context context) {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
        context.deleteDatabase("rent_master_db");
    }
}
