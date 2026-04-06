package com.rentmaster.app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Landlord;
import com.rentmaster.app.ui.viewmodel.LandlordViewModel;
import com.rentmaster.app.util.FormatUtils;

public class LandlordDetailActivity extends AppCompatActivity {
    private LandlordViewModel landlordViewModel;
    private EditText etName, etPhone1, etPhone2;
    private Landlord currentLandlord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Landlord Details");
        }

        etName = findViewById(R.id.et_landlord_name);
        etPhone1 = findViewById(R.id.et_phone1);
        etPhone2 = findViewById(R.id.et_phone2);
        Button btnSave = findViewById(R.id.btn_save_landlord);

        landlordViewModel = new ViewModelProvider(this).get(LandlordViewModel.class);

        landlordViewModel.getLandlord().observe(this, landlord -> {
            if (landlord != null) {
                currentLandlord = landlord;
                etName.setText(landlord.name);
                etPhone1.setText(landlord.phone1);
                etPhone2.setText(landlord.phone2);
            }
        });

        btnSave.setOnClickListener(v -> saveLandlord());
    }

    private void saveLandlord() {
        String name = etName.getText().toString().trim();
        String phone1 = etPhone1.getText().toString().trim();
        String phone2 = etPhone2.getText().toString().trim();

        if (name.isEmpty() || phone1.isEmpty() || phone2.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!FormatUtils.isValidAustralianPhone(phone1) || !FormatUtils.isValidAustralianPhone(phone2)) {
            Toast.makeText(this, FormatUtils.getPhoneValidationError(), Toast.LENGTH_LONG).show();
            return;
        }

        if (currentLandlord == null) {
            Landlord newLandlord = new Landlord(name, phone1, phone2);
            landlordViewModel.insert(newLandlord);
            Toast.makeText(this, "Landlord details saved", Toast.LENGTH_SHORT).show();
        } else {
            currentLandlord.name = name;
            currentLandlord.phone1 = phone1;
            currentLandlord.phone2 = phone2;
            landlordViewModel.update(currentLandlord);
            Toast.makeText(this, "Landlord details updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
