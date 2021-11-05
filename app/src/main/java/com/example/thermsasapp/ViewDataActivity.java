package com.example.thermsasapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;



public class ViewDataActivity extends AppCompatActivity {

    private Button goBack;

    public ViewDataActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewdata_activity);

        goBack = findViewById(R.id.button2);
        goBack.setOnClickListener(v -> {
            Toast.makeText(ViewDataActivity.this, "Go Back", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ViewDataActivity.this, MainActivity.class));
        });

    }

}
