package com.example.thermsasapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class notificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_activity);

        RecyclerView recyclerV = (RecyclerView) findViewById(R.id.recylerV);
        notifyAdapter adapterC = new notifyAdapter(MainActivity.notifications);
        recyclerV.setAdapter(adapterC);
        recyclerV.setLayoutManager(new LinearLayoutManager(this));

    }
}