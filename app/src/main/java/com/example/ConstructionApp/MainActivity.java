package com.example.ConstructionApp;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        getWindow().getInsetsController().setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        );

        View main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    topInset,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        BottomNavigationView btn1 = findViewById(R.id.btn1);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new Home())
                    .commit();
        }

        btn1.setOnItemSelectedListener(item -> {
            FragmentManager fm = getSupportFragmentManager();

            if (item.getItemId() == R.id.Home) {
                fm.beginTransaction()
                        .replace(R.id.fragmentContainerView, new Home())
                        .commit();
                return true;

            } else if (item.getItemId() == R.id.workers) {
                fm.beginTransaction()
                        .replace(R.id.fragmentContainerView, new workers())
                        .commit();
                return true;

            } else if (item.getItemId() == R.id.posts) {
                fm.beginTransaction()
                        .replace(R.id.fragmentContainerView, new Posts())
                        .commit();
                return true;

            } else if (item.getItemId() == R.id.Activity) {
                fm.beginTransaction()
                        .replace(R.id.fragmentContainerView, new Activity())
                        .commit();
                return true;

            } else if (item.getItemId() == R.id.mess) {
                fm.beginTransaction()
                        .replace(R.id.fragmentContainerView, new Messages())
                        .commit();
                return true;
            }

            return false;
        });
    }
}
