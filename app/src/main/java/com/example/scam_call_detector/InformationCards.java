package com.example.scam_call_detector;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

        populate();

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

    public void populate(){
        populateList(R.array.ScamTypes, R.array.ScamTypesDESC,"Scam Types",false);
        populateList(R.array.protection, R.array.protectionDESC,"How to protect yourself from a scam?",false);
        populateList(R.array.Resources, R.array.ResourcesDESC,"Useful Resources",true);
    }
    public void populateList(int Title, int Desc,String sectionTitleText, boolean showButton){
        Resources res = getResources();
        String[] titles = res.getStringArray(Title);
        String[] descs = res.getStringArray(Desc);
        String[] urls = res.getStringArray(R.array.ResourcesURL);
        container = findViewById(R.id.info_container);

        MaterialCardView card =
                new MaterialCardView(
                        InformationCards.this);

        //first layer
        LinearLayout cardContent = new LinearLayout(InformationCards.this);
        cardContent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout hiddenSection = new LinearLayout(InformationCards.this);
        LinearLayout TitlelayoutSection = new LinearLayout(InformationCards.this);
        ImageButton Sectionarrow = new ImageButton(InformationCards.this);
        Sectionarrow.setImageResource(R.drawable.outline_arrow_downward_alt_24);

        TitlelayoutSection.setOrientation(LinearLayout.HORIZONTAL);
        TitlelayoutSection.setGravity(Gravity.CENTER_VERTICAL);
        TitlelayoutSection.setPadding(20, 0, 20, 0);

        hiddenSection.setOrientation(LinearLayout.VERTICAL);
        hiddenSection.setGravity(Gravity.CENTER_VERTICAL);
        hiddenSection.setPadding(20, 0, 20, 20);
        hiddenSection.setVisibility(MaterialCardView.GONE);

        TextView sectionTitle = new TextView(InformationCards.this);
        sectionTitle.setText(sectionTitleText);
        sectionTitle.setTextSize(20);

        Sectionarrow.setOnClickListener(view -> {
            if (hiddenSection.getVisibility() == MaterialCardView.VISIBLE) {
                TransitionManager.beginDelayedTransition(card, new AutoTransition());
                hiddenSection.setVisibility(MaterialCardView.GONE);
                Sectionarrow.setImageResource(R.drawable.outline_arrow_downward_alt_24);
            } else {
                TransitionManager.beginDelayedTransition(card, new AutoTransition());
                hiddenSection.setVisibility(MaterialCardView.VISIBLE);
                Sectionarrow.setImageResource(R.drawable.outline_arrow_upward_alt_24);
            }
        });

        LinearLayout.LayoutParams sectionTitleParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        sectionTitle.setLayoutParams(sectionTitleParams);

        TitlelayoutSection.addView(sectionTitle);
        TitlelayoutSection.addView(Sectionarrow);

        //section layer
        for(int i = 0; i < titles.length; i++) {
//            MaterialCardView card =
//                    new MaterialCardView(
//                            InformationCards.this);
            final int index = i;

            LinearLayout hidden = new LinearLayout(InformationCards.this);
            LinearLayout Titlelayout = new LinearLayout(InformationCards.this);
            ImageButton arrow = new ImageButton(InformationCards.this);
            arrow.setImageResource(R.drawable.outline_arrow_downward_alt_24);

            Titlelayout.setOrientation(LinearLayout.HORIZONTAL);
            Titlelayout.setGravity(Gravity.CENTER_VERTICAL);
            Titlelayout.setPadding(20, 0, 20, 0);

//            if(i % 2 == 0){
//                Titlelayout.setBackgroundColor(Color.parseColor("#CE93D8"));
//            }

            hidden.setOrientation(LinearLayout.VERTICAL);
            hidden.setGravity(Gravity.CENTER_VERTICAL);
            hidden.setPadding(20, 0, 20, 20);
            hidden.setVisibility(MaterialCardView.GONE);

            TextView title = new TextView(InformationCards.this);
            TextView desc = new TextView(InformationCards.this);
            title.setText(titles[i]);
            title.setTextSize(18);
            desc.setText(Html.fromHtml(descs[i], Html.FROM_HTML_MODE_LEGACY));
            desc.setTextSize(16);

            // currently -- when clicking, only activates last card?
            arrow.setOnClickListener(view -> {
                if (hidden.getVisibility() == MaterialCardView.VISIBLE) {
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


            hiddenSection.addView(Titlelayout);
            hiddenSection.addView(hidden);

            if (showButton){
                Button button = new Button(InformationCards.this);
                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(20);
                drawable.setColor(Color.parseColor("#6750A4"));
                button.setBackground(drawable);
                button.setText("Visit Website");
                button.setTextColor(Color.parseColor("#FFFFFF"));

                button.setOnClickListener(view -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[index]));
                    startActivity(browserIntent);
                });
                hidden.addView(button);
            }


        }

        cardContent.addView(TitlelayoutSection);
        cardContent.addView(hiddenSection);
        card.addView(cardContent);
        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        cardParams.setMargins(0, 0, 0, 50);
        card.setLayoutParams(cardParams);


        container.addView(card);

    }

}