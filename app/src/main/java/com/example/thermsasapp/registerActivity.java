package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 @author: Abeer Rafiq

 Purpose of Class: When a user wants to register, this class registers the user by
 sending a request to the database server to add the user to the database with user information.
 It then redirects the view to the login view.
 */
public class registerActivity extends AppCompatActivity {

    // Class variables
    private Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private EditText enteredUsername, enteredPassword;
    private CheckBox licensedPhysician_checkbox;
    private Button register_button;
    public static Handler exHandler;

    // When a user goes back to this activity make sure messageRetriever (to retrieve user messages)
    // is closed since user is logged out
    protected void onRestart() {
        super.onRestart();
        messageRetriever.udpDatagramSocket.close();
        MainActivity.recMsgs.exitThread(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        // To handle continuous retrieval of user messages (notifications) and associating pop ups
        messageRetriever.udpDatagramSocket.close();
        MainActivity.recMsgs.exitThread(true);

        // Two editText instances to enter passwords and username
        enteredUsername = (EditText) findViewById(R.id.enterUsername);
        enteredPassword = (EditText) findViewById(R.id.enterPassword);
        // Check box to identify if user is a physician
        licensedPhysician_checkbox = (CheckBox) findViewById(R.id.licensedPhysicianCheckbox);
        // Register button to trigger app to registration
        register_button = (Button) findViewById(R.id.registerB);

        // If registration is requested (and entered password/username is not blank)
        // send database server request to add username to database
        register_button.setOnClickListener(v -> {
            // If blank username and password entered, they are invalid
            if (enteredPassword.getText().toString().isEmpty() || enteredUsername.getText().toString().isEmpty()){
                Toast.makeText(mContext, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                enteredUsername.setText("");
                enteredPassword.setText("");
            } else {
                // Send request to database server
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "3");
                    userinfo.put("username", enteredUsername.getText().toString());
                    userinfo.put("password", enteredPassword.getText().toString());
                    userinfo.put("physician", licensedPhysician_checkbox.isChecked());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);
            }
        });

        // If user wants to see details about registration
        Button details_button = (Button) findViewById(R.id.details7);
        details_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(registerActivity.this, detailPopUpActivity.class);
                intent2.putExtra("height", "0.4");
                intent2.putExtra("popupText", "\n\n\n\n\n\n\n\n\n\nABOUT REGISTRATION: " +
                        "\n\n * You must enter a username that doesn't already exist and that isn't blank" +
                        "\n * You will be re-directed to login page after registration" +
                        "\n * If you are a licensed physician, then click on the check box" +
                        "\n\n    * SWIPE POP UP RIGHT TO CLOSE IT *  ");
                startActivity(intent2);
            }
        });

        // If database server noticed that the username is already registered, then registration failed
        // The user has to enter a different username to register
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message valid) {
                super.handleMessage(valid);
                try {
                    // Extract if valid username (username not registered already) from received message
                    JSONObject obj = new JSONObject((String) valid.obj);
                    String validity = obj.getString("valid");

                    // If valid username, redirect activity to loginActivity, else show message to enter new username
                    if (validity.equals("yes")){
                        Toast.makeText(mContext, "Redirecting to Login", Toast.LENGTH_SHORT).show();
                        enteredUsername.setText("");
                        enteredPassword.setText("");
                        startActivity(new Intent(registerActivity.this, loginActivity.class));
                    } else {
                        Toast.makeText(mContext, "User Already Exists!\n Please use a different Username", Toast.LENGTH_SHORT).show();
                        enteredUsername.setText("");
                        enteredPassword.setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}
