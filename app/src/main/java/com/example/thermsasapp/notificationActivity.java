package com.example.thermsasapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

public class notificationActivity extends AppCompatActivity {

    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    Context mContext = this;
    public static Handler exHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String currentUser = intent.getStringExtra("currentUser");

        JSONObject userinfo = new JSONObject();
        try {
            userinfo.put("opcode", "21");
            userinfo.put("username", currentUser);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Sender = new sender();
        Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

        exHandler = new Handler() {
            @Override
            public void handleMessage(Message dbpassword) {
                super.handleMessage(dbpassword);
                try {
                    setContentView(R.layout.notification_activity);

                    JSONObject obj = new JSONObject((String) dbpassword.obj);
                    String notif = obj.getString("notifications");
                    MainActivity.notifications.clear();
                    MainActivity.notifications.add(notif);
                    RecyclerView recyclerV = (RecyclerView) findViewById(R.id.recylerV);
                    notifyAdapter adapterC = new notifyAdapter(MainActivity.notifications);
                    recyclerV.setAdapter(adapterC);
                    recyclerV.setLayoutManager(new LinearLayoutManager(mContext));

                    Button clear = (Button) findViewById(R.id.clearNotif);
                    clear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            JSONObject userinfo = new JSONObject();
                            try {
                                userinfo.put("opcode", "20");
                                userinfo.put("username", currentUser);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Sender = new sender();
                            Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                            try {
                                userinfo.put("opcode", "21");
                                userinfo.put("username", currentUser);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Sender = new sender();
                            Sender.run(databaseServerAddr, userinfo.toString(), senderPort);
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}