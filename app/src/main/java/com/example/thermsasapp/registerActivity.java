package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


public class registerActivity extends AppCompatActivity {
    Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    public static Handler exHandler;


    public registerActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);


        EditText username_textview = (EditText) findViewById(R.id.enterUsername);
        EditText password_textview = (EditText) findViewById(R.id.enterPassword);
        CheckBox licensedPhysician_checkbox = (CheckBox) findViewById(R.id.licensedPhysicianCheckbox);
        Button register_button = (Button) findViewById(R.id.registerB);
        String isPhysician;

        register_button.setOnClickListener(v -> {
            if (password_textview.getText().toString().isEmpty() || username_textview.getText().toString().isEmpty()){
                Toast.makeText(mContext, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
            }
            else {
                JSONObject userinfo = new JSONObject();
                try {
                    userinfo.put("opcode", "3");
                    userinfo.put("username", username_textview.getText().toString());
                    userinfo.put("password", password_textview.getText().toString());
                    userinfo.put("physician", licensedPhysician_checkbox.isChecked());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Sender = new sender();
                Sender.run(databaseServerAddr, userinfo.toString(), senderPort);

            }
        });

        exHandler = new Handler() {

            @Override
            public void handleMessage(Message valid) {
                super.handleMessage(valid);
                try {
                    JSONObject obj = new JSONObject((String) valid.obj);
                    String validity = obj.getString("valid");
                    if (validity.equals("yes")){
                        Toast.makeText(mContext, "Redirecting to Login", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(registerActivity.this, loginActivity.class));
                    } else {
                        Toast.makeText(mContext, "User Already Exists!\n Please use a different Username", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        };
    }

}
