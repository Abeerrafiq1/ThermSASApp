package com.example.thermsasapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button viewdata_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewdata_button = findViewById(R.id.button); //view data
        viewdata_button.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "view data", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, ViewDataActivity.class));
        });
    }
}