package com.example.thermsasapp;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.os.StrictMode;

public class MainActivity extends AppCompatActivity {
    private receiver Receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(android.os.Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {
            Receiver = new receiver();
            Receiver.start();
        } catch (Exception e) {
            String str = e.toString();
        }

        Button login_button = (Button) findViewById(R.id.loginbutton);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, loginActivity.class));
            }

        });
    }
}