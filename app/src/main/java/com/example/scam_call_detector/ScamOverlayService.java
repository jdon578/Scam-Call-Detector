package com.example.scam_call_detector;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ScamOverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private TelephonyManager telephonyManager;
    private TelephonyCallback telephonyCallback;

    private  String phoneNumber;
    private  String scamType;
    private boolean activeState = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
         phoneNumber = intent.getStringExtra("phoneNumber");
         scamType = intent.getStringExtra("scamType");

        //showOverlay(phoneNumber, scamType);
        registerCallStateListener();
        return START_NOT_STICKY;
    }

    private void showOverlay(String phoneNumber, String scamType) {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }
        removeOverlay();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(60, 50, 60, 40);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F9F6FD"));
        bg.setCornerRadius(40f);
        bg.setStroke(6, Color.parseColor("#6A4C93"));
        layout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("Phone number flagged as scam");
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);

        TextView info = new TextView(this);
        info.setText( "scam type：" + scamType);
        info.setGravity(Gravity.CENTER_HORIZONTAL);
        info.setTextSize(16);
        info.setTextColor(Color.BLACK);
        info.setPadding(0, 30, 0, 30);

        Button botton = new Button(this);
        botton.setText("Block this caller");
        botton.setBackgroundColor(Color.RED);
        botton.setTextColor(Color.WHITE);
        botton.setOnClickListener(v -> {
            removeOverlay();
            stopSelf();
        });

        layout.addView(title);
        layout.addView(info);
        layout.addView(botton);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;

        overlayView = layout;
        windowManager.addView(overlayView, params);
    }

    private void registerCallStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyCallback = new CallState();
        telephonyManager.registerTelephonyCallback(ContextCompat.getMainExecutor(this), telephonyCallback);
    }

    private class CallState extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        public void onCallStateChanged(int state) {
            Log.d("OverlayService", "call state = " + state);
            if (state == TelephonyManager.CALL_STATE_RINGING ) {
                activeState = true;
                showNotification(phoneNumber,scamType);
                showOverlay(phoneNumber,scamType);
            }
            else if( state == TelephonyManager.CALL_STATE_OFFHOOK ){
                activeState = true;
                if(overlayView == null){
                    showOverlay(phoneNumber,scamType);
                }
            }
            else if (state == TelephonyManager.CALL_STATE_IDLE && activeState) {
                removeOverlay();
                stopSelf();
            }
        }
    }

    private void removeOverlay() {
        if (windowManager != null && overlayView != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception e) {
                Log.e("OverlayService", "Failed to remove overlay view");
            }
            overlayView = null;
        }
    }

    @Override
    public void onDestroy() {
        removeOverlay();
        if (telephonyManager != null && telephonyCallback != null) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback);
        }
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) { return null; }

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