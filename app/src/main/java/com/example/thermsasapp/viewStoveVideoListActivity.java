package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

/**
 @author: Abeer Rafiq

 Purpose of Class: To display the list of stove analysis tables that each correspond to a recorded video of the registered stove.
 It requests the database server to send the list and then displays it in a table format.
 */
public class viewStoveVideoListActivity extends AppCompatActivity {

    // Class Variables
    public static Handler exHandler;
    private Context mContext = this;
    private TableView tb_v;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private EditText tableID;
    private Button viewDataBtn;
    private static final int senderPort = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_stove_video_list_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // Set headers of the table (2 columns)
        String[] titles = {"   ID", "               TABLE NAME"};
        tb_v = (TableView) findViewById(R.id.analysisTable);
        tb_v.setHeaderBackgroundColor(Color.parseColor("#D2C5EA"));
        tb_v.setHeaderAdapter(new SimpleTableHeaderAdapter(mContext, titles));
        tb_v.setColumnCount(2);

        // Set width of the table columns
        TableColumnDpWidthModel columnModel = new TableColumnDpWidthModel(mContext, 2);
        columnModel.setColumnWidth(0, 70);
        columnModel.setColumnWidth(1, 800);
        tb_v.setColumnModel(columnModel);

        // EditText to enter table id to view the table
        tableID = (EditText) findViewById(R.id.editTextEnterID);
        // Button to trigger view a certain table's data after table id entered
        viewDataBtn = (Button) findViewById(R.id.viewData);

        // When database server sends a message containing the list of stove analysis tables that associate with registered stove
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // ArrayList to store the stove analysis table names
                ArrayList<String[]> stoveVideoList = new ArrayList<>();
                try {
                    // Extract the video list from received message and initialize variables
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String videoList = obj.getString("videoList");
                    String[] record = {""};
                    final JSONObject[] object = new JSONObject[1];
                    final String[][] item = new String[1][1];

                    // If the video list is empty array, show corresponding message
                    if (videoList.equals("[]")) {
                        Toast.makeText(mContext, "Sorry no videos recorded on registered stove", Toast.LENGTH_SHORT).show();
                    }
                    // If video list = "" it means no stove is registered, show corresponding messages
                    else if (videoList.equals("")) {
                        Toast.makeText(mContext, "Sorry no stove registered", Toast.LENGTH_SHORT).show();
                    }
                    // Otherwise parse the video list to add each table id and table name to the stoveVideoList arrayList
                    else {
                        videoList = videoList.replace("[", "");
                        videoList = videoList.replace("]", "");
                        videoList = videoList.replaceAll("'", "\"");
                        videoList = videoList.substring(0, videoList.length() - 1);
                        record = videoList.split(Pattern.quote("}, "));

                        for (int i = 0; i < record.length; i++) {
                            record[i] = record[i] + "}";
                            object[0] = new JSONObject((String) record[i]);
                            item[0] = new String[]{object[0].getString("id") + " :", object[0].getString("analysis_table_name")};
                            stoveVideoList.add(item[0]);
                        }
                    }

                    // Update the table data adapter to contain the video lists and update table in app's view
                    SimpleTableDataAdapter simple = new SimpleTableDataAdapter(viewStoveVideoListActivity.this, stoveVideoList);
                    simple.setPaddings(100, 15, 20, 15);
                    simple.setTextSize(13);
                    tb_v.setDataAdapter(simple);

                    // If user has requested to view a certain stove analysis table's data by entering table's id
                    String finalVideoList = videoList;
                    String[] finalRecord = record;
                    viewDataBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tb_ID = tableID.getText().toString();

                            // Show corresponding message if user tries to view a stove analysis table but there is no corresponding table with the
                            // registered stove or stove isn't registered yet
                            if (finalVideoList.equals("") ){
                                Toast.makeText(mContext, "Sorry no stove registered", Toast.LENGTH_SHORT).show();
                            }
                            else if (finalVideoList.equals("[]")) {
                                Toast.makeText(mContext, "Sorry no videos recorded on registered stove", Toast.LENGTH_SHORT).show();
                            }
                            // There are stove video tables associated with stove registered
                            else {
                                // Iterate though video list that was retrieved before "view data" button was pressed
                                // to get the table name that corresponds with the table id entered
                                String tableNmToLookup = "";
                                try {
                                    for (int i = 0; i < finalRecord.length; i++) {
                                        finalRecord[i] = finalRecord[i] + "}";
                                        object[0] = new JSONObject((String) finalRecord[i]);
                                        item[0] = new String[]{object[0].getString("id") + " :",object[0].getString( "analysis_table_name")};

                                        if (tb_ID.equals(object[0].getString("id"))){
                                            tableNmToLookup = object[0].getString( "analysis_table_name");
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.d("AppDebug", "Error! " + e.toString());
                                }

                                // If table id entered is a valid table id (corresponding table exists) then send the database server
                                // a request to retrieve that table's data and start the viewStoveVideoAnalysisActivity
                                if (!tableNmToLookup.equals("")) {
                                    JSONObject userinfo = new JSONObject();
                                    try {
                                        userinfo.put("opcode", "17");
                                        userinfo.put("username", username);
                                        userinfo.put("tableName", tableNmToLookup);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.d("AppDebug", "Error! " + e.toString());
                                    }
                                    Sender = new sender();
                                    Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

                                    // Start the viewStoveVideoAnalysisActivity
                                    Intent intent = new Intent(viewStoveVideoListActivity.this, viewStoveVideoAnalysisActivity.class);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                }
                                // If table id entered is invalid (no such table exists), then show corresponding message
                                else {
                                    Toast.makeText(mContext, "Sorry table ID entered is invalid", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    // If user wants to see details about how viewing stove analysis tables works
                    Button details_button = (Button) findViewById(R.id.viewData2);
                    details_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent2 = new Intent(viewStoveVideoListActivity.this, detailPopUpActivity.class);
                            intent2.putExtra("height", "0.60");
                            intent2.putExtra("popupText", "\n\n\n\n\n\n\nABOUT STOVE ANALYSIS TABLES: " +
                                    "\n\n * Each analysis table corresponds to a recorded stove video" +
                                    "\n * If no stove is registered, this list will remain empty " +
                                    "\n * If no recorded videos for registered stove, list will be empty " +
                                    "\n\n ENTERING TABLE ID " +
                                    "\n\n * Must have a stove registered with videos recorded " +
                                    "\n * Must enter a table ID from list provided on page" +
                                    "\n\n\n    * SWIPE POP UP RIGHT TO CLOSE IT *  ");
                            startActivity(intent2);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}
