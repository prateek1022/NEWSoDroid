package com.cu.newsodroid.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.cu.newsodroid.R;
import com.cu.newsodroid.config.static_variables;

public class Splash_screen extends AppCompatActivity {


    int PERMISSION_ID = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                if(checkPermissions()){
                    Intent intent=new Intent(Splash_screen.this,Home.class);
                    intent.putExtra("location",true);
                    startActivity(intent);
                    finish();
                }
                else{
                    requestPermissions();
                }


            }
        }, 2000);

    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent=new Intent(Splash_screen.this,Home.class);
                intent.putExtra("location",true);
                startActivity(intent);
                finish();
            }
            else{
                show_dialog();
            }
        }
    }
    public void show_dialog(){
        final Dialog dialog = new Dialog(Splash_screen.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.location_dialog);
        dialog.show();
        Button ind_btn=dialog.findViewById(R.id.ind_btn);
        Button us_btn=dialog.findViewById(R.id.us_btn);

        ind_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Splash_screen.this,Home.class);
                intent.putExtra("country", static_variables.India);
                intent.putExtra("location",false);
                startActivity(intent);
                finish();
            }
        });

        us_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Splash_screen.this,Home.class);
                intent.putExtra("country", static_variables.United_States);
                intent.putExtra("location",false);
                startActivity(intent);
                finish();
            }
        });

    }
}