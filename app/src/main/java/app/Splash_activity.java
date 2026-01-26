package app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import auth.GetStartedActivity;
import com.example.ConstructionApp.R;

public class Splash_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /*
        ImageView logo = findViewById(R.id.logo);
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.fade_scale);
        logo.startAnimation(logoAnim);

        TextView tagline = findViewById(R.id.tagline);
        if (tagline != null) {
            Animation textAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_delayed);
            tagline.startAnimation(textAnim);
        }

         */


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(Splash_activity.this, GetStartedActivity.class));
            finish();
        }, 10000);
    }
}
