package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

/**
 @author: Abeer Rafiq

 Purpose of Class: To show the currently registered stove ID and to remove/add a stove ID.
 The stove ID associates with the stove that the thermal camera is monitoring and collecting data for.
 Once the stove ID is added or cleared, the messages will be updated.
 */
public class addStoveActivity extends AppCompatActivity {

    // Class variables
    private Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private EditText stoveID_editText;
    private Button registerStove_button;
    public static Handler exHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_stove_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // EditText to enter a stove ID to register stove
        stoveID_editText = (EditText) findViewById(R.id.enterStoveID);
        // Register button to trigger stove registration
        registerStove_button = (Button) findViewById(R.id.registerStoveBtn);

        // If user requests to add or remove currently registered stove ID
        // send the database server a request to update stove ID
        registerStove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "11");
                    userinfo.put("username", username);
                    userinfo.put("stoveID", stoveID_editText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);
            }
        });

        // If user wants to see details about how stove registration/clearing works
        Button details_button = (Button) findViewById(R.id.details3);
        details_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(addStoveActivity.this, detailPopUpActivity.class);
                intent2.putExtra("height", "0.55");
                intent2.putExtra("popupText", "\n\n\n\n\n\n\nREGISTRATION OF STOVE: " +
                        "\n\n * Enter ID corresponding to stove that the thermal camera monitors " +
                        "\n * The stove ID must not be registered already " +
                        "\n * ID is used to view stove analysis data corresponding to stove ID " +
                        "\n\n UNREGISTERING STOVE " +
                        "\n\n * Leave stove ID field empty and press REGISTER STOVE button" +
                        "\n\n\n    * SWIPE POP UP RIGHT TO CLOSE IT *  ");
                startActivity(intent2);
            }
        });

        // Opcode 12:
        // When database server sends a message for successful/unsuccessful registration, take appropriate action
        // If stove ID cleared or registered, update messages
        // Opcode 14:
        // When database server sends the currently registered stove, display it in the view (updateText Textview)
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    // Initialize text view that will display stove ID
                    TextView updateText = (TextView) findViewById(R.id.updateText);

                    // Extract opcode from received message and initialize variables
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String opcode = obj.getString("opcode");
                    String message =  "";
                    Boolean updateMessages = false;

                    // If opcode = 12, the database server has sent a response for successful/unsuccessful stove registration
                    if (opcode.equals("12")) {
                        String validity = obj.getString("validity");
                        String maxStoveID = obj.getString("maxStoveID");

                        // If successful registration, update textView; should update messages
                        if (validity.equals("yes")) {
                            Toast.makeText(mContext, "Stove registered successfully!", Toast.LENGTH_SHORT).show();
                            updateText.setText(stoveID_editText.getText().toString());
                            updateMessages = true;
                            message = "Stove with ID " + stoveID_editText.getText().toString() + " has been registered successfully!";
                        }
                        // If successfully cleared stove ID, update textView; should update messages
                        else if (validity.equals("empty")){
                            Toast.makeText(mContext, "Stove Entry Cleared", Toast.LENGTH_SHORT).show();
                            updateText.setText("None");
                            updateMessages = true;
                            message = "Your stove has been unregistered";
                        }
                        // If stove ID already registered, suggest a new stove ID that can be registered
                        else
                        {
                            Toast.makeText(mContext, "Stove ID already exists \nSuggestion: " + (Integer.parseInt(maxStoveID) + 1), Toast.LENGTH_SHORT).show();
                        }
                    }
                    // If opcode = 14, the database server has sent the currently registered stove ID
                    // Update the currently registered stove ID in the updateText textView
                    else if (opcode.equals("14")){
                        String stoveRegistered = obj.getString("stoveRegistered");
                        if (stoveRegistered.equals("")) {
                            updateText.setText("None");
                        } else {
                            updateText.setText(stoveRegistered);
                        }
                    }

                    // If stove ID is registered or cleared, add associating message to messages
                    if (updateMessages){
                        ArrayList<String> copyMessages = new ArrayList<>();
                        copyMessages.add(MainActivity.messages.get(0));
                        MainActivity.messages.clear();

                        // Create message with timestamp
                        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String formattedTimeStamp = sdf3.format(timestamp);
                        String usersMessage = "\n\n [" + formattedTimeStamp + "]\n\n" + message + "\n\n-----------------------------------------";

                        // Update messages array list
                        MainActivity.messages.add(0, copyMessages.get(0).toString() + usersMessage);

                        // Update messages in database by sending request to database server
                        JSONObject userinfo = new JSONObject();
                        try {
                            userinfo.put("opcode", "19");
                            userinfo.put("username", username);
                            userinfo.put("UserMessages", usersMessage);
                            userinfo.put("ContactMessages", "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("AppDebug", "Error! " + e.toString());
                        }
                        Sender = new sender();
                        Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}