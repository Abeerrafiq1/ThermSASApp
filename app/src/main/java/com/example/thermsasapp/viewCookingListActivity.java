package com.example.thermsasapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class viewCookingListActivity extends AppCompatActivity {
    public static Handler exHandler;
    Context mContext = this;
    private TableView tb_v;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_cooking_list_activity);

        String[] titles = {"     #", "               TABLE NAME"};
        tb_v = (TableView) findViewById(R.id.analysisTable);
        tb_v.setHeaderBackgroundColor(Color.parseColor("#D2C5EA"));
        tb_v.setHeaderAdapter(new SimpleTableHeaderAdapter(mContext, titles));
        tb_v.setColumnCount(2);

        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(mContext, 2);
        columnModel.setColumnWidth(0, 70);
        columnModel.setColumnWidth(1, 800);
        tb_v.setColumnModel(columnModel);

        EditText tableNumber = (EditText) findViewById(R.id.editTextEnterNumber);
        Button viewDataBtn = (Button) findViewById(R.id.viewData);

        Intent intent = getIntent();
        String currentUser = intent.getStringExtra("currentUser");


        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ArrayList<String[]> foodList = new ArrayList<>();
                try {
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String videoList = obj.getString("videoList");
                    String[] record = {""};
                    final JSONObject[] object = new JSONObject[1];
                    final String[][] item = new String[1][1];
                    if (videoList.equals("[]")) {
                        Toast.makeText(mContext, "Sorry no videos recorded on registered stove", Toast.LENGTH_SHORT).show();
                    } else if (videoList.equals("")) {
                        Toast.makeText(mContext, "Sorry no stove registered", Toast.LENGTH_SHORT).show();
                    } else {
                        videoList = videoList.replace("[", "");
                        videoList = videoList.replace("]", "");
                        videoList = videoList.replaceAll("'", "\"");
                        videoList = videoList.substring(0, videoList.length() - 1);
                        record = videoList.split(Pattern.quote("}, "));
                        for (int i = 0; i < record.length; i++) {
                            record[i] = record[i] + "}";
                            object[0] = new JSONObject((String) record[i]);
                            item[0] = new String[]{object[0].getString("id") + " :", object[0].getString("analysis_table_name")};
                            foodList.add(item[0]);
                        }
                    }
                    SSimpleTableDataAdapter simple = new SSimpleTableDataAdapter(viewCookingListActivity.this, foodList);
                    simple.setTextSize(13);
                    tb_v.setDataAdapter(simple);

                    String finalVideoList = videoList;
                    String[] finalRecord = record;
                    viewDataBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tb_number = tableNumber.getText().toString();
                            if (finalVideoList.equals("") ){
                                Toast.makeText(mContext, "Sorry no stove registered", Toast.LENGTH_SHORT).show();
                            } else if (finalVideoList.equals("[]")) {
                                Toast.makeText(mContext, "Sorry no videos recorded on registered stove", Toast.LENGTH_SHORT).show();
                            } else {
                                    String tableNmToLookup = "";
                                    try {
                                        for (int i = 0; i < finalRecord.length; i++) {
                                            finalRecord[i] = finalRecord[i] + "}";
                                            object[0] = new JSONObject((String) finalRecord[i]);
                                            item[0] = new String[]{object[0].getString("id") + " :",object[0].getString( "analysis_table_name")};
                                            if (tb_number.equals(object[0].getString("id"))){
                                                tableNmToLookup = object[0].getString( "analysis_table_name");
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (!tableNmToLookup.equals("")) {
                                        JSONObject userinfo = new JSONObject();
                                        try {
                                            userinfo.put("opcode", "17");
                                            userinfo.put("username", currentUser);
                                            userinfo.put("tableName", tableNmToLookup);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Sender = new sender();
                                        Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                                        Intent intent = new Intent(viewCookingListActivity.this, viewStoveDataActivity.class);
                                        intent.putExtra("username", currentUser);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(mContext, "Sorry table ID entered is invalid", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        };
    }


}
