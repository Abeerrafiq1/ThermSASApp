package com.example.thermsasapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class addStoveActivity extends AppCompatActivity {
    Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    public static Handler exHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_stove_activity);

        Intent intent = getIntent();
        String currentUser = intent.getStringExtra("currentUser");

        EditText stoveID_editText = (EditText) findViewById(R.id.enterStoveID);
        Button registerStove_button = (Button) findViewById(R.id.registerStoveBtn);


        registerStove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "11");
                    userinfo.put("currentUser", currentUser);
                    userinfo.put("stoveID", stoveID_editText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

            }

        });

        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String opcode = obj.getString("opcode");
                    TextView updateText = (TextView) findViewById(R.id.updateText);
                    Boolean update = false;
                    String notification =  "";
                    if (opcode.equals("12")) {
                        String validity = obj.getString("validity");
                        String maxStoveID = obj.getString("maxStoveID");
                        if (validity.equals("yes")) {
                            Toast.makeText(mContext, "Stove registered successfully!", Toast.LENGTH_SHORT).show();
                            update = true;
                            notification = "Stove #" + stoveID_editText.getText().toString() + " has been registered successfully!";
                        }
                        else if (validity.equals("empty")){
                            Toast.makeText(mContext, "Stove Entry Cleared", Toast.LENGTH_SHORT).show();
                            update = true;
                            notification = "Your stove has been unregistered";
                        }
                        else
                        {
                            Toast.makeText(mContext, "Stove number already exists \nSuggestion: " + (Integer.parseInt(maxStoveID) + 1), Toast.LENGTH_SHORT).show();
                        }
                        JSONObject userinfo = new JSONObject();
                        try {
                            userinfo.put("opcode", "13");
                            userinfo.put("currentUser", currentUser);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Sender = new sender();
                        Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                    }
                    else if (opcode.equals("14")){
                        String stoveRegistered = obj.getString("stoveRegistered");
                        if (stoveRegistered.equals("")) {
                            updateText.setText("None");
                        } else {
                            updateText.setText(stoveRegistered);
                        }
                    }

                    if (update){
                        ArrayList<String> copyNotifications = new ArrayList<>();
                        copyNotifications.add(MainActivity.notifications.get(0));
                        MainActivity.notifications.clear();

                        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String formattedTimeStamp = sdf3.format(timestamp);
                        String usersNotification = "\n\n [" + formattedTimeStamp + "]\n\n" + notification + "\n\n-----------------------------------------";
                        MainActivity.notifications.add(0, copyNotifications.get(0).toString() + usersNotification);
                        JSONObject userinfo = new JSONObject();
                        try {
                            userinfo.put("opcode", "19");
                            userinfo.put("username", currentUser);
                            userinfo.put("UserNotifications", usersNotification);
                            userinfo.put("ContactNotifications", "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Sender = new sender();
                        Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        };
    }
}