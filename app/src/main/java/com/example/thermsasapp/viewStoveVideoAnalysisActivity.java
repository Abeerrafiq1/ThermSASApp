package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.model.TableColumnDpWidthModel;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

/**
 @author: Abeer Rafiq

 Purpose of Class: To display the table data that corresponds to a recorded video of the registered stove.
 It requests the database server to send the table data and then displays it in a table format.
 */
public class viewStoveVideoAnalysisActivity extends AppCompatActivity{

    // Class Variables
    private Context mContext = this;
    public static Handler exHandler;
    private TableView tb_v;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_stove_video_analysis_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // Set headers of the table (7 columns)
        String[] titles = {" Time Elapsed", "Classification", "  Pan Area", "  Pan Temp", "Number Food", "       Food Area", "       Food Temp"};
        tb_v = (TableView) findViewById(R.id.stoveData);
        tb_v.setHeaderBackgroundColor(Color.parseColor("#D2C5EA"));
        tb_v.setHeaderAdapter(new SimpleTableHeaderAdapter(mContext, titles));
        tb_v.setColumnCount(7);

        // Set width of the table columns
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(mContext, 7, 150);
        columnModel.setColumnWidth(0, 150);
        columnModel.setColumnWidth(1, 150);
        columnModel.setColumnWidth(2, 150);
        columnModel.setColumnWidth(3, 150);
        columnModel.setColumnWidth(4, 150);
        columnModel.setColumnWidth(5, 400);
        columnModel.setColumnWidth(6, 400);
        tb_v.setColumnModel(columnModel);

        // When database server sends a message containing the data for a stove analysis table
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // ArrayList to store the stove analysis table data
                ArrayList<String[]> analysisTable = new ArrayList<>();

                try {
                    // Extract the video list from received message and initialize variables
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String data = obj.getString("data");
                    boolean isOnTooLong = false;
                    String[] record = {""};
                    final JSONObject[] object = new JSONObject[1];
                    final String[][] item = new String[1][1];

                    // Parse the data to add each row of data into the analysisTable arrayList
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

                            // If classification is determined to risky, set isOnTooLong to true to send corresponding messages (last if statement of this code)
                            if (object[0].getString("classification").equals("on too long")){
                                isOnTooLong = true;
                            }
                        }
                    }
                    // To show data corresponding to end of video first
                    Collections.reverse(analysisTable);

                    // Update the table data adapter to contain the table data and update table in app's view
                    SimpleTableDataAdapter simple = new SimpleTableDataAdapter(viewStoveVideoAnalysisActivity.this, analysisTable);
                    simple.setTextSize(15);
                    simple.setPaddings(100, 15, 20, 15);
                    tb_v.setDataAdapter(simple);

                    // If classification is determined to risky, update notifications
                    if (isOnTooLong){
                        //Create a copy of the messages ArrayList
                        ArrayList<String> copyMessages = new ArrayList<>();
                        copyMessages.add(MainActivity.messages.get(0));

                        // Create messages and update messages in ArrayList
                        MainActivity.messages.clear();
                        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String formattedTimeStamp = sdf3.format(timestamp);
                        String usersMessage = "\n\n [" + formattedTimeStamp + "]\n\nYour stove was on too long!\nMessages have been sent to your contacts\n\n-----------------------------------------";
                        String contactMessage = "\n\n [" + formattedTimeStamp + "]\n\nStove owner with username * " + username + " * has the stove on too long! Please make sure everything is okay\n\n-----------------------------------------";
                        MainActivity.messages.add(0, copyMessages.get(0).toString() + usersMessage);

                        // Send database server message to update messages of user and contacts in the database server
                        JSONObject userinfo = new JSONObject();
                        try {
                            userinfo.put("opcode", "19");
                            userinfo.put("username", username);
                            userinfo.put("UserMessages", usersMessage);
                            userinfo.put("ContactMessages", contactMessage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("AppDebug", "Error! " + e.toString());
                        }
                        Sender = new sender();
                        Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}