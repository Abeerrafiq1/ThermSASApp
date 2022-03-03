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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

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
                        startActivity(new Intent(registerActivity.this, loginActivity.class));
                    } else {
                        Toast.makeText(mContext, "User Already Exists!\n Please use a different Username", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}
