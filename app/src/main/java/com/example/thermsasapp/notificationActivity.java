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

 Purpose of Class: When user presses the view notifications button, this class retrieves the notifications by
 sending a request to the database server. Then it displays the updated notifications.
 */
public class notificationActivity extends AppCompatActivity {

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
        String currentUser = intent.getStringExtra("currentUser");

        // Send request to the database server to retrieve the most recent notifications from database
        JSONObject userinfo = new JSONObject();
        try {
            userinfo.put("opcode", "21");
            userinfo.put("username", currentUser);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
        Sender = new sender();
        Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

        // When database server responds with the retrieve notifications, update the notifications in app's display
        // Handles clearing notifications as well
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    // Set app view
                    setContentView(R.layout.notification_activity);

                    // Extract notifications from received message and update them in arraylist
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String notif = obj.getString("notifications");
                    MainActivity.notifications.clear();
                    MainActivity.notifications.add(notif);

                    // Update notifications in app's view through the recycler view (it can scroll vertically)
                    RecyclerView recyclerV = (RecyclerView) findViewById(R.id.recylerV);
                    notifyAdapter adapterC = new notifyAdapter(MainActivity.notifications);
                    recyclerV.setAdapter(adapterC);
                    recyclerV.setLayoutManager(new LinearLayoutManager(mContext));

                    // If clear pressed, user has triggered app to clear notifications
                    Button clear = (Button) findViewById(R.id.clearNotif);
                    clear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        // Send request to database server to clear notifications in the database
                        public void onClick(View v) {
                            JSONObject userinfo = new JSONObject();
                            try {
                                userinfo.put("opcode", "20");
                                userinfo.put("username", currentUser);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("AppDebug", "Error! " + e.toString());
                            }
                            Sender = new sender();
                            Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                            // Also send request to database server to retrieve cleared notifications
                            // The purpose of this is to trigger the notifications in the app's view to be cleared as well
                            // by executing code in this exHandler again
                            try {
                                userinfo.put("opcode", "21");
                                userinfo.put("username", currentUser);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("AppDebug", "Error! " + e.toString());
                            }
                            Sender = new sender();
                            Sender.run(databaseServerAddr, userinfo.toString(), senderPort);
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