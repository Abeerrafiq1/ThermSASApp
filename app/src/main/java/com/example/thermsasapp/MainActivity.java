package com.example.thermsasapp;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.os.StrictMode;
import java.util.ArrayList;

/**
 @author: Abeer Rafiq

 Purpose of Class: When the app is first initialized, it is directed to this class.
 The class gives a login and register button option to log in.
 */
public class MainActivity extends AppCompatActivity {

    // Class variables
    private receiver Receiver;
    public static messageRetriever recMsgs;
    private Button login_button, register_button;
    static ArrayList<String> messages = new ArrayList<>();

    // When a user goes back from the loginActivity, they go to this MainActivity
    // Make sure messageRetriever (to retrieve user messages) is properly initialized
    protected void onRestart() {
        super.onRestart();
        // Initialize a receiver to receive messages
        try {
            recMsgs = new messageRetriever("NoUser");
        } catch (Exception e) {
            String str = e.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("AppDebug", "App is Initialized (Note: DS = Database Server)");


        // Important for the app to communicate with the database server
        if(android.os.Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // If user wants to stop pop ups/phone notifications or start them again
        messageActivity.stopPopUp_button= (Button) findViewById(R.id.stopPopUps);
        messageActivity.startPopUp_button = (Button) findViewById((R.id.startPopUps));


        // Initialize a receiver to only receive a user's messages (polls for them)
        try {
            recMsgs = new messageRetriever("NoUser");
        } catch (Exception e) {
            String str = e.toString();
        }

        // Initialize a receiver to receive messages from database server
        try {
            Receiver = new receiver();
            Receiver.start();
        } catch (Exception e) {
            String str = e.toString();
        }

        // Login and register buttons
        login_button = (Button) findViewById(R.id.loginbutton);
        register_button = (Button) findViewById(R.id.registerButton);

        // If login requested, start loginActivity
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, loginActivity.class));
            }
        });

        // If registration requested, start registerActivity
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, registerActivity.class));
            }
        });
    }
}