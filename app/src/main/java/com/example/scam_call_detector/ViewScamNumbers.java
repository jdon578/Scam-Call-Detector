package com.example.scam_call_detector;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ViewScamNumbers extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_scam_numbers);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        getAllNumbers();
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
    }

    public void getAllNumbers() {
        // [START get_all_users]
        db.collection("phone_numbers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            LinearLayout container = findViewById(R.id.scam_number_container);
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String phoneNumber = document.getString("phoneNumber");
                                String location = document.getString("location");
                                String scamType = document.getString("scamType");
                                String numberType = document.getString("numberType");
                                String nameAttached = document.getString("nameAttached");
                                String other = document.getString("other");
                                String status = document.getString("status");
                                String globalStatus = document.getString("globalStatus");

                                com.google.android.material.card.MaterialCardView card =
                                        new com.google.android.material.card.MaterialCardView(
                                                ViewScamNumbers.this);
                                LinearLayout layout =
                                        new LinearLayout(ViewScamNumbers.this);

                                layout.setOrientation(LinearLayout.VERTICAL);
                                layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
                                layout.setPadding(20, 20, 20, 20);

                                TextView phoneText = new TextView(ViewScamNumbers.this);
                                phoneText.setText(phoneNumber);
                                phoneText.setTextSize(18);

                                TextView nameText = new TextView(ViewScamNumbers.this);
                                nameText.setText("Name: " + nameAttached);
                                nameText.setTextSize(18);

                                TextView typeText  = new TextView(ViewScamNumbers.this);
                                typeText.setText("Scam Type: "+ scamType);
                                typeText.setTextSize(18);

                                TextView numberTypeText  = new TextView(ViewScamNumbers.this);
                                numberTypeText.setText("Number Type: "+ numberType);
                                numberTypeText.setTextSize(18);

                                TextView locationText  = new TextView(ViewScamNumbers.this);
                                locationText .setText("location: " + location);
                                locationText .setTextSize(18);

                                TextView otherText  = new TextView(ViewScamNumbers.this);
                                otherText.setText("other: " + other);
                                otherText.setTextSize(18);

                                TextView statusText  = new TextView(ViewScamNumbers.this);
                                statusText.setText("status: " + status);
                                statusText.setTextSize(18);

                                TextView globalStatusText  = new TextView(ViewScamNumbers.this);
                                globalStatusText.setText("global status: " + globalStatus);
                                globalStatusText.setTextSize(18);

                                layout.addView(phoneText);
                                layout.addView(nameText);
                                layout.addView(typeText);
                                layout.addView(numberTypeText);
                                layout.addView(locationText);
                                layout.addView(otherText);
                                layout.addView(statusText);
                                layout.addView(globalStatusText);

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