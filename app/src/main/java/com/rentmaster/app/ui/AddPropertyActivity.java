package com.rentmaster.app.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.rentmaster.app.R;
import com.rentmaster.app.data.entity.Property;
import com.rentmaster.app.ui.viewmodel.PropertyViewModel;

public class AddPropertyActivity extends AppCompatActivity {
    private PropertyViewModel propertyViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Property");
        }

        propertyViewModel = new ViewModelProvider(this).get(PropertyViewModel.class);

        int propertyId = getIntent().getIntExtra("PROPERTY_ID", -1);
        EditText etName = findViewById(R.id.et_property_name);
        EditText etAddress = findViewById(R.id.et_property_address);
        EditText etType = findViewById(R.id.et_property_type);
        Button btnSave = findViewById(R.id.btn_save_property);

        if (propertyId != -1) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Property");
            propertyViewModel.getPropertyById(propertyId).observe(this, property -> {
                if (property != null) {
                    etName.setText(property.name);
                    etAddress.setText(property.address);
                    etType.setText(property.type);
                }
            });
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String type = etType.getText().toString().trim();

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Property property = new Property(name, address, type);
            if (propertyId != -1) {
                property.id = propertyId;
                propertyViewModel.update(property);
            } else {
                propertyViewModel.insert(property);
            }
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
