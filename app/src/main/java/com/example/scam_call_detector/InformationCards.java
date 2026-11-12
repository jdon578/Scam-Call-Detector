package com.example.scam_call_detector;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.Gravity;
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

    ImageButton arrow;
    ConstraintLayout constraintLayout;
    LinearLayout container;
    LinearLayout layout;
    LinearLayout hidden;
    MaterialCardView card;

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

        for(int i = 0; i < titles.length-1; i++){
            container = findViewById(R.id.info_container);
            MaterialCardView card =
                    new MaterialCardView(
                            InformationCards.this);
            arrow = new ImageButton(InformationCards.this);
            hidden = new LinearLayout(InformationCards.this);
            layout = new LinearLayout(InformationCards.this);
            arrow.setImageResource(R.drawable.outline_arrow_downward_alt_24);
            constraintLayout = new ConstraintLayout(InformationCards.this);

            //constraintLayout.setMaxHeight(1000);

            //card.setMinimumHeight(300);

            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setPadding(20, 20, 20, 400);

            hidden.setOrientation(LinearLayout.VERTICAL);
            hidden.setGravity(Gravity.CENTER_VERTICAL);
            hidden.setPadding(20, 0, 20, 0);
            hidden.setVisibility(MaterialCardView.GONE);
            hidden.setY(50);

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

            layout.addView(title);
            layout.addView(arrow);
            hidden.addView(desc);
            card.addView(layout);
            card.addView(hidden);

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

            cardParams.setMargins(0, 0, 0, 200);
            card.setLayoutParams(cardParams);
            constraintLayout.addView(card);
            container.addView(constraintLayout, i);
        }
    }
}