package com.example.scam_call_detector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Use Google's libphonenumber library to check if a phone number is valid
//Version: 9.0.40
//Source: https://github.com/google/libphonenumber

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONObject;
import org.json.JSONArray;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IncomeCall extends CallScreeningService {

    private FirebaseFirestore db;
    private static final String API_TOKEN = "6mxdtn5wck20glbyz4j1pq3s7e9fio";
    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "IncomeNumber";
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

            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumberObj = null;
            try {
                phoneNumberObj = phoneUtil.parse(incomeNumber, "US");
                boolean isValid = phoneUtil.isValidNumber(phoneNumberObj);
                if(!isValid){
                    rejectCall(callDetails);
                    return;
                }
                String standardNumber = phoneUtil.format(phoneNumberObj, PhoneNumberUtil.PhoneNumberFormat.E164);
                incomeNumber = standardNumber;
//                String countryCode = phoneUtil.getRegionCodeForCountryCode(phoneNumberObj.getCountryCode());
            } catch (NumberParseException e) {
                e.printStackTrace();
                return;
            }

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
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(IncomeCall.this, "Request failed", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful() && response.body() != null) {
                            final String responseData = response.body().string();
                            boolean isFound = false;
                            String scamType = null;

                            try{
                                JSONObject jsonObject = new JSONObject(responseData);

                                JSONArray resultArray = jsonObject.optJSONArray("result");
                                isFound = jsonObject.optBoolean("status",false);

                                if(isFound && resultArray != null){
                                    JSONObject resultObject = resultArray.optJSONObject(0);
                                    scamType = resultObject.optString("scammertype");

                                    Log.d("IncomeCall", "Scam number found");
                                    Log.d("IncomeCall", "Scam type: " + scamType);

                                    //showNotification(phoneNumber,scamType);
                                    Intent intent = new Intent(IncomeCall.this, ScamOverlayService.class);
                                    intent.putExtra("phoneNumber", phoneNumber);
                                    intent.putExtra("scamType", scamType);
                                    startService(intent);
                                }
                            } catch (Exception e){
                                isFound = false;
                            }

                        }
                    }
                });



            Map<String, Object> phone_numbers = new HashMap<>();
            phone_numbers.put("phoneNumber", incomeNumber);

            db.collection("blocked_numbers")
                    .whereEqualTo("phoneNumber", incomeNumber)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty()){
                                Log.d("IncomeCall", "Blocked number found");
                                rejectCall(callDetails);
                            }
                        }
                        else{
                            acceptCall(callDetails);
                        }

                    });

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

//        CallScreeningService.CallResponse response =
//                new CallScreeningService.CallResponse.Builder()
//                        .setDisallowCall(false)
//                        .setRejectCall(false)
//                        .setSkipCallLog(false)
//                        .setSkipNotification(false)
//                        .build();
//
//        respondToCall(callDetails, response);
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
                        numbers.add(phoneNo.replaceAll("[()+\\- ]",""));
                    }
                    nCur.close();
                }
            }
            return numbers;
        }
        return null;
    }

    public void rejectCall(Call.Details callDetails){
        CallScreeningService.CallResponse response =
                new CallScreeningService.CallResponse.Builder()
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .setSkipCallLog(false)
                        .setSkipNotification(true)
                        .build();
        respondToCall(callDetails, response);

    }

    public void acceptCall(Call.Details callDetails){
        CallScreeningService.CallResponse response =
                new CallScreeningService.CallResponse.Builder()
                        .setDisallowCall(false)
                        .setRejectCall(false)
                        .setSkipCallLog(false)
                        .setSkipNotification(false)
                        .build();
        respondToCall(callDetails, response);

    }

}