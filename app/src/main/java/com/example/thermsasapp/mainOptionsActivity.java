package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


public class mainOptionsActivity extends AppCompatActivity {
    Context mContext = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_options_activity);

        Intent intent = getIntent();
        String currentUser = intent.getStringExtra("currentUser");

        Button addSubscriber = (Button) findViewById(R.id.addSubscriberButton);
        Button notification_button = (Button) findViewById(R.id.view_Notifications);


        addSubscriber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainOptionsActivity.this, addSubscribersActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);

            }
        });
        notification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mainOptionsActivity.this, notificationActivity.class));
            }

        });
    }
}
