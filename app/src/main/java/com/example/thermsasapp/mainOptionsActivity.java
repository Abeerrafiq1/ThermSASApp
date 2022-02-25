package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class mainOptionsActivity extends AppCompatActivity {
    Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private String currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_options_activity);

        Intent intent = getIntent();
        currentUser = intent.getStringExtra("currentUser");

        Button addSubscriber = (Button) findViewById(R.id.addSubscriberButton);
        Button notification_button = (Button) findViewById(R.id.view_Notifications);
        Button stoveData_button = (Button) findViewById(R.id.view_stoveData);
        Button addStove_button = (Button) findViewById(R.id.addStoveBtn);
        Button currentSubscribers = (Button) findViewById(R.id.currentSubscribers);

        notification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainOptionsActivity.this, notificationActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }

        });

        currentSubscribers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "15");
                    userinfo.put("currentUser", currentUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                startActivity(new Intent(mainOptionsActivity.this, currentSubscribersActivity.class));
            }

        });

        addStove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject userinfo = new JSONObject();


                try {
                    userinfo.put("opcode", "13");
                    userinfo.put("currentUser", currentUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);


                Intent intent = new Intent(mainOptionsActivity.this, addStoveActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);

            }

        });

        stoveData_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "9");
                    userinfo.put("username", currentUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);
                Intent intent = new Intent(mainOptionsActivity.this, viewCookingListActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);

            }

        });

        addSubscriber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainOptionsActivity.this, addSubscribersActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);

            }
        });
    }
}
