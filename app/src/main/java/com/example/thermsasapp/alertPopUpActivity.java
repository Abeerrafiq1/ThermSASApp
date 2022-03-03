package com.example.thermsasapp;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

/**
 @author: Abeer Rafiq

 Purpose of Class: To display a pop up on the app and alert users
 of events that need immediate action.
 */
public class alertPopUpActivity extends Activity {

    // Class variables
    private TextView stoveID_editText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_pop_window_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String popTxt = intent.getStringExtra("popupText");

        // Update information displayed on pop up
        stoveID_editText = (TextView) findViewById(R.id.popupText);
        stoveID_editText.setText(popTxt);

        // To format the pop up view
        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.9), (int)(height*.5));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
    }
}
