package com.example.scam_call_detector;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.scam_call_detector.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private AppBarConfiguration appBarConfiguration;
    BottomNavigationView bottomNav;

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;
    private static final int REQUEST_CALL_SCREENING_ROLE = 1;

    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
        /*bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnClickListener(view -> switchActivities());*/
        /*binding.fab.setOnClickListener(
                view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show());*/

        binding.fab.setOnClickListener(
                view -> contactAdmin());

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.go_home);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.report_phone_number) {
                switchActivities();
                return true;
            }

            if (item.getItemId() == R.id.go_home) {
                return true;
            }

            if (item.getItemId() == R.id.action_settings){
                switchToSettings();
                return true;
            }

            return false;
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshScreen();
            }
        });

        db = FirebaseFirestore.getInstance();
        //getAllNumbers();
        getCallLogs();
        requestCallScreeningRole();
        requestPermissions();
        requestOverlayPermission();
    }

    private void refreshScreen() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
            @Override
            public void run() {
                getCallLogs();
                Toast.makeText(MainActivity.this, "Refreshed", Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }

    private void switchActivities(){
        Intent switchActivityIntent = new Intent(this, ReportPhoneNumber.class);
        startActivity(switchActivityIntent);
    }

    private void switchToReportAuto(String phoneNumber){
        Intent reportIntent = new Intent(this, ReportPhoneNumber.class);
        reportIntent.putExtra("phoneNumber", phoneNumber);
        startActivity(reportIntent);
    }

    private void contactAdmin(){
        Intent contactAdminIntent = new Intent(this, ContactAdmin.class);
        startActivity(contactAdminIntent);
    }

    private void switchToSettings(){
        Intent settingsIntent = new Intent(this, Settings.class);
        startActivity(settingsIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            /*Intent myIntent = new Intent(this, ReportPhoneNumber.class);
            startActivity(myIntent);*/
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        boolean handled = false;
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            handled = NavigationUI.navigateUp(navController, appBarConfiguration);
        }
        return handled || super.onSupportNavigateUp();
    }

    private void requestCallScreeningRole() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);

            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {

                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);

                startActivityForResult(intent, REQUEST_CALL_SCREENING_ROLE);
            }
        }
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Overlay permission is already granted", Toast.LENGTH_SHORT).show();
        }
    }


    private void requestPermissions() {
        // from GeeksforGeeks
        String[] permissions = {
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE
        };

        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()){
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        } else {
            Toast.makeText(this, "All permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        // from GeeksforGeeks
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE){
            List<String> deniedPermissions = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    deniedPermissions.add(permissions[i]);
                }
            }

            if (deniedPermissions.isEmpty()){
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied: "  + deniedPermissions, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getCallLogs(){
        Uri callUri = CallLog.Calls.CONTENT_URI;

        String[] callLogs = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };

        ContentResolver contentResolver = getContentResolver();

        LinearLayout container = findViewById(R.id.call_log_container);
        container.removeAllViews();

        try (Cursor cursor = contentResolver.query(callUri, callLogs, null, null, null)){
            if (cursor == null){
                Log.e(TAG, "Cursor is null");
                return;
            }
            int numberID = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int typeID = cursor.getColumnIndex(CallLog.Calls.TYPE);
            int dateID = cursor.getColumnIndex(CallLog.Calls.DATE);
            int durationID = cursor.getColumnIndex(CallLog.Calls.DURATION);

            while(cursor.moveToNext()){
                String phoneNumber = cursor.getString(numberID);
                String callType = cursor.getString(typeID);
                String callDate = cursor.getString(dateID);
                String callDuration = cursor.getString(durationID);

                Date callDayTime = new Date(Long.parseLong(callDate));

                String dir = "UNKNOWN";
                int dircode = Integer.parseInt(callType);
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                    case CallLog.Calls.REJECTED_TYPE:
                        dir = "REJECTED";
                        break;
                }

                com.google.android.material.card.MaterialCardView card =
                        new com.google.android.material.card.MaterialCardView(
                                MainActivity.this);
                LinearLayout layout =
                        new LinearLayout(MainActivity.this);

                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
                layout.setPadding(20, 20, 20, 20);

                TextView phoneText = new TextView(MainActivity.this);
                phoneText.setText(phoneNumber);
                phoneText.setTextSize(18);

                TextView typeText = new TextView(MainActivity.this);
                typeText.setText("Type: " + callType);
                typeText.setTextSize(18);

                TextView dateText  = new TextView(MainActivity.this);
                dateText.setText("Date: " + callDayTime);
                dateText.setTextSize(18);

                TextView durationText  = new TextView(MainActivity.this);
                durationText.setText("Duration: " + callDuration);
                durationText.setTextSize(18);

                // change to ONLY give option if scam
                // if it's in the contact list, or unknown, do not add
                Button goToReport = new Button(MainActivity.this);
                goToReport.setText("Report");
                goToReport.setTextSize(16);
                goToReport.setOnClickListener(v -> switchToReportAuto(phoneNumber));

                layout.addView(phoneText);
                layout.addView(typeText);
                layout.addView(dateText);
                layout.addView(durationText);
                layout.addView(goToReport);

                card.addView(layout);

                LinearLayout.LayoutParams cardParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);

                cardParams.setMargins(0, 0, 0, 25);
                card.setLayoutParams(cardParams);
                container.addView(card, 0);
            }
        } catch (Exception e){
            Log.e(TAG, "Error getting call logs", e);
        }
    }

    public void getAllNumbers() {
        // [START get_all_users]
        db.collection("phone_numbers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            LinearLayout container = findViewById(R.id.call_log_container);
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String phoneNumber = document.getString("phoneNumber");
                                String location = document.getString("location");
                                String scamType = document.getString("scamType");
                                String nameAttached = document.getString("nameAttached");
                                String other = document.getString("other");

                                com.google.android.material.card.MaterialCardView card =
                                        new com.google.android.material.card.MaterialCardView(
                                                MainActivity.this);
                                LinearLayout layout =
                                        new LinearLayout(MainActivity.this);

                                layout.setOrientation(LinearLayout.VERTICAL);
                                layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
                                layout.setPadding(20, 20, 20, 20);

                                TextView phoneText = new TextView(MainActivity.this);
                                phoneText.setText(phoneNumber);
                                phoneText.setTextSize(18);

                                TextView nameText = new TextView(MainActivity.this);
                                nameText.setText("Name: " + nameAttached);
                                nameText.setTextSize(18);

                                TextView typeText  = new TextView(MainActivity.this);
                                typeText.setText("Scam Type: "+ scamType);
                                typeText.setTextSize(18);

                                TextView locationText  = new TextView(MainActivity.this);
                                locationText .setText("location: " + location);
                                locationText .setTextSize(18);

                                TextView otherText  = new TextView(MainActivity.this);
                                otherText.setText("other: " + other);
                                otherText.setTextSize(18);

                                layout.addView(phoneText);
                                layout.addView(nameText);
                                layout.addView(typeText);
                                layout.addView(locationText);
                                layout.addView(otherText);

                                card.addView(layout);

                                LinearLayout.LayoutParams cardParams =
                                        new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT);

                                cardParams.setMargins(0, 0, 0, 25);
                                card.setLayoutParams(cardParams);
                                container.addView(card);

                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
        // [END get_all_users]
    }
}