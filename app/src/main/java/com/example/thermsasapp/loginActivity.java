package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 @author: Abeer Rafiq

 Purpose of Class: When a user wants to login, they will enter thier username and password.
 The entered password will be checked against the password stored in the database for the user.
 If passwords match, the user can login otherwise they are denied access.
 */
public class loginActivity extends AppCompatActivity {

    // Class variables
    private Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private EditText editTextUsername, editTextPassword;
    private Button login_button2;
    public static Handler exHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Two editText instances to enter passwords and username
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        // Login button to trigger app to login
        login_button2 = (Button) findViewById(R.id.loginbutton2);

        // If login requested, send database server username to retrieve stored password
        login_button2.setOnClickListener(v -> {
            JSONObject userinfo = new JSONObject();
            try {
                userinfo.put("opcode", "1");
                userinfo.put("username", editTextUsername.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("AppDebug", "Error! " + e.toString());
            }
            Sender = new sender();
            Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);
        });

        // After database server sends stored password of user, compare it against user entered password
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message dbpassword) {
                super.handleMessage(dbpassword);
                try {
                    // Extract password and notifications from received message
                    JSONObject obj = new JSONObject((String) dbpassword.obj);
                    String password = obj.getString("password");
                    String notifications = obj.getString("notifications");
                    String onTooLongNotif = "false";

                    // If the password matches the user entered password, let user login, otherwise show on app incorrect credentials
                    if (!editTextPassword.getText().toString().isEmpty() && !editTextUsername.getText().toString().isEmpty() && password.equals(editTextPassword.getText().toString())) {
                        // Show authenticated
                        Toast.makeText(mContext, "Authenticated", Toast.LENGTH_SHORT).show();

                        // Update user's notifications
                        MainActivity.notifications.clear();
                        MainActivity.notifications.add(0, notifications);

                        // If there is a notification that needs immediate attention, the next view (main options page) must show a pop up
                        if (notifications.contains("has the stove on too long! Please make sure everything is okay")){
                            onTooLongNotif = "true-contact";
                        } else if (notifications.contains("Your stove was on too long!")){
                            onTooLongNotif = "true-owner";
                        }

                        // Start mainOptionsActivity to give five options
                        Intent intent = new Intent(loginActivity.this, mainOptionsActivity.class);
                        intent.putExtra("currentUser", editTextUsername.getText().toString());
                        intent.putExtra("onTooLong", onTooLongNotif);
                        startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}
