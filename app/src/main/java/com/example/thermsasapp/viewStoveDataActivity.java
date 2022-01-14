package com.example.thermsasapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class viewStoveDataActivity extends AppCompatActivity{
    Context mContext = this;
    public static Handler exHandler;
    private TableView tb_v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_stove_data_activity);

        String[] titles = {"", "", "", "", "", ""};
        tb_v = (TableView) findViewById(R.id.analysisTable);
        tb_v.setHeaderBackgroundColor(Color.parseColor("#D2C5EA"));
        tb_v.setHeaderAdapter(new SimpleTableHeaderAdapter(mContext, titles));
        tb_v.setColumnCount(6);

        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ArrayList<String[]> analysisTable = new ArrayList<>();
                try {
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String data = obj.getString("data");
                    String[] record = {""};
                    final JSONObject[] object = new JSONObject[1];
                    final String[][] item = new String[1][1];
                    if (!data.equals("")){
                        data = data.replace("[", "");
                        data = data.replace("]", "");
                        data = data.replaceAll("'", "\"");
                        data = data.substring(0, data.length() - 1);
                        record = data.split(Pattern.quote("}, "));
                        for (int i = 0; i < record.length; i++) {
                            record[i] = record[i] + "}";
                            object[0] = new JSONObject((String) record[i]);
                            item[0] = new String[]{object[0].getString("time_elapsed"), object[0].getString("pan_temp"),
                                    object[0].getString("pan_area"), object[0].getString("num_food"),
                                    object[0].getString("food_temp"), object[0].getString("food_area")};
                            analysisTable.add(item[0]);
                        }
                    }
                    SSimpleTableDataAdapter simple = new SSimpleTableDataAdapter(viewStoveDataActivity.this, analysisTable);
                    simple.setTextSize(13);
                    tb_v.setDataAdapter(simple);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}