package com.example.thermsasapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class currentSubscribersActivity extends AppCompatActivity {
    Context mContext = this;
    public static Handler exHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_subscribers_activity);

        TextView physician = (TextView) findViewById(R.id.physicianEdit);
        TextView contact1 = (TextView) findViewById(R.id.contact1Text);
        TextView contact2 = (TextView) findViewById(R.id.contact2Text);
        TextView contact3  = (TextView) findViewById(R.id.contact3Text);

        exHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    JSONObject obj = new JSONObject((String) msg.obj);
                    String physician_data = obj.getString("physician");
                    String contact1_data = obj.getString("contact1");
                    String contact2_data = obj.getString("contact2");
                    String contact3_data = obj.getString("contact3");
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
                }
            }
        };
    }


}
