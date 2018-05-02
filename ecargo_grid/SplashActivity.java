package com.ecargo.ecargo_grid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SplashActivity extends AppCompatActivity {
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        img = (ImageView) findViewById(R.id.splash_icon);

        animation2();
    }

    private void animation2() {

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate_top_to_center);
        img.startAnimation(anim);
    }


}
