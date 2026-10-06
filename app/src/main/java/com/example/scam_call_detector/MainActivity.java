package com.example.scam_call_detector;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scam_call_detector.databinding.ActivityMainBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    BottomNavigationView bottomNav;

    private static final String TAG = "MainActivity";
    //Button submit;'
    private FirebaseFirestore db;

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

            return false;
        });

        db = FirebaseFirestore.getInstance();
        getAllNumbers();
//        findViewById(R.id.button_test).setOnClickListener(view -> addMarlaSinger());
        //addMarlaSinger();
        //addTylerDurden();
        //getAllNumbers();
    }

    private void switchActivities(){
        Intent switchActivityIntent = new Intent(this, ReportPhoneNumber.class);
        startActivity(switchActivityIntent);
    }

    private void contactAdmin(){
        Intent contactAdminIntent = new Intent(this, ContactAdmin.class);
        startActivity(contactAdminIntent);
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

//    public void addMarlaSinger(){
//        Map<String, Object> phone_numbers = new HashMap<>();
//        phone_numbers.put("phoneNumber", "800-555-0143");
//        phone_numbers.put("location", "Business");
//        phone_numbers.put("scamType", "health/life");
//        phone_numbers.put("nameAttached", "Marla Singer");
//        phone_numbers.put("other", "scams people out of their health insurance");
//
//        db.collection("phone_numbers")
//                .document()
//                .set(phone_numbers)
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Log.d(TAG, "write:onComplete");
//                        if(!task.isSuccessful()){
//                            Log.w(TAG, "write:onComplete:failed", task.getException());
//                            Snackbar.make(findViewById(R.id.button_test), "Unsuccessful " + task.getException(), Snackbar.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
    public void addTylerDurden(){
        Map<String, Object> phone_numbers = new HashMap<>();
        phone_numbers.put("phoneNumber", "800-555-0153");
        phone_numbers.put("location", "Business");
        phone_numbers.put("scamType", "money");
        phone_numbers.put("nameAttached", "Tyler Durden");
        phone_numbers.put("other", "wants to sell soap");

        db.collection("phone_numbers")
                .document()
                .set(phone_numbers)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "write:onComplete");
                        if(!task.isSuccessful()){
                            Log.w(TAG, "write:onComplete:failed", task.getException());
                        }
                    }
                });
    }

    public void getAllNumbers() {
        // [START get_all_users]
        db.collection("phone_numbers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            LinearLayout container = findViewById(R.id.scam_numbers_container);
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