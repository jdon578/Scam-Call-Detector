package com.example.scam_call_detector;

import android.telecom.Call;
import android.telecom.CallScreeningService;
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
            db = FirebaseFirestore.getInstance();

            Map<String, Object> phone_numbers = new HashMap<>();
            phone_numbers.put("phoneNumber", incomeNumber);

            db.collection("incoming_calls")
                    .document()
                    .set(phone_numbers);


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
}