package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 @author: Abeer Rafiq

 Purpose of Class: To view currently added contacts (physician contacts and regular contacts).
 Also has link to show which usernames have added the current user as a contact.
 */
public class currentContactsActivity extends AppCompatActivity {

    // Class variables
    private Context mContext = this;
    private TextView physician, contact1, contact2, contact3;
    private TextView whoHasAddedMe;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    public static Handler exHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_contacts_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        
        // TextViews to show the currently added contacts/physician
        physician = (TextView) findViewById(R.id.physicianEdit);
        contact1 = (TextView) findViewById(R.id.contact1Text);
        contact2 = (TextView) findViewById(R.id.contact2Text);
        contact3  = (TextView) findViewById(R.id.contact3Text);

        // If user wants to see details about viewing contacts
        Button details_button = (Button) findViewById(R.id.details4);
        details_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(currentContactsActivity.this, detailPopUpActivity.class);
                intent2.putExtra("height", "0.4");
                intent2.putExtra("popupText", "\n\n\n\n\n\n\n\n\nABOUT CONTACTS: " +
                        "\n\n * These are contacts currently added to your account" +
                        "\n * The primary contacts will receive messages in the case of a stove emergency " +
                        "\n * The physician contact will receive messages about cooking trends " +
                        "\n\n    * SWIPE POP UP RIGHT TO CLOSE IT *  ");
                startActivity(intent2);
            }
        });

        // Link to see which usernames have added the current user as a contact
        whoHasAddedMe = (TextView) findViewById(R.id.whoAddedMe);
        whoHasAddedMe.setPaintFlags(whoHasAddedMe.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        whoHasAddedMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Activity
                Intent intent = new Intent(currentContactsActivity.this, whoHasAddedUserAsContactActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
        
        
        // When database server sends a message with the currently stored contacts for user
        // update the TextViews
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    // Extract contacts from received message
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String physician_data = obj.getString("physician");
                    String contact1_data = obj.getString("contact1");
                    String contact2_data = obj.getString("contact2");
                    String contact3_data = obj.getString("contact3");

                    // Update TextViews (either with contact's username or "None" if no added contacts)
                    if (physician_data.equals("")){
                        physician_data = "None";
                    }
                    if (contact1_data.equals("")){
                        contact1_data = "None";
                    }
                    if (contact2_data.equals("")){
                        contact2_data = "None";
                    }
                    if (contact3_data.equals("")){
                        contact3_data = "None";
                    }
                    physician.setText(physician_data);
                    contact1.setText(contact1_data);
                    contact2.setText(contact2_data);
                    contact3.setText(contact3_data);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };
    }
}
