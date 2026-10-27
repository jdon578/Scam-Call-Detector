package com.example.scam_call_detector;

import static com.google.common.base.Predicates.equalTo;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.scam_call_detector.databinding.ActivityReportPhoneNumberBinding;
import com.twilio.http.HttpClient;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReportPhoneNumber extends AppCompatActivity {

    private static final String TAG = "ReportPhoneNumber";
    //Button submit;'
    private FirebaseFirestore db;
    BottomNavigationView bottomNav;

    EditText phoneNumberEdit;
    Spinner numberTypeEdit;
    Spinner scamTypeEdit;
    EditText otherEdit;

    private final OkHttpClient client = new OkHttpClient();
    private static final String API_TOKEN = "6mxdtn5wck20glbyz4j1pq3s7e9fio";
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityReportPhoneNumberBinding  binding = ActivityReportPhoneNumberBinding .inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.fab.setOnClickListener(
                view -> contactAdmin());

        bottomNav = findViewById(R.id.bottomNavigationView);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.go_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;

            } else if (id == R.id.report_phone_number) {
                return true;

            } else if (id == R.id.action_settings) {
                startActivity(new Intent(this, AppSettings.class));
                return true;
            }

            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //submit.findViewById(R.id.submit_phone);
        //submit.setOnClickListener(view -> switchActivities());

        bottomNav.setSelectedItemId(R.id.report_phone_number);
        db = FirebaseFirestore.getInstance();

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        phoneNumberEdit = findViewById(R.id.phone_number);
        phoneNumberEdit.setText(phoneNumber, TextView.BufferType.EDITABLE);

        /*executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    getRequestTest(phoneNumber);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });*/

        findViewById(R.id.submit_phone).setOnClickListener(
                new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        phoneNumberEdit = findViewById(R.id.phone_number);
                        String phoneNumber = phoneNumberEdit.getText().toString();
                        numberTypeEdit = findViewById(R.id.number_type);
                        String numberType = numberTypeEdit.getSelectedItem().toString();
                        scamTypeEdit = findViewById(R.id.scam_type);
                        String scamType = scamTypeEdit.getSelectedItem().toString();
                        String location = getLocation(phoneNumber);
                        String nameAttached = getNameAttached(phoneNumber);
                        otherEdit = findViewById(R.id.other_information);
                        String other = otherEdit.getText().toString();
                        String[] info = {phoneNumber, scamType, location, nameAttached, other, numberType, ""};

                        if(phoneNumber.isEmpty() || other.isEmpty()){
                            Toast.makeText(getApplicationContext(), "Please enter the data", Toast.LENGTH_SHORT).show();
                        } else {
                            String url = "https://scamsearch.io/api/search";
                            String type = "phone";
                            String encodedQuery = "";
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                encodedQuery = URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8);
                            }
                            String fullUrl = String.format("%s?search=%s&type=%s&api_token=%s", url, encodedQuery, type, API_TOKEN);
                            Request request = new Request.Builder().url(fullUrl).build();

                            Map<String, Object> verified = new HashMap<>();
                            verified.put("phoneNumber", phoneNumber);
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    e.printStackTrace();
                                    runOnUiThread(() ->
                                            Toast.makeText(ReportPhoneNumber.this, "Request failed", Toast.LENGTH_SHORT).show()
                                    );
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful() && response.body() != null) {
                                        final String responseData = response.body().string();
                                        info[6] = responseData;
                                        boolean isFound = false;

                                        try{
                                            JSONObject jsonObject = new JSONObject(responseData);
                                            isFound = jsonObject.optBoolean("status",false);
                                        } catch (Exception e){
                                            isFound = false;
                                        }

                                        verified.put("response", responseData);
                                        db.collection("global_verified")
                                                .document()
                                                .set(verified)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Log.d(TAG, "write:onComplete");
                                                        if(!task.isSuccessful()){
                                                            Log.w(TAG, "write:onComplete:failed", task.getException());
                                                            Snackbar.make(findViewById(R.id.submit_phone), "Unsuccessful " + task.getException(), Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                        runOnUiThread(() ->
                                                Toast.makeText(ReportPhoneNumber.this, "Request received", Toast.LENGTH_SHORT).show()
                                        );
                                        submitReport(info, isFound );
                                    }
                                }
                            });
                        }
                    }
                });
        //findViewById(R.id.submit_phone).setOnClickListener(view -> finish());
    }

    public void submitReport(String[] info, boolean isFound ){
        Map<String, Object> phone_numbers = new HashMap<>();
        phone_numbers.put("phoneNumber", info[0]);
        phone_numbers.put("location", info[2]);
        phone_numbers.put("numberType", info[5]);
        phone_numbers.put("scamType", info[1]);
        phone_numbers.put("nameAttached", info[3]);
        phone_numbers.put("other", info[4]);
        phone_numbers.put("status", "pending");
        phone_numbers.put("globalStatus", info[6]); // change with global database verification


        DocumentReference pendingRef = db.collection("phone_numbers").document();

        pendingRef.set(phone_numbers)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "write:onComplete");
                        if(!task.isSuccessful()){
                            Log.w(TAG, "write:onComplete:failed", task.getException());
                            Snackbar.make(findViewById(R.id.submit_phone), "Unsuccessful " + task.getException(), Snackbar.LENGTH_SHORT).show();
                        }
                        else {
                            String id = pendingRef.getId();
                            Log.d(TAG, "id: " + id);
                            if(isFound){
                                verifiedReport(id);
                            }

                            new AlertDialog.Builder(ReportPhoneNumber.this)
                                    .setTitle("Report Confirmation")
                                    .setMessage("\nYou have successfully submitted the report.")
                                    .setPositiveButton("Confirm",null)
                                    .show();
                        }
                    }
                });
    }

    public void getRequestTest(String phoneNumber) throws IOException {
        /*String baseURL = "https://github.com/";
        Request request = new Request.Builder()
                .url(baseURL + "/jdon578")
                .build();
        Call call = client.newCall(request);
        Response response = call.execute();

        if(response.code() == 200){
            showToast("Request received");
        }*/

        String url = "https://scamsearch.io/api/search";
        String type = "phone";
        String encodedQuery = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            encodedQuery = URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8);
        }
        String fullUrl = String.format("%s?search=%s&type=%s&api_token=%s", url, encodedQuery, type, API_TOKEN);
        Request request = new Request.Builder().url(fullUrl).build();

        Map<String, Object> verified = new HashMap<>();
        verified.put("phoneNumber", phoneNumber);

        Call call = client.newCall(request);
        Response response = call.execute();
        if(response.code() == 200){
            String responseData = response.body().string();
            verified.put("response", responseData);
            db.collection("global_verified")
                    .document()
                    .set(verified)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "write:onComplete");
                            if(!task.isSuccessful()){
                                Log.w(TAG, "write:onComplete:failed", task.getException());
                                Snackbar.make(findViewById(R.id.submit_phone), "Unsuccessful " + task.getException(), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
            showToast("Request received");
        }
    }


    /**
     * set the status of the report to verified and move it to the verified_numbers collection
     * @param id
     */
    public void verifiedReport(String id)
    {
        DocumentReference pendingRef = db.collection("phone_numbers").document(id);
        DocumentReference verifiedRef = db.collection("verified_numbers").document(id);

        pendingRef.get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(TAG, "write:onComplete");

                if (!task.isSuccessful()) {
                    Log.w(TAG, "write:onComplete:failed", task.getException());
                    Snackbar.make(findViewById(R.id.submit_phone),
                            "Unsuccessful " + task.getException(),
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                DocumentSnapshot document = task.getResult();
                if (document == null || !document.exists()) {
                    Log.d(TAG, "No such document");
                    return;
                }
                Log.d(TAG, "Document exists");

                Map<String, Object> data = document.getData();
                if (data == null) {
                    return;
                }

                data.put("status", "verified");
                verifiedRef.set(data).addOnCompleteListener(ReportPhoneNumber.this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d(TAG, "verified_numbers:onComplete");

                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "verified_numbers:failed", task.getException());
                                    Snackbar.make(findViewById(R.id.submit_phone),
                                            "Unsuccessful " + task.getException(),
                                            Snackbar.LENGTH_SHORT).show();
                                    return;
                                }

                                pendingRef.update("status", "verified")
                                        .addOnCompleteListener(ReportPhoneNumber.this,
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Log.d(TAG, "phone_numbers update:onComplete");

                                                        if (!task.isSuccessful()) {
                                                            Log.w(TAG, "phone_numbers update:failed",
                                                                    task.getException());
                                                            Snackbar.make(findViewById(R.id.submit_phone),
                                                                    "Unsuccessful " + task.getException(),
                                                                    Snackbar.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        Log.d(TAG, "Verification successful");
                                                    }
                                                });
                            }
                        });
            }
        });
    }


    public String getNameAttached(String phoneNumber){
        // IN FUTURE, GET NAMES FROM API
        // currently for testing purposes
        String[] splitNumber = phoneNumber.split("-");
        String nameAttachedVal = "";
        // FOR TESTING
        Random random = new Random();
        int rand = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            rand = random.nextInt(0, 10);
        }
        if(rand == 1 || rand == 6){
            nameAttachedVal = "Jack Moore";
        } else if(rand == 2 || rand == 7){
            nameAttachedVal = "Donnie Darko";
        } else if(rand == 3 || rand == 8){
            nameAttachedVal = "Kaz Miller";
        } else if(rand == 4 || rand == 9){
            nameAttachedVal = "Solid Snake";
        } else if(rand == 5 || rand == 10){
            nameAttachedVal = "Ellen Ripley";
        }
        return nameAttachedVal;
    }

    public String getLocation(String phoneNumber){
        // IN FUTURE, GET LOCATIONS FROM API, HAVE CASES FOR ALL DIFFERENT INPUTS
        // if string is xxx-xxx-xxxx
        String[] splitNumber = phoneNumber.split("-");
        String locationVal = "";
        if(splitNumber[0].equals("800")){
            locationVal = "Business";
        }
        return locationVal;
    }

    private void contactAdmin(){
        Intent contactAdminIntent = new Intent(this, ContactAdmin.class);
        startActivity(contactAdminIntent);
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

}