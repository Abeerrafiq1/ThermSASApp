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

 Purpose of Class: To display a pop up on the app show details
 for a specific app feature.
 */
public class detailPopUpActivity extends Activity {

    // Class variables
    private TextView stoveID_editText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        // Set app view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_pop_window_activity);

        // Get currently logged in username from previous view
        Intent intent = getIntent();
        String popTxt = intent.getStringExtra("popupText");
        String popHeight = intent.getStringExtra("height");
        Double popHeightDouble = Double.parseDouble(popHeight);

        // Update information displayed on pop up
        stoveID_editText = (TextView) findViewById(R.id.popupText2);
        stoveID_editText.setText(popTxt);

        // To format the pop up view
        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*.9), (int)(height*popHeightDouble));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);
    }
}
