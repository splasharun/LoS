package com.splash.arun.los;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1600;
    ImageView splash;
    AnimationDrawable frameAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        splash = (ImageView) findViewById(R.id.splashImage);
        splash.setBackgroundResource(R.drawable.splashdrawable);
        frameAnimation = (AnimationDrawable) splash.getBackground();
        frameAnimation.start();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                frameAnimation.stop();
                Intent intent = new Intent(SplashScreen.this, WelcomeActivity.class);
                startActivity(intent);

                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
