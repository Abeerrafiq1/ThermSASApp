package com.example.thermsasapp;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 @author: Abeer Rafiq

 Purpose of Class: To view currently added contacts (physician contacts and regular contacts).
 */
public class currentContactsActivity extends AppCompatActivity {

    // Class variables
    private Context mContext = this;
    private TextView physician, contact1, contact2, contact3;
    public static Handler exHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_contacts_activity);

        // TextViews to show the currently added contacts/physician
        physician = (TextView) findViewById(R.id.physicianEdit);
        contact1 = (TextView) findViewById(R.id.contact1Text);
        contact2 = (TextView) findViewById(R.id.contact2Text);
        contact3  = (TextView) findViewById(R.id.contact3Text);

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
