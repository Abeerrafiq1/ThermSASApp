package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 @author: Abeer Rafiq

 Purpose of Class: To show which usernames have added the current user as a primary contact or a physician contact.
 */
public class whoHasAddedUserAsContactActivity extends AppCompatActivity {

    // Class variables
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private Context mContext = this;
    public static Handler exHandler;
    private TextView info;
    // Array list to store usernames that have added current user as contact
    static ArrayList<String> usernames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // Send request to the database server to retrieve usernames that have added current user as contact from database
        JSONObject userinfo = new JSONObject();
        try {
            userinfo.put("opcode", "24");
            userinfo.put("username", username);
            Log.d("AppDebug", "worked! ");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("AppDebug", "Error! " + e.toString());
        }
        Sender = new sender();
        Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);


        // When database server responds with the retrieved usernames, update the username list in app's display
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    // Set app view
                    setContentView(R.layout.who_has_added_user_activity);

                    // Extract usernames that have added current user as primary contact from received message and update them in arraylist
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String usernamesFromDB1 = obj.getString("usernames");
                    usernames.clear();
                    String[] usernamesArray1 = {""};
                    if (!usernamesFromDB1.equals("_")) {
                        usernamesArray1= usernamesFromDB1.split(Pattern.quote(","));
                        for (int i = 0; i < usernamesArray1.length; i++) {
                            usernames.add(" - " + usernamesArray1[i]);
                        }
                    }
                    // Extract usernames that have added current user as physician contact from received message and update them in arraylist
                    String usernamesFromDB2 = obj.getString("physician");
                    String[] usernamesArray2 = {""};
                    // Parse usernames and add to usernames array
                    if (!usernamesFromDB2.equals("_")) {
                        usernamesArray2 = usernamesFromDB2.split(Pattern.quote(","));
                        for (int i = 0; i < usernamesArray2.length; i++) {
                            usernames.add(" * " + usernamesArray2[i]);
                        }
                        // Set details
                        if (usernamesArray2.length > 0 && !usernamesFromDB2.equals("")) {
                            info = (TextView) findViewById(R.id.InfoText);
                            info.setText(" - Primary Contact\n * Physician Contact");
                        }
                    }

                    // If no usernames have added current user as a contact, show none
                    if (usernamesFromDB2.equals("_") && usernamesFromDB1.equals("_")){
                        usernames.add(" - None ");
                    }

                    // If more than 6 items, view is scrollable so show corresponding text
                    if (usernames.size() > 6) {
                        info = (TextView) findViewById(R.id.scrollText);
                        info.setText("(List can be scrolled)");
                    }

                    // Update usernames in app's view through the recycler view (it can scroll vertically)
                    RecyclerView recyclerV = (RecyclerView) findViewById(R.id.recyclerViewUsernames);
                    messageAdapter adapterC = new messageAdapter(usernames, "type2");
                    recyclerV.setAdapter(adapterC);
                    recyclerV.setLayoutManager(new LinearLayoutManager(mContext));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}