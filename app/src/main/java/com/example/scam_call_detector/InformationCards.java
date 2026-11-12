package com.example.scam_call_detector;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class InformationCards extends AppCompatActivity {

//    ImageButton arrow;
//    ConstraintLayout constraintLayout;
    LinearLayout container;
//    LinearLayout layout;
//    LinearLayout hidden;
//    MaterialCardView card;

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_information_cards);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.information);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.report_phone_number) {
                switchActivities();
                return true;
            }

            if (item.getItemId() == R.id.go_home) {
                switchToHome();
                return true;
            }

            if (item.getItemId() == R.id.action_settings){
                switchToSettings();
                return true;
            }

            if(item.getItemId() == R.id.information){
                return true;
            }

            return false;
        });

        populateList();

    }

    private void switchActivities(){
        Intent switchActivityIntent = new Intent(this, ReportPhoneNumber.class);
        startActivity(switchActivityIntent);
    }

    private void switchToHome(){
        Intent switchActivityHome = new Intent(this, MainActivity.class);
        startActivity(switchActivityHome);
    }

    private void switchToSettings(){
        Intent settingsIntent = new Intent(this, AppSettings.class);
        startActivity(settingsIntent);
    }

    public void populateList(){
        Resources res = getResources();
        String[] titles = res.getStringArray(R.array.ScamTypes);
        String[] descs = res.getStringArray(R.array.ScamTypesDESC);
        container = findViewById(R.id.info_container);

        for(int i = 0; i < titles.length-1; i++){
            MaterialCardView card =
                    new MaterialCardView(
                            InformationCards.this);

            LinearLayout hidden = new LinearLayout(InformationCards.this);
            LinearLayout Titlelayout = new LinearLayout(InformationCards.this);
            ImageButton arrow = new ImageButton(InformationCards.this);
            arrow.setImageResource(R.drawable.outline_arrow_downward_alt_24);
            LinearLayout cardContent = new LinearLayout(InformationCards.this);
            cardContent.setOrientation(LinearLayout.VERTICAL);

            //constraintLayout.setMaxHeight(1000);

            //card.setMinimumHeight(300);

            Titlelayout.setOrientation(LinearLayout.HORIZONTAL);
            Titlelayout.setGravity(Gravity.CENTER_VERTICAL);
            Titlelayout.setPadding(20, 0, 20, 0);

            hidden.setOrientation(LinearLayout.VERTICAL);
            hidden.setGravity(Gravity.CENTER_VERTICAL);
            hidden.setPadding(20, 0, 20, 20);
            hidden.setVisibility(MaterialCardView.GONE);
//            hidden.setY(50);

            TextView title = new TextView(InformationCards.this);
            TextView desc = new TextView(InformationCards.this);
            title.setText(titles[i]);
            title.setTextSize(18);
            desc.setText(descs[i]);
            desc.setTextSize(16);

            // currently -- when clicking, only activates last card?
            arrow.setOnClickListener(view -> {
                if(hidden.getVisibility() == MaterialCardView.VISIBLE){
                    TransitionManager.beginDelayedTransition(card, new AutoTransition());
                    hidden.setVisibility(MaterialCardView.GONE);
                    arrow.setImageResource(R.drawable.outline_arrow_downward_alt_24);
                } else {
                    TransitionManager.beginDelayedTransition(card, new AutoTransition());
                    hidden.setVisibility(MaterialCardView.VISIBLE);
                    arrow.setImageResource(R.drawable.outline_arrow_upward_alt_24);
                }
            });

            LinearLayout.LayoutParams titleParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );
            title.setLayoutParams(titleParams);

            Titlelayout.addView(title);
            Titlelayout.addView(arrow);
            hidden.addView(desc);
            cardContent.addView(Titlelayout);
            cardContent.addView(hidden);

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

            cardParams.setMargins(0, 0, 0, 20);
            card.setLayoutParams(cardParams);

            card.addView(cardContent);

            container.addView(card, i);
        }
    }

}