package com.example.scam_call_detector;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.VoicemailContract;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class VoicemailList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voicemail_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //fakeVoicemails();
        retrieveVoicemails();
        findViewById(R.id.back_arrow3).setOnClickListener(v -> finish());
    }

    public void retrieveVoicemails(){
        Uri uri = VoicemailContract.Voicemails.CONTENT_URI;
        //Uri audioUri = ContentUris.withAppendedId(VoicemailContract.Voicemails.CONTENT_URI, voicemailId);
        String[] projection = {
                VoicemailContract.Voicemails._ID,
                VoicemailContract.Voicemails.NUMBER,
                VoicemailContract.Voicemails.DATE,
                VoicemailContract.Voicemails.DURATION,
                VoicemailContract.Voicemails.TRANSCRIPTION
        };

        LinearLayout container = findViewById(R.id.voicemail_container);

        ContentResolver cr = getContentResolver();
        try(Cursor cursor = cr.query(uri, projection, null, null, null)){
            if(cursor != null){
                @SuppressLint("Range") int idIndex = cursor.getColumnIndex(VoicemailContract.Voicemails._ID);
                @SuppressLint("Range") int numberIndex = cursor.getColumnIndex(VoicemailContract.Voicemails.NUMBER);
                @SuppressLint("Range") int dateIndex = cursor.getColumnIndex(VoicemailContract.Voicemails.DATE);
                @SuppressLint("Range") int durationIndex = cursor.getColumnIndex(VoicemailContract.Voicemails.DURATION);
                @SuppressLint("Range") int transcriptionIndex = cursor.getColumnIndex(VoicemailContract.Voicemails.TRANSCRIPTION);

                while (cursor.moveToNext()){
                    String id = cursor.getString(idIndex);
                    String number = cursor.getString(numberIndex);
                    String date = cursor.getString(dateIndex);
                    String duration = cursor.getString(durationIndex);
                    String transcription = cursor.getString(transcriptionIndex);

                    Uri voicemailUri = ContentUris.withAppendedId(uri, Long.parseLong(id));

                    Date callDayTime = new Date(Long.parseLong(date));
                    LocalDateTime dateTime = callDayTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    DateTimeFormatter callDayTimeFormatted = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");
                    String finalCallDate = dateTime.format(callDayTimeFormatted);

                    int durINT = Integer.parseInt(duration);
                    int hour = 0;
                    int min = 0;
                    int sec = 0;
                    if(durINT > 3600){
                        hour = durINT/3600;
                        durINT = durINT-(3600*hour);
                    }
                    if(durINT > 60){
                        min = durINT/60;
                        durINT = durINT-(60*min);
                    }
                    sec = durINT;

                    MaterialCardView card =
                            new MaterialCardView(
                                    VoicemailList.this);
                    LinearLayout layout =
                            new LinearLayout(VoicemailList.this);

                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setGravity(Gravity.CENTER_VERTICAL);
                    layout.setPadding(20, 20, 20, 20);

                    TextView phoneText = new TextView(VoicemailList.this);
                    phoneText.setText(number);
                    phoneText.setTextSize(18);

                    TextView dateText  = new TextView(VoicemailList.this);
                    dateText.setText("Date: " + finalCallDate);
                    dateText.setTextSize(18);

                    TextView durationText  = new TextView(VoicemailList.this);
                    @SuppressLint("DefaultLocale") String formatDuration = String.format("%02d:%02d:%02d", hour, min, sec);
                    durationText.setText("Duration: " + formatDuration);
                    durationText.setTextSize(18);

                    TextView transcriptionText = new TextView(VoicemailList.this);
                    transcriptionText.setText(transcription);
                    transcriptionText.setTextSize(18);

                    layout.addView(phoneText);
                    layout.addView(dateText);
                    layout.addView(durationText);
                    layout.addView(transcriptionText);

                    card.addView(layout);

                    LinearLayout.LayoutParams cardParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                    cardParams.setMargins(0, 0, 0, 25);
                    card.setLayoutParams(cardParams);
                    container.addView(card, 0);
                }
            }
        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public void fakeVoicemails(){
        ContentValues values = new ContentValues();

        values.put(VoicemailContract.Voicemails.NUMBER, "1234567890");
        values.put(VoicemailContract.Voicemails.DATE, System.currentTimeMillis());
        values.put(VoicemailContract.Voicemails.DURATION, 45);
        values.put(VoicemailContract.Voicemails.IS_READ, 0);

        values.put(VoicemailContract.Voicemails.SOURCE_PACKAGE, getPackageName());

        Uri uri = getContentResolver().insert(VoicemailContract.Voicemails.CONTENT_URI, values);
        if(uri != null){
            Log.d("VoicemailTest", "Inserted voicemail: " + uri.toString());
        }
    }
}