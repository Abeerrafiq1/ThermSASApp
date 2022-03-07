package com.example.thermsasapp;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 @author: Abeer Rafiq

 Purpose of Class: When a user wants to login, they will enter their username and password.
 The entered password will be checked against the password stored in the database for the user.
 If passwords match, the user can login otherwise they are denied access.
 */
public class loginActivity extends AppCompatActivity {

    // Class variables
    private Context mContext = this;
    private sender Sender;
    private String databaseServerAddr = "192.168.137.1";
    private static final int senderPort = 1000;
    private EditText editTextUsername, editTextPassword;
    private Button login_button2;
    public static String stopPopUps;
    public static Handler exHandler;
    public static Handler exHandler1;
    public static Handler exHandler2;

    // When a user goes back from the mainOptionsActivity, they go to this loginActivity
    // Make sure receiverMessages (to retrieve user messages) is closed since user is logged out
    protected void onRestart() {
        super.onRestart();
        receiverMessages.udpDatagramSocket.close();
        stopPopUps = "false";
        MainActivity.recMsgs.exitThread(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // To handle continuous retrieval of user messages (notifications) and associating pop ups
        receiverMessages.udpDatagramSocket.close();
        stopPopUps = "false";
        MainActivity.recMsgs.exitThread(true);

        // Two editText instances to enter passwords and username
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        // Login button to trigger app to login
        login_button2 = (Button) findViewById(R.id.loginbutton2);

        // If login requested, send database server username to retrieve stored password
        login_button2.setOnClickListener(v -> {
            JSONObject userinfo = new JSONObject();
            try {
                userinfo.put("opcode", "1");
                userinfo.put("username", editTextUsername.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("AppDebug", "Error! " + e.toString());
            }
            Sender = new sender();
            Sender.run(databaseServerAddr, userinfo.toString(), senderPort);
        });

        // If user wants to see details about login
        Button details_button = (Button) findViewById(R.id.details5);
        details_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(loginActivity.this, detailPopUpActivity.class);
                intent2.putExtra("height", "0.3");
                intent2.putExtra("popupText", "\n\n\n\n\n\n\n\n\n\n\nABOUT LOGIN: " +
                        "\n\n * Username and password are both case sensitive" +
                        "\n * Do not add extra spaces" +
                        "\n\n    * SWIPE POP UP RIGHT TO CLOSE IT *  ");
                startActivity(intent2);
            }
        });

        // After database server sends stored password of user, compare it against user entered password
        exHandler = new Handler() {
            @Override
            public void handleMessage(Message dbpassword) {
                super.handleMessage(dbpassword);
                try {
                    // Extract password and messages from received message
                    JSONObject obj = new JSONObject((String) dbpassword.obj);
                    String password = obj.getString("password");
                    String messages = obj.getString("messages");
                    String onTooLongNotif = "false";

                    // If the password matches the user entered password, let user login, otherwise show on app incorrect credentials
                    if (!editTextPassword.getText().toString().isEmpty() && !editTextUsername.getText().toString().isEmpty() && password.equals(editTextPassword.getText().toString())) {
                        // Show authenticated
                        Toast.makeText(mContext, "Authenticated", Toast.LENGTH_SHORT).show();

                        // To handle continuous retrieval of user messages (notifications)
                        MainActivity.recMsgs = new receiverMessages(editTextUsername.getText().toString());
                        MainActivity.recMsgs.start();

                        // Update user's messages
                        MainActivity.messages.clear();
                        MainActivity.messages.add(0, messages);

                        // Start mainOptionsActivity to give five options
                        Intent intent = new Intent(loginActivity.this, mainOptionsActivity.class);
                        intent.putExtra("username", editTextUsername.getText().toString());
                        // Clear editTexts so if you go back from main options activity, credentials have to be re-entered
                        editTextUsername.setText("");
                        editTextPassword.setText("");
                        startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "Incorrect Credentials", Toast.LENGTH_SHORT).show();
                        // Clear edit texts so credentials have to be re-entered since they are wrong
                        editTextUsername.setText("");
                        editTextPassword.setText("");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AppDebug", "Error! " + e.toString());
                }
            }
        };

        // If user wants to stop pop up messages about a member's stove for a current logged in session
        // they can, otherwise pop ups will be shown with notifications on phone's notification bar
        exHandler1 = new Handler() {
            @Override
            public void handleMessage(Message dbpassword) {
                super.handleMessage(dbpassword);
                if (stopPopUps.equals("false")) {
                    // Show Pop Up
                    String text = "A member has the stove on too long! Please check messages!";
                    Intent intent = new Intent(loginActivity.this, alertPopUpActivity.class);
                    intent.putExtra("popupText", text);
                    startActivity(intent);

                    // Show Notification on phone's notification bar
                    String text2 = "A member has the stove on too long! Swipe to close";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
                        NotificationManager manager = getSystemService(NotificationManager.class);
                        manager.createNotificationChannel(channel);
                    }
                    Log.d("AppDebug", "Again in Main");
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(loginActivity.this, "My Notification");
                    builder.setContentTitle("Stove Risk!");
                    builder.setContentText(text2);
                    builder.setSmallIcon(R.drawable.save2);
                    builder.setAutoCancel(true);
                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(loginActivity.this);
                    managerCompat.notify(1, builder.build());
                }
            }
        };

        // If user wants to stop pop up messages about a their own stove for a current logged in session
        // they can, otherwise pop ups will be shown with notifications on phone's notification bar
        exHandler2 = new Handler() {
            @Override
            public void handleMessage(Message dbpassword) {
                super.handleMessage(dbpassword);
                if (stopPopUps.equals("false")) {
                    // Show Pop Up
                    String text = "Your stove was on too long!\n Check stove and messages!";
                    Intent intent = new Intent(loginActivity.this, alertPopUpActivity.class);
                    intent.putExtra("popupText", text);
                    startActivity(intent);

                    // Show Notification on phone's notification bar
                    String text2 = "Your stove was on too long!\nSwipe to close";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
                        NotificationManager manager = getSystemService(NotificationManager.class);
                        manager.createNotificationChannel(channel);
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(loginActivity.this, "My Notification");
                    builder.setContentTitle("Stove Risk!");
                    builder.setContentText(text2);
                    builder.setSmallIcon(R.drawable.save2);
                    builder.setAutoCancel(true);
                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(loginActivity.this);
                    managerCompat.notify(2, builder.build());
                }
            }
        };
    }
}