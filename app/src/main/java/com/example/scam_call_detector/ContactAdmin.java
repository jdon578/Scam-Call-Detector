package com.example.scam_call_detector;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class ContactAdmin extends AppCompatActivity {

    EditText nameEdit;
    EditText subjectEdit;
    EditText messageEdit;
    EditText emailEdit;

    private FirebaseFirestore db;
    private static final String TAG = "ContactAdmin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_admin);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        findViewById(R.id.send_button).setOnClickListener(
                new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        nameEdit = findViewById(R.id.name);
                        String name = nameEdit.getText().toString();

                        subjectEdit = findViewById(R.id.subject);
                        String subject = subjectEdit.getText().toString();

                        messageEdit = findViewById(R.id.message);
                        String message = messageEdit.getText().toString();

                        emailEdit = findViewById(R.id.email);
                        String email = emailEdit.getText().toString();

                        String[] info = {name, email, subject,message};

                        if(name.isEmpty() || message.isEmpty() || email.isEmpty()){
                            Toast.makeText(getApplicationContext(), "Please enter the information", Toast.LENGTH_SHORT).show();
                        } else {
                            submitReport(info);
                        }
                    }
                });

    }

    public void submitReport(String[] info){
        Map<String, Object> contact_admin = new HashMap<>();
        contact_admin.put("name", info[0]);
        contact_admin.put("message", info[3]);
        contact_admin.put("email", info[1]);
        contact_admin.put("subject", info[2]);

        db.collection("contact_admin")
                .document()
                .set(contact_admin)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "write:onComplete");
                        if(!task.isSuccessful()){
                            Log.w(TAG, "write:onComplete:failed", task.getException());
                            Snackbar.make(findViewById(R.id.send_button), "Unsuccessful " + task.getException(), Snackbar.LENGTH_SHORT).show();
                        }
                        else {
                            new AlertDialog.Builder(ContactAdmin.this)
                                    .setTitle("Message Sent")
                                    .setMessage("\nYour message has been sent. We will get back to you as soon as possible.")
                                    .setPositiveButton("Confirm",null)
                                    .show();
                        }
                    }
                });
    }
}