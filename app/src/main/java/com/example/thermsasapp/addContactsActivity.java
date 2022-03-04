package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 @author: Abeer Rafiq

 Purpose of Class: To add contacts such as relatives, friends or a physcician to stove owner's account.
 The contacts that are not a physician will receive messages about the stove owner's account.
 All contacts will receive messages once they have been added to the stove owner's account.
 The stove owner will receive a message that the contacts have been updated.
 */
public class addContactsActivity extends AppCompatActivity {

    // Class variables
    private Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private EditText physician_editText, contactOne_editText, contactTwo_editText, contactThree_editText;
    private Button add_contacts, add_Physician;
    public static Handler exHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contacts_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        // EditText to enter one physician
        physician_editText = (EditText) findViewById(R.id.physicianText);
        // EditText to enter up to three contacts
        contactOne_editText = (EditText) findViewById(R.id.contactOneText);
        contactTwo_editText = (EditText) findViewById(R.id.contactTwoText);
        contactThree_editText = (EditText) findViewById(R.id.contactThreeText);
        // Buttons to trigger adding contacts or a physician to a account
        add_contacts = (Button) findViewById(R.id.addContacts);
        add_Physician = (Button) findViewById(R.id.addPhysician);

        // If user requests to add contacts, send database server a request to add them
        add_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "5");
                    userinfo.put("username", username);
                    userinfo.put("contactOne", contactOne_editText.getText().toString());
                    userinfo.put("contactTwo", contactTwo_editText.getText().toString());
                    userinfo.put("contactThree", contactThree_editText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);
            }
        });

        // If user requests to add a physician, send database server a request to add the physician
        add_Physician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "6");
                    userinfo.put("username", username);
                    userinfo.put("physician", physician_editText.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);
            }
        });

        // If user wants to see details about adding contacts
        Button details_button = (Button) findViewById(R.id.details);
        details_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(addContactsActivity.this, detailPopUpActivity.class);
                intent2.putExtra("height", "0.85");
                intent2.putExtra("popupText", "\n\nAfter adding a contact, contact is notified that they are added to your account\n\nPHYSICIAN CONTACTS: " +
                        "\n * To register a physician, enter username that is registered as a physician (can't be yourself)" +
                        "\n * To clear a physician, leave field empty and press ADD PHYSICIAN button" +
                        "\n * The physician contact will receive messages about cooking trends " +
                        "\n\n PRIMARY CONTACTS " +
                        "\n * Only three contacts allowed per account, primary contacts can't be repeated, contacts must be registered, can't register yourself and each time three contacts are added, previous ones are cleared" +
                        "\n * Primary contacts can be left empty " +
                        "\n * Primary contacts receive messages in the case of a stove emergency  " +
                        "\n * To clear all primary contacts, leave all primary contact fields empty and press ADD PRIMARY CONTACTS button" +
                        "\n\n    * SWIPE POP UP RIGHT TO CLOSE IT *  ");
                startActivity(intent2);
            }
        });

        // Opcode 7:
        // When database server sends a message for successful/unsuccessful contact registration
        // If contacts determined to be successfully added, send message to stove owner and those contacts
        // Opcode 8:
        // When database server sends a message for successful/unsuccessful physician registration
        // If physician determined to be successfully added, send message to stove owner and the physician
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    // Extract opcode from received message and initialize variables
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String opcode = obj.getString("opcode");
                    Boolean sendContactNotif = false;
                    Boolean sendPhysicianNotif = false;

                    // If opcode = 7, the database server has sent a response for successful/unsuccessful contact registration
                    // Show corresponding messages to user
                    if (opcode.equals("7")) {
                        String contactOne = obj.getString("contactOne");
                        String contactTwo = obj.getString("contactTwo");
                        String contactThree = obj.getString("contactThree");

                        // Unsuccessful contact registration : If username of contacts to be added doesn't exist in database
                        if (contactOne.equals("1") || contactTwo.equals("1") || contactThree.equals("1") ){
                            Toast.makeText(mContext, "You have entered a username doesn't Exist. Please re-enter all usernames.", Toast.LENGTH_SHORT).show();
                        }
                        // Unsuccessful contact registration : If username of contacts includes stove owner themself
                        else if(contactOne.equals("2")  || contactTwo.equals("2") || contactThree.equals("2")){
                            Toast.makeText(mContext, "You can't add yourself. Please re-enter all usernames.", Toast.LENGTH_SHORT).show();
                        }
                        // Unsuccessful contact registration : If contacts are repeated
                        else if(contactOne.equals("3") || contactTwo.equals("3") || contactThree.equals("3")){
                            Toast.makeText(mContext, "Repeated Contacts not allowed. Please re-enter all usernames.", Toast.LENGTH_SHORT).show();
                        }
                        // Successfully cleared contacts
                        else if (contactOne.equals("5") && contactTwo.equals("5") && contactThree.equals("5")){
                            Toast.makeText(mContext, "All contacts cleared.", Toast.LENGTH_SHORT).show();
                        }
                        // Successfully added contacts; should update messages
                        else if (contactOne.equals("4") && contactTwo.equals("4") && contactThree.equals("4")){
                            Toast.makeText(mContext, "Contacts added successfully.", Toast.LENGTH_SHORT).show();
                            sendContactNotif = true;
                        }
                    }
                    // If opcode = 8, the database server has sent a response for successful/unsuccessful physician registration
                    // Show corresponding messages to user
                    else if (opcode.equals("8")) {
                        String physician = obj.getString("physician");

                        // Unsuccessful physician registration : If username of physician to be added doesn't exist in database
                        if (physician.equals("1")) {
                            Toast.makeText(mContext, "You have entered a username doesn't exist. Please re-enter username.", Toast.LENGTH_SHORT).show();
                        }
                        // Unsuccessful contact registration : If username isn't actually registered as a physician
                        else if (physician.equals("2")){
                            Toast.makeText(mContext, "The username entered is not a physician. Please re-enter username.", Toast.LENGTH_SHORT).show();
                        }
                        // Unsuccessful contact registration : If username of physician includes stove owner themself
                        else if(physician.equals("3")){
                            Toast.makeText(mContext, "You can't add yourself. Please re-enter username.", Toast.LENGTH_SHORT).show();
                        }
                        // Successfully cleared physician
                        else if(physician.equals("5")){
                            Toast.makeText(mContext, "Physician cleared.", Toast.LENGTH_SHORT).show();
                        }
                        // Successfully added physician; should update messages
                        else if (physician.equals("4")){
                            Toast.makeText(mContext, "Physician added successfully.", Toast.LENGTH_SHORT).show();
                            sendPhysicianNotif = true;
                        }
                    }

                    // To update messages of stove owner when contacts or a physician has been successfully added
                    // To update messages of contacts or physician when they have been added to stove owner's account
                    if (sendContactNotif || sendPhysicianNotif) {
                        ArrayList<String> copyMessages = new ArrayList<>();
                        copyMessages.add(MainActivity.messages.get(0));
                        MainActivity.messages.clear();

                        // Create message with timestamp
                        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        String formattedTimeStamp = sdf3.format(timestamp);
                        String usersMessage = "\n\n [" + formattedTimeStamp + "]\n\nContacts have been updated and notified\n\n-----------------------------------------";

                        // Send database request to update message of stove owner (in database and messages arraylist) and contacts
                        JSONObject userinfo = new JSONObject();
                        if (sendContactNotif) {
                            String contactMessage = "\n\n [" + formattedTimeStamp + "]\n\nStove owner with username * " + username + " * has added you as a contact!\n\n-----------------------------------------";
                            MainActivity.messages.add(0, copyMessages.get(0).toString() + usersMessage);
                            try {
                                userinfo.put("opcode", "19");
                                userinfo.put("username", username);
                                userinfo.put("UserMessages", usersMessage);
                                userinfo.put("ContactMessages", contactMessage);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("AppDebug", "Error! " + e.toString());
                            }
                        }

                        // Send database request to update message of stove owner (in database and messages arraylist) and physician
                        if (sendPhysicianNotif) {
                            String physicianMessage = "\n\n [" + formattedTimeStamp + "]\n\nStove owner with username * " + username + " * has added you as a physician contact!\n\n-----------------------------------------";
                            MainActivity.messages.add(0, copyMessages.get(0).toString() + usersMessage);
                            try {
                                userinfo.put("opcode", "23");
                                userinfo.put("username", username);
                                userinfo.put("UserMessages", usersMessage);
                                userinfo.put("PhysicianMessages", physicianMessage);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("AppDebug", "Error! " + e.toString());
                            }
                        }
                        Sender = new sender();
                        Sender.run(databaseServerAddr, userinfo.toString(), senderPort);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}
