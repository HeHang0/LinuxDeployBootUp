package com.oohoo.bootup;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText startX;
    private EditText startY;
    private EditText confirmX;
    private EditText confirmY;
    final static public String ButtonPositionKey = "button_position_key";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startX = findViewById(R.id.startX);
        startY = findViewById(R.id.startY);
        confirmX = findViewById(R.id.confirmX);
        confirmY = findViewById(R.id.confirmY);
        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(v -> savePosition());
        MultiProcessSharedPreferences.setAuthority("com.oohoo.bootup.provider");
        initPosition();
    }

    private void initPosition(){
        SharedPreferences settings = MultiProcessSharedPreferences.getSharedPreferences(this,
                ButtonPositionKey, Context.MODE_PRIVATE);
        int startXV = settings.getInt("startX", -1);
        int startYV = settings.getInt("startY", -1);
        int confirmXV = settings.getInt("confirmX", -1);
        int confirmYV = settings.getInt("confirmY", -1);
        if(startXV < 0 || startYV < 0 || confirmXV < 0 || confirmYV < 0){
            int[] size = getSize();
            startXV = 50;
            startYV = size[1] - 50;
            confirmXV = (int) (size[0] * 0.8125);
            confirmYV = (int) (size[1] * 0.6008);
            setSharedPreferences(startXV, startYV, confirmXV, confirmYV);
        }
        startX.setText(String.valueOf(startXV));
        startY.setText(String.valueOf(startYV));
        confirmX.setText(String.valueOf(confirmXV));
        confirmY.setText(String.valueOf(confirmYV));
    }

    private int[] getSize() {
        int[] size = new int[2];
        DisplayMetrics displayMetrics=new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        size[0]=displayMetrics.widthPixels;
        size[1]=displayMetrics.heightPixels;
        return size;
    }

    private void savePosition(){
        try {
            int startXV =Integer.parseInt(startX.getText().toString());
            int startYV =Integer.parseInt(startY.getText().toString());
            int confirmXV =Integer.parseInt(confirmX.getText().toString());
            int confirmYV =Integer.parseInt(confirmY.getText().toString());
            setSharedPreferences(startXV, startYV, confirmXV, confirmYV);
        }catch (Exception e) {
            Log.e("BootUp MainActivity", e.getMessage());
        }
    }

    private void setSharedPreferences(int sx, int sy, int cx, int cy){
        SharedPreferences settings = MultiProcessSharedPreferences.getSharedPreferences(this,
                ButtonPositionKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("startX", sx);
        editor.putInt("startY", sy);
        editor.putInt("confirmX", cx);
        editor.putInt("confirmY", cy);
        editor.apply();
    }
}