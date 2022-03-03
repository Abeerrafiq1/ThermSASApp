package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

/**
 @author: Abeer Rafiq

 Purpose of Class: When a user wants to logout, this class redirects the view
 to the main activity page showing login and register buttons.
 The receiver isn't initialized again since it is still running from MainActivity
 */
public class logoutActivity extends AppCompatActivity {

    // Class variables
    private Button login_button, register_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Login and register buttons
        login_button = (Button) findViewById(R.id.loginbutton);
        register_button = (Button) findViewById(R.id.registerButton);

        // If login requested, start loginActivity
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(logoutActivity.this, loginActivity.class));
            }
        });

        // If registration requested, start registerActivity
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(logoutActivity.this, registerActivity.class));
            }
        });
    }
}
