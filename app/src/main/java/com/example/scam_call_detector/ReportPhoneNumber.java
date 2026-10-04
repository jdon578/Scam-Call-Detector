package com.example.scam_call_detector;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ReportPhoneNumber extends AppCompatActivity {

    private static final String TAG = "ReportPhoneNumber";
    //Button submit;'
    private FirebaseFirestore db;
    String phoneNumber;
    String location;
    String scamType;
    String nameAttached;
    String other;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_phone_number);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //submit.findViewById(R.id.submit_phone);
        //submit.setOnClickListener(view -> switchActivities());

        db = FirebaseFirestore.getInstance();

        // sends information, does NOT send the inputted information tho
        EditText phoneNumberEdit = findViewById(R.id.phone_number);
        phoneNumber = phoneNumberEdit.getText().toString();
        Spinner scamTypEdit = findViewById(R.id.scam_type);
        scamType = scamTypEdit.getSelectedItem().toString();
        location = getLocation(phoneNumber);
        nameAttached = getNameAttached(phoneNumber);
        EditText otherEdit = findViewById(R.id.other_information);
        other = otherEdit.getText().toString();
        String[] info = {phoneNumber, scamType, location, nameAttached, other};

        findViewById(R.id.submit_phone).setOnClickListener(view -> submitReport(info));
        //findViewById(R.id.submit_phone).setOnClickListener(view -> finish());
    }

    public void submitReport(String[] info){
        Map<String, Object> phone_numbers = new HashMap<>();
        phone_numbers.put("phoneNumber", info[0]);
        phone_numbers.put("location", info[2]);
        phone_numbers.put("scamType", info[1]);
        phone_numbers.put("nameAttached", info[3]);
        phone_numbers.put("other", info[4]);

        db.collection("phone_numbers")
                .document()
                .set(phone_numbers)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "write:onComplete");
                        if(!task.isSuccessful()){
                            Log.w(TAG, "write:onComplete:failed", task.getException());
                            Snackbar.make(findViewById(R.id.submit_phone), "Unsuccessful " + task.getException(), Snackbar.LENGTH_SHORT).show();
                        }
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
}