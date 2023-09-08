package com.example.myapplication;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

import com.example.myapplication.databinding.ActivityWarningBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WarningActivity extends AppCompatActivity {
    private ActivityWarningBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityWarningBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}