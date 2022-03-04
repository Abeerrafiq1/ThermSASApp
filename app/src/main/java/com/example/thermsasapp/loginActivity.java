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
import android.view.View;
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

        // If user wants to see details about login
        Button details_button = (Button) findViewById(R.id.details5);
        details_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(loginActivity.this, detailPopUpActivity.class);
                intent2.putExtra("height", "0.3");
                intent2.putExtra("popupText", "\n\n\n\n\n\n\n\n\n\n\nABOUT LOGIN: " +
                        "\n\n * Username and password are both case sensitive" +
                        "\n * Do not add extra spaces" +
                        "\n\n    * SWIPE POP UP RIGHT TO CLOSE IT *  ");
                startActivity(intent2);
            }
        });


        // After database server sends stored password of user, compare it against user entered password
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message dbpassword) {
                super.handleMessage(dbpassword);
                try {
                    // Extract password and messages from received message
                    JSONObject obj = new JSONObject((String) dbpassword.obj);
                    String password = obj.getString("password");
                    String messages = obj.getString("messages");
                    String onTooLongNotif = "false";

                    // If the password matches the user entered password, let user login, otherwise show on app incorrect credentials
                    if (!editTextPassword.getText().toString().isEmpty() && !editTextUsername.getText().toString().isEmpty() && password.equals(editTextPassword.getText().toString())) {
                        // Show authenticated
                        Toast.makeText(mContext, "Authenticated", Toast.LENGTH_SHORT).show();

                        // Update user's messages
                        MainActivity.messages.clear();
                        MainActivity.messages.add(0, messages);

                        // If there is a message that needs immediate attention, the next view (main options page) must show a pop up
                        if (messages.contains("has the stove on too long! Please make sure everything is okay")){
                            onTooLongNotif = "true-contact";
                        } else if (messages.contains("Your stove was on too long!")){
                            onTooLongNotif = "true-owner";
                        }

                        // Start mainOptionsActivity to give five options
                        Intent intent = new Intent(loginActivity.this, mainOptionsActivity.class);
                        intent.putExtra("username", editTextUsername.getText().toString());
                        intent.putExtra("onTooLong", onTooLongNotif);
                        // Clear editTexts so if you go back from main options activity, credentials have to be re-entered
                        editTextUsername.setText("");
                        editTextPassword.setText("");
                        startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                        // Clear edit texts so credentials have to be re-entered since they are wrong
                        editTextUsername.setText("");
                        editTextPassword.setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}
