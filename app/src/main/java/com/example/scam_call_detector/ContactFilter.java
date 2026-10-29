package com.example.scam_call_detector;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ContactFilter extends AppCompatActivity {

    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_contact_filter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton filterButton = findViewById(R.id.filter_button);
        TextView main_title = findViewById(R.id.main_title);
        filterButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, filterButton);
            popup.getMenu().add("Blocked Numbers");
            popup.getMenu().add("Contact");
            popup.getMenu().add("Contact Blocked Numbers");

            popup.setOnMenuItemClickListener(item -> {
                if(item.getTitle().equals("Blocked Numbers")){
                    main_title.setText("Blocked Numbers");
                    showBlockNumbers();
                    return true;
                }
                if(item.getTitle().equals("Contact")){
                    main_title.setText("Contact");
                    populateList();
                    return true;
                }
                if(item.getTitle().equals("Contact Blocked Numbers")){
                    main_title.setText("Contact Blocked Numbers");
                    showBlockContactNumbers();
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
        Map<String, Boolean> blockNumbers = new HashMap<>();
        db.collection("blocked_contact_numbers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for(QueryDocumentSnapshot document : task.getResult()){
                            String number = document.getString("phoneNumber");
                            if(number != null) {
                                blockNumbers.put(number, true);
                            }
                        }
                        Log.d("ContactFilter", "Document added successfully");
                    } else {
                        Log.d("ContactFilter", "Error adding document", task.getException());
                    }
                });

        LinearLayout container = findViewById(R.id.contact_container);
        container.removeAllViews();

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
                        if(blockNumbers.containsKey(number)){
                            continue;
                        }
                    }

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

                    Button blockButton = new Button(ContactFilter.this);
                    blockButton.setText("Block Number");

                    Map<String, Object> blockNumber = new HashMap<>();
                    blockNumber.put("phoneNumber", number);
                    blockNumber.put("name", name);

                    blockButton.setOnClickListener(view -> {
                        db.collection("blocked_contact_numbers")
                                .document()
                                .set(blockNumber)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("ContactFilter", "Document added successfully");
                                        Toast.makeText(ContactFilter.this, "Number blocked", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ContactFilter.this, "Error adding number", Toast.LENGTH_SHORT).show();
                                        Log.d("ContactFilter", "Error adding document", task.getException());
                                    }

                                });

                            });


                    layout.addView(phoneText);
                    layout.addView(phoneName);
                    layout.addView(blockButton);

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
                            String scamType = document.getString("scamType");
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
                            phoneName.setText(scamType);
                            phoneName.setTextSize(18);

                            Button unblockButton = new Button(ContactFilter.this);
                            unblockButton.setText("Unblock");

                            unblockButton.setOnClickListener(view -> {
                                String documentId = document.getId();
                                db.collection("blocked_numbers").document(documentId)
                                        .delete()
                                        .addOnCompleteListener(task1 ->{
                                            if(task1.isSuccessful())
                                            {
                                                Log.d("ContactFilter", "Document deleted successfully");
                                                Toast.makeText(ContactFilter.this, "Number unblocked", Toast.LENGTH_SHORT).show();
                                                showBlockNumbers();
                                            }
                                            else
                                            {
                                                Toast.makeText(ContactFilter.this, "Error deleting number", Toast.LENGTH_SHORT).show();
                                                Log.d("ContactFilter", "Error deleting document", task1.getException());
                                            }
                                        });
                            });

                            layout.addView(phoneText);
                            layout.addView(phoneName);
                            layout.addView(unblockButton);

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

    public void showBlockContactNumbers(){
        db.collection("blocked_contact_numbers")
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

                            Button unblockButton = new Button(ContactFilter.this);
                            unblockButton.setText("Unblock");

                            unblockButton.setOnClickListener(view -> {
                                String documentId = document.getId();
                                db.collection("blocked_contact_numbers").document(documentId)
                                        .delete()
                                        .addOnCompleteListener(task1 ->{
                                            if(task1.isSuccessful())
                                            {
                                                Log.d("ContactFilter", "Document deleted successfully");
                                                Toast.makeText(ContactFilter.this, "Number unblocked", Toast.LENGTH_SHORT).show();
                                                showBlockContactNumbers();
                                            }
                                            else
                                            {
                                                Toast.makeText(ContactFilter.this, "Error deleting number", Toast.LENGTH_SHORT).show();
                                                Log.d("ContactFilter", "Error deleting document", task1.getException());
                                            }
                                        });
                            });

                            layout.addView(phoneText);
                            layout.addView(phoneName);
                            layout.addView(unblockButton);

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