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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Pattern;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class viewStoveDataActivity extends AppCompatActivity{
    Context mContext = this;
    public static Handler exHandler;
    private TableView tb_v;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_stove_data_activity);

        String[] titles = {" Time Elapsed", "Classification", "  Pan Area", "  Pan Temp", "Number Food", "       Food Area", "       Food Temp"};
        tb_v = (TableView) findViewById(R.id.stoveData);
        tb_v.setHeaderBackgroundColor(Color.parseColor("#D2C5EA"));
        tb_v.setHeaderAdapter(new SimpleTableHeaderAdapter(mContext, titles));
        tb_v.setColumnCount(7);

        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(mContext, 7, 150);
        columnModel.setColumnWidth(0, 150);
        columnModel.setColumnWidth(1, 150);
        columnModel.setColumnWidth(2, 150);
        columnModel.setColumnWidth(3, 150);
        columnModel.setColumnWidth(4, 150);
        columnModel.setColumnWidth(5, 400);
        columnModel.setColumnWidth(6, 400);
        tb_v.setColumnModel(columnModel);

        Intent intent = getIntent();
        String currentUser = intent.getStringExtra("username");

        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ArrayList<String[]> analysisTable = new ArrayList<>();
                boolean isOnTooLong = false;
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
                             item[0] = new String[]{object[0].getString("time_elapsed"), object[0].getString("classification"), object[0].getString("pan_area"),
                                     object[0].getString("pan_temp"), object[0].getString("num_food"),
                                     "["+object[0].getString("food_area")+"]", "["+object[0].getString("food_temp")+"]"};
                            analysisTable.add(item[0]);
                            if (object[0].getString("classification").equals("on too long")){
                                isOnTooLong = true;
                            }
                        }
                    }
                    Collections.reverse(analysisTable);
                    SSimpleTableDataAdapter simple = new SSimpleTableDataAdapter(viewStoveDataActivity.this, analysisTable);
                    simple.setTextSize(15);
                    tb_v.setDataAdapter(simple);

                    if (isOnTooLong){
                        ArrayList<String> copyNotifications = new ArrayList<>();
                        copyNotifications.add(MainActivity.notifications.get(0));
                        startActivity(new Intent(getApplicationContext(), popActivity.class));
                        MainActivity.notifications.clear();

                        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String formattedTimeStamp = sdf3.format(timestamp);
                        String usersNotification = "\n\n [" + formattedTimeStamp + "]\n\nYour stove was on too long!\nNotifications have been sent to your contacts\n\n-----------------------------------------";
                        String contactNotification = "\n\n [" + formattedTimeStamp + "]\n\nStove owner with username * " + currentUser + " * has the stove on too long! Please make sure everything is okay\n\n-----------------------------------------";
                        MainActivity.notifications.add(0, copyNotifications.get(0).toString() + usersNotification);
                        JSONObject userinfo = new JSONObject();
                        try {
                            userinfo.put("opcode", "19");
                            userinfo.put("username", currentUser);
                            userinfo.put("UserNotifications", usersNotification);
                            userinfo.put("ContactNotifications", contactNotification);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Sender = new sender();
                        Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}