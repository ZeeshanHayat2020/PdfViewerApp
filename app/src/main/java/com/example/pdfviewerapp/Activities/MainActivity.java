package com.example.pdfviewerapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pdfviewerapp.R;
import com.example.pdfviewerapp.constants.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends BaseActivity {

    String TAG = "MainActivity";
    private Button uploadBtn;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSharedPrefs();
        if (getThemeNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_main);
        checkStoragePermission();
        uploadBtn = findViewById(R.id.btnMain_Next);
        uploadBtn.setOnClickListener(onClickListener);

    }

    private void initSharedPrefs() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private boolean getThemeNightMode() {
        return sharedPreferences.getBoolean(Constant.KEY_PREFS_THEME_MODE, false);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            startActivity(new Intent(MainActivity.this, PdfViewActivity.class));
//           startActivity(new Intent(MainActivity.this, PinchZoomActivity.class));
        }
    };


    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}