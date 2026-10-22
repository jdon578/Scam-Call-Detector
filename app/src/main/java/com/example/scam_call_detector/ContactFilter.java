package com.example.scam_call_detector;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ContactFilter extends AppCompatActivity {

    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
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
        ImageButton filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, filterButton);
            popup.getMenu().add("Block Numbers");
            popup.getMenu().add("Unblock Numbers");

            popup.setOnMenuItemClickListener(item -> {
                if(item.getTitle().equals("Block Numbers")){
                    showBlockNumbers();
                    return true;
                }
                return false;
            });

            popup.show();
        });

        populateList();
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
    }
    @SuppressLint("Range")
    private void populateList(){

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                null, null);
        if (cur.getCount() > 0) {
            String number = null;
            String name = null;
            while (cur.moveToNext()) {
                @SuppressLint("Range") String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor nCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);

                    // retrieve other numbers for the same contact
                    while (nCur.moveToNext()) {
                        number = nCur.getString(nCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("[() -+]","");
                        name = nCur.getString(nCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    }

                    LinearLayout container = findViewById(R.id.contact_container);
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

                    TextView phoneName = new TextView(ContactFilter.this);
                    phoneName.setText(name);
                    phoneName.setTextSize(18);

                    layout.addView(phoneText);
                    layout.addView(phoneName);

                    card.addView(layout);

                    LinearLayout.LayoutParams cardParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                    cardParams.setMargins(0, 0, 0, 25);
                    card.setLayoutParams(cardParams);
                    container.addView(card);
                    nCur.close();
                }
            }
        }
    }

    public void showBlockNumbers(){
        db = FirebaseFirestore.getInstance();
        db.collection("blocked_numbers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        LinearLayout container = findViewById(R.id.contact_container);
                        container.removeAllViews();
                        if(task.getResult().isEmpty())
                        {
                            TextView text = new TextView(ContactFilter.this);
                            text.setText("No blocked numbers");
                            text.setTextSize(18);
                            container.addView(text);
                            return;
                        }

                        for(QueryDocumentSnapshot document : task.getResult())
                        {
                            String number = document.getString("phoneNumber");
                            String name = document.getString("name");
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

                            TextView phoneName = new TextView(ContactFilter.this);
                            phoneName.setText(name);
                            phoneName.setTextSize(18);

                            layout.addView(phoneText);
                            layout.addView(phoneName);

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
                        Log.d("ContactFilter", "Error getting documents: ", task.getException());
                    }
                });
        }
}