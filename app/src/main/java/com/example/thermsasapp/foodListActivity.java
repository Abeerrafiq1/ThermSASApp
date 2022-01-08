package com.example.thermsasapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class foodListActivity extends AppCompatActivity {
    static ArrayList<String> foodList = new ArrayList<>();
    public static Handler exHandler;
    Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.food_list_activity);

        RecyclerView recyclerV = (RecyclerView) findViewById(R.id.analysis_list);
        notifyAdapter adapterC = new notifyAdapter(foodListActivity.foodList);
        recyclerV.setAdapter(adapterC);
        recyclerV.setLayoutManager(new LinearLayoutManager(this));


    }


}
