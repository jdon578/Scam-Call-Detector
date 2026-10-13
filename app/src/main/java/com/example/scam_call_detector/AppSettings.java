package com.example.scam_call_detector;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.scam_call_detector.databinding.ActivityAppSettingsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AppSettings extends AppCompatActivity {
    BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityAppSettingsBinding binding = ActivityAppSettingsBinding .inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.fab.setOnClickListener(
                view -> contactAdmin());

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.action_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.go_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;

            } else if (id == R.id.report_phone_number) {
                startActivity(new Intent(this, ReportPhoneNumber.class));
                return true;

            } else if (id == R.id.action_settings) {
                return true;
            }

            return false;
        });

//        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.view_scam_numbers).setOnClickListener(
                v -> scamNumbers());

    }

    private void scamNumbers(){
        Intent scamNumbersIntent = new Intent(this, ViewScamNumbers.class);
        startActivity(scamNumbersIntent);
    }

    private void contactAdmin(){
        Intent contactAdminIntent = new Intent(this, ContactAdmin.class);
        startActivity(contactAdminIntent);
    }
}