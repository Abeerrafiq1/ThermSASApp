package com.example.thermsasapp;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.Pattern;

public class loginActivity extends AppCompatActivity {
    Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    public static Handler exHandler;

    public loginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        EditText username_textview = (EditText) findViewById(R.id.editTextUsername);
        EditText password_textview = (EditText) findViewById(R.id.editTextPassword);
        Button login_button2 = (Button) findViewById(R.id.loginbutton2);

        login_button2.setOnClickListener(v -> {
            JSONObject userinfo = new JSONObject();
            try {
                userinfo.put("opcode", "1");
                userinfo.put("username", username_textview.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Sender = new sender();
            Sender.run(databaseServerAddr, userinfo.toString(),  senderPort);

       });

        exHandler = new Handler() {
            @Override
            public void handleMessage(Message dbpassword) {
                super.handleMessage(dbpassword);
                try {
                    Toast.makeText(mContext, "enter", Toast.LENGTH_SHORT).show();
                    JSONObject obj = new JSONObject((String) dbpassword.obj);
                    String password = obj.getString("password");
                    if (!password_textview.getText().toString().isEmpty() && !username_textview.getText().toString().isEmpty() && password.equals(password_textview.getText().toString())) {
                        String notifications = obj.getString("notifications");
                        MainActivity.notifications.clear();
                        MainActivity.notifications.add(0, notifications);

                        Toast.makeText(mContext, "Authenticated", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(loginActivity.this, mainOptionsActivity.class);
                        intent.putExtra("currentUser", username_textview.getText().toString());
                        startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
