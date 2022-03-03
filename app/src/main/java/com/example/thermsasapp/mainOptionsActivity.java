package com.example.thermsasapp;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 @author: Abeer Rafiq

 Purpose of Class: When the app logs into a user account, five main options are shown.
 For each option, this class directs the app to the associated view.
 The options are the following:
 -view notifications
 -add/view stove#
 -view stove data
 -add contacts
 -current contacts
 */
public class mainOptionsActivity extends AppCompatActivity {

    // Class variables
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private Button addContacts, notification_button, stoveData_button, addStove_button, currentContacts;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_options_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        currentUser = intent.getStringExtra("currentUser");

        // Five buttons associated with the five main options
        addContacts = (Button) findViewById(R.id.addContactsButton);
        notification_button = (Button) findViewById(R.id.view_Notifications);
        stoveData_button = (Button) findViewById(R.id.view_stoveData);
        addStove_button = (Button) findViewById(R.id.addStoveBtn);
        currentContacts = (Button) findViewById(R.id.currentContacts);

        // If user requests to view notifications, start the notificationActivity
        notification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainOptionsActivity.this, notificationActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });

        // If user requests to view current contacts:
        // - send the database server a request to retrieve currently stored contacts for user
        // - then start the currentContactsActivity
        currentContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send request to database server
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "15");
                    userinfo.put("currentUser", currentUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                // Start Activity
                startActivity(new Intent(mainOptionsActivity.this, currentContactsActivity.class));
            }
        });

        // If user requests to view currently registered stove or add/remove a stove number:
        // - send the database server a request to retrieve currently registered stove # for user
        // - then start the addStoveActivity
        addStove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send request to database server
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "13");
                    userinfo.put("currentUser", currentUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                // Start Activity
                Intent intent = new Intent(mainOptionsActivity.this, addStoveActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });

        // If user requests to view stove data:
        // - send the database server a request to retrieve cooking analysis tables associated with user's stove
        // - then start the viewStoveVideoListActivity
        stoveData_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send request to database server
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "9");
                    userinfo.put("username", currentUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                // Start Activity
                Intent intent = new Intent(mainOptionsActivity.this, viewStoveVideoListActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });

        // If user requests to add contacts, start the addContactsActivity
        addContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainOptionsActivity.this, addContactsActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });


        // If user has a notification that needs immediate attention, show a pop up
        // after 400ms of logging in
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // If notification is about a contact's stove
                if (intent.getStringExtra("onTooLong").equals("true-contact")) {
                    String text = "A member has the stove on too long!\n Please check notifications!";

                    Intent intent = new Intent(mainOptionsActivity.this, popActivity.class);
                    intent.putExtra("popupText", text);
                    startActivity(intent);
                }
                // If notification is about your stove
                else if (intent.getStringExtra("onTooLong").equals("true-owner")) {
                    String text = "Your stove was on too long!\n Check stove and notifications!";

                    Intent intent = new Intent(mainOptionsActivity.this, popActivity.class);
                    intent.putExtra("popupText", text);
                    startActivity(intent);
                }
            }
        }, 400);
    }
}