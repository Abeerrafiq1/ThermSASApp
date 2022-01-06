package com.example.thermsasapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class addSubscribersActivity extends AppCompatActivity {
    Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    public static Handler exHandler;

    public addSubscribersActivity(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_subcribers_activity);

        EditText physician_textview = (EditText) findViewById(R.id.physicianText);
        EditText contactOne_textview = (EditText) findViewById(R.id.contactOneText);
        EditText contactTwo_textview = (EditText) findViewById(R.id.contactTwoText);
        EditText contactThree_textview = (EditText) findViewById(R.id.contactThreeText);
        Button add_contacts = (Button) findViewById(R.id.addContacts);
        Button add_Physician = (Button) findViewById(R.id.addPhysician);

        Intent intent = getIntent();
        String currentUser = intent.getStringExtra("currentUser");

        add_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "5");
                    userinfo.put("currentUser", currentUser);
                    userinfo.put("contactOne", contactOne_textview.getText().toString());
                    userinfo.put("contactTwo", contactTwo_textview.getText().toString());
                    userinfo.put("contactThree", contactThree_textview.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);
            }

        });

        add_Physician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "6");
                    userinfo.put("currentUser", currentUser);
                    userinfo.put("physician", physician_textview.getText().toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);

            }

        });

        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String opcode = obj.getString("opcode");

                    if (opcode.equals("7")) {
                        String contactOne = obj.getString("contactOne");
                        String contactTwo = obj.getString("contactTwo");
                        String contactThree = obj.getString("contactThree");

                        if (contactOne.equals("1") || contactTwo.equals("1") || contactThree.equals("1") ){
                            Toast.makeText(mContext, "You have entered a username doesn't Exist. Please re-enter all usernames.", Toast.LENGTH_SHORT).show();
                        }
                        else if(contactOne.equals("2")  || contactTwo.equals("2") || contactThree.equals("2")){
                            Toast.makeText(mContext, "You can't subscribe yourself. Please re-enter all usernames.", Toast.LENGTH_SHORT).show();
                        }
                        else if(contactOne.equals("3") || contactTwo.equals("3") || contactThree.equals("3")){
                            Toast.makeText(mContext, "Repeated Contacts not allowed. Please re-enter all usernames.", Toast.LENGTH_SHORT).show();
                        }
                        else if (contactOne.equals("5") && contactTwo.equals("5") && contactThree.equals("5")){
                            Toast.makeText(mContext, "All subscribers cleared.", Toast.LENGTH_SHORT).show();
                        }
                        else if (contactOne.equals("4") && contactTwo.equals("4") && contactThree.equals("4")){
                            Toast.makeText(mContext, "Subscribers added successfully.", Toast.LENGTH_SHORT).show();
                        }
                        startActivity(new Intent(getApplicationContext(), popActivity.class));
                    } else if (opcode.equals("8")) {
                        String physician = obj.getString("physician");
                        if (physician.equals("1")) {
                            Toast.makeText(mContext, "You have entered a username doesn't exist. Please re-enter username.", Toast.LENGTH_SHORT).show();
                        }
                        else if (physician.equals("2")){
                            Toast.makeText(mContext, "The username entered is not a physician. Please re-enter username.", Toast.LENGTH_SHORT).show();
                        }
                        else if(physician.equals("3")){
                            Toast.makeText(mContext, "You can't subscribe yourself. Please re-enter username.", Toast.LENGTH_SHORT).show();
                        }
                        else if(physician.equals("5")){
                            Toast.makeText(mContext, "Physician cleared.", Toast.LENGTH_SHORT).show();
                        }
                        else if (physician.equals("4")){
                            Toast.makeText(mContext, "Physician added successfully.", Toast.LENGTH_SHORT).show();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        };
    }
}
