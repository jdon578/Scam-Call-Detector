package com.example.scam_call_detector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;


public class IncomeCallReceiver extends BroadcastReceiver {
    private FirebaseFirestore db;
    private static final String TAG = "IncomeCallReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(
//                context,
//                "Receiver triggered",
//                Toast.LENGTH_LONG
//        ).show();
        if(TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())){
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE); //current state of the call
            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "Incoming call: " + incomingNumber);
                if(incomingNumber != null){
                    db = FirebaseFirestore.getInstance();

                    Map<String, Object> phone_numbers = new HashMap<>();
                    phone_numbers.put("phoneNumber", incomingNumber);

                    db.collection("incoming_calls")
                            .document()
                            .set(phone_numbers)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Phone number saved to Firebase"))
                            .addOnFailureListener(e -> Log.w(TAG, "Failed to save phone number", e));


                }
            }

        }

    }


}
