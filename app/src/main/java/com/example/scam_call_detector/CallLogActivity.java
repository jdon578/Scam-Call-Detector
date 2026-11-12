package com.example.scam_call_detector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.role.RoleManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.example.scam_call_detector.databinding.ActivityCallLogBinding;
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

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

public class CallLogActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private static final String TAG = "CallLogActivity";
    private FirebaseFirestore db;
    private static final int REQUEST_CALL_SCREENING_ROLE = 1;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityCallLogBinding binding = ActivityCallLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshScreen();
            }
        });

        db = FirebaseFirestore.getInstance();

        getCallLogs();

        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
    }

    private void refreshScreen() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
            @Override
            public void run() {
                getCallLogs();
                Toast.makeText(CallLogActivity.this, "Refreshed", Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }

    private void switchToReportAuto(String phoneNumber){
        Intent reportIntent = new Intent(this, ReportPhoneNumber.class);
        reportIntent.putExtra("phoneNumber", phoneNumber);
        startActivity(reportIntent);
    }
    private void getCallLogs(){
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
                LocalDateTime dateTime = callDayTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                DateTimeFormatter callDayTimeFormatted = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");
                String finalCallDate = dateTime.format(callDayTimeFormatted);

                int durINT = Integer.parseInt(callDuration);
                int hour = 0;
                int min = 0;
                int sec = 0;
                if(durINT > 3600){
                    hour = durINT/3600;
                    durINT = durINT-(3600*hour);
                }
                if(durINT > 60){
                    min = durINT/60;
                    durINT = durINT-(60*min);
                }
                sec = durINT;

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

                MaterialCardView card =
                        new MaterialCardView(
                                CallLogActivity.this);
                LinearLayout layout =
                        new LinearLayout(CallLogActivity.this);

                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(Gravity.CENTER_VERTICAL);
                layout.setPadding(20, 20, 20, 20);

                // get verified phone numbers
//                db = FirebaseFirestore.getInstance();

                Map<String, Object> phone_numbers = new HashMap<>();
                phone_numbers.put("phoneNumber", phoneNumber);

                db.collection("incoming_calls")
                        .document()
                        .set(phone_numbers);

                TextView phoneText = new TextView(CallLogActivity.this);
                phoneText.setText(phoneNumber);
                phoneText.setTextSize(18);

                TextView typeText = new TextView(CallLogActivity.this);
                typeText.setText("Type: " + dir);
                typeText.setTextSize(18);

                TextView dateText  = new TextView(CallLogActivity.this);
                dateText.setText("Date: " + finalCallDate);
                dateText.setTextSize(18);

                TextView durationText  = new TextView(CallLogActivity.this);
                @SuppressLint("DefaultLocale") String formatDuration = String.format("%02d:%02d:%02d", hour, min, sec);
                durationText.setText("Duration: " + formatDuration);
                durationText.setTextSize(18);

                layout.addView(phoneText);
                TextView nameText = new TextView(CallLogActivity.this);
                String name = getContact(phoneNumber);
                if(name != null){
                    nameText.setText(name);
                    nameText.setTextSize(18);
                    layout.addView(nameText);
                }
                layout.addView(typeText);
                layout.addView(dateText);
                layout.addView(durationText);

                db.collection("verified_numbers")
                        .whereEqualTo("phoneNumber", phoneNumber)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if(!task.getResult().isEmpty()){
                                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                    String scamType;
                                    scamType = document.getString("scamType");

                                    TextView scamText = new TextView(CallLogActivity.this);
                                    Button goToReport = new Button(CallLogActivity.this);
                                    if(scamType != null){
                                        scamText.setText("Scam Type: " + scamType);
                                        scamText.setTextSize(18);
                                        goToReport.setText("Report");
                                        goToReport.setTextSize(16);
                                        goToReport.setOnClickListener(v -> switchToReportAuto(phoneNumber));
                                        layout.addView(scamText);
                                        layout.addView(goToReport);
                                    }

                                    Log.d("IncomeCall", "Scam number found");
                                    Log.d("IncomeCall", "Scam type: " + scamType);

                                } else {
                                    Log.d("IncomeCall", "Number not found:");
                                }
                            }
                            else {
                                Log.d("IncomeCall", "Error getting documents: ", task.getException());
                            }
                        });

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

    @SuppressLint("Range")
    public String getContact(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        String number = "";
        String name = "";
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                @SuppressLint("Range") String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor nCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    // retrieve other numbers for the same contact
                    while (nCur.moveToNext()) {
                        number = nCur.getString(nCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("[^0-9]", "");
                        name = nCur.getString(nCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        if(number.equals(phoneNumber)){
                            nCur.close();
                            return name;
                        }
                    }
                    nCur.close();
                }
            }
        }
        return null;
    }
}