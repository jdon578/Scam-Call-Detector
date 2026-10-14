package com.example.scam_call_detector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

            // don't check for spam if a contact is calling
            List<String> contactNumbers = getContactList();
            if (contactNumbers != null && contactNumbers.contains(incomeNumber)) {
                return;
            }

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

    private ArrayList<String> getContactList(){
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                null, null);
        if (cur.getCount() > 0) {
            ArrayList<String> numbers = new ArrayList<>();
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor nCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);

                    // retrieve other numbers for the same contact
                    while (nCur.moveToNext()) {
                        String phoneNo = nCur.getString(nCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        numbers.add(phoneNo.replaceAll("[()+- ]",""));
                    }
                    nCur.close();
                }
            }
            return numbers;
        }
        return null;
    }


}