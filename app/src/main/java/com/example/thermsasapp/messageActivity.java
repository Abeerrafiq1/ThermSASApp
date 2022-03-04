package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 @author: Abeer Rafiq

 Purpose of Class: When user presses the view messages button, this class retrieves the messages by
 sending a request to the database server. Then it displays the updated messages.
 */
public class messageActivity extends AppCompatActivity {

    // Class variables
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private Context mContext = this;
    public static Handler exHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // Send request to the database server to retrieve the most recent messages from database
        JSONObject userinfo = new JSONObject();
        try {
            userinfo.put("opcode", "21");
            userinfo.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
        Sender = new sender();
        Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

        // When database server responds with the retrieved messages, update the messages in app's display
        // Handles clearing messages as well
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    // Set app view
                    setContentView(R.layout.message_activity);

                    // Extract messages from received message and update them in arraylist
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String notif = obj.getString("messages");
                    MainActivity.messages.clear();
                    MainActivity.messages.add(notif);

                    // Update messages in app's view through the recycler view (it can scroll vertically)
                    RecyclerView recyclerV = (RecyclerView) findViewById(R.id.recyclerV);
                    messageAdapter adapterC = new messageAdapter(MainActivity.messages, "type1");
                    recyclerV.setAdapter(adapterC);
                    recyclerV.setLayoutManager(new LinearLayoutManager(mContext));

                    // If clear pressed, user has triggered app to clear messages
                    Button clear = (Button) findViewById(R.id.clearNotif);
                    clear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        // Send request to database server to clear messages in the database
                        public void onClick(View v) {
                            JSONObject userinfo = new JSONObject();
                            try {
                                userinfo.put("opcode", "20");
                                userinfo.put("username", username);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("AppDebug", "Error! " + e.toString());
                            }
                            Sender = new sender();
                            Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                            // Also send request to database server to retrieve cleared messages
                            // The purpose of this is to trigger the messages in the app's view to be cleared as well
                            // by executing code in this exHandler again
                            try {
                                userinfo.put("opcode", "21");
                                userinfo.put("username", username);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("AppDebug", "Error! " + e.toString());
                            }
                            Sender = new sender();
                            Sender.run(databaseServerAddr, userinfo.toString(), senderPort);
                        }
                    });

                    // If user wants to see details about how messages work
                    Button details_button = (Button) findViewById(R.id.details66);
                    details_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent2 = new Intent(messageActivity.this, detailPopUpActivity.class);
                            intent2.putExtra("height", "0.65");
                            intent2.putExtra("popupText", "\n\n\n\n\n\nTYPES OF NOTIFICATIONS: " +
                                    "\n\n * A stove has been unregistered " +
                                    "\n * A stove ID has been registered " +
                                    "\n * Contacts have been cleared or added " +
                                    "\n * Stove needs immediate attention due to risk " +
                                    "\n * Stove owner has added you as a contact " +
                                    "\n * Stove owner that has added you as a contact needs immediate attention due to risk of their stove" +
                                    "\n\n CLEAR NOTIFICATIONS " +
                                    "\n\n * Can be used to remove all previous messages" +
                                    "\n\n    * SWIPE POP UP RIGHT TO CLOSE IT *  ");
                            startActivity(intent2);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}