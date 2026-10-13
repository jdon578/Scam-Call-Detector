package com.example.scam_call_detector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



import java.util.HashMap;
import java.util.Map;


public class IncomeCall extends CallScreeningService {

    private FirebaseFirestore db;
    @Override
    public void onScreenCall(Call.Details callDetails) {

        String incomeNumber = null;

        if (callDetails.getHandle() != null) {
            incomeNumber = callDetails.getHandle().getSchemeSpecificPart();
        }
        if(incomeNumber != null){
            String phoneNumber = incomeNumber;
            db = FirebaseFirestore.getInstance();

            Map<String, Object> phone_numbers = new HashMap<>();
            phone_numbers.put("phoneNumber", incomeNumber);

            db.collection("incoming_calls")
                    .document()
                    .set(phone_numbers);

            db.collection("verified_numbers")
                    .whereEqualTo("phoneNumber", incomeNumber)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()){
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                String scamType = document.getString("scamType");

                                Log.d("IncomeCall", "Scam number found");
                                Log.d("IncomeCall", "Scam type: " + scamType);

                                //showNotification(phoneNumber,scamType);
                                Intent intent = new Intent(this, ScamOverlayService.class);
                                intent.putExtra("phoneNumber", phoneNumber);
                                intent.putExtra("scamType", scamType);
                                startService(intent);
                            } else {
                                Log.d("IncomeCall", "Number not found:");
                            }
                        }
                        else {
                            Log.d("IncomeCall", "Error getting documents: ", task.getException());
                        }
                    });


        }

        CallScreeningService.CallResponse response =
                new CallScreeningService.CallResponse.Builder()
                        .setDisallowCall(false)
                        .setRejectCall(false)
                        .setSkipCallLog(false)
                        .setSkipNotification(false)
                        .build();

        respondToCall(callDetails, response);
    }

    public void showNotification(String phoneNumber, String scamType){
        NotificationManager manager = getSystemService (NotificationManager.class);
        if(manager == null){
            return;
        }

        String channelId = "scamAlert";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Scam Call Alerts", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        {
            Log.w("IncomeCall", "Notification permission not granted");
            return;
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_report_foreground)
                .setContentTitle("Phone number flagged as scam")
                .setContentText("Scam type:" + scamType)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(this).notify(phoneNumber.hashCode(), notification);
    }


}