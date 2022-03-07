package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 @author: Abeer Rafiq

 Purpose of Class: When the app logs into a user account, five main options are shown.
 For each option, this class directs the app to the associated view.
 The options are the following:
 -view messages
 -add/view stove#
 -view stove data
 -add contacts
 -current contacts

  There is also a log out option.
 */
public class mainOptionsActivity extends AppCompatActivity {

    // Class variables
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private Button addContacts, message_button, stoveData_button, addStove_button, currentContacts;
    private TextView helloTextView;
    private String username;
    public static Handler exHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_options_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Five buttons associated with the five main options
        addContacts = (Button) findViewById(R.id.addContactsButton);
        message_button = (Button) findViewById(R.id.view_messages);
        stoveData_button = (Button) findViewById(R.id.view_stoveData);
        addStove_button = (Button) findViewById(R.id.addStoveBtn);
        currentContacts = (Button) findViewById(R.id.currentContacts);
        helloTextView = (TextView) findViewById(R.id.helloTextView);

        // Display username on main page
        helloTextView.setText("Hello " + username + "!");

        // If user requests to view messages, start the messageActivity
        message_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainOptionsActivity.this, messageActivity.class);
                intent.putExtra("username", username);
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
                    userinfo.put("username", username);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                // Start Activity
                // Start Activity
                Intent intent = new Intent(mainOptionsActivity.this, currentContactsActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
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
                    userinfo.put("username", username);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                // Start Activity
                Intent intent = new Intent(mainOptionsActivity.this, addStoveActivity.class);
                intent.putExtra("username", username);
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
                    userinfo.put("username", username);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                // Start Activity
                Intent intent = new Intent(mainOptionsActivity.this, viewStoveVideoListActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        // If user requests to add contacts, start the addContactsActivity
        addContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainOptionsActivity.this, addContactsActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }
}