package com.example.scam_call_detector;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class ContactFilter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_filter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        populateList();
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
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

    private void populateList(){
        LinearLayout container = findViewById(R.id.scam_number_container);
        List<String> contactNumbers = getContactList();
        assert contactNumbers != null;
        for (String number : contactNumbers) {

            com.google.android.material.card.MaterialCardView card =
                    new com.google.android.material.card.MaterialCardView(
                            ContactFilter.this);
            LinearLayout layout =
                    new LinearLayout(ContactFilter.this);

            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            layout.setPadding(20, 20, 20, 20);

            TextView phoneText = new TextView(ContactFilter.this);
            phoneText.setText(number);
            phoneText.setTextSize(18);

            layout.addView(phoneText);

            card.addView(layout);

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

            cardParams.setMargins(0, 0, 0, 25);
            card.setLayoutParams(cardParams);
            container.addView(card);

        }
    }
}