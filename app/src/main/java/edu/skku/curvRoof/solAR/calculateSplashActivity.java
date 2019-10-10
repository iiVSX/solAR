package edu.skku.curvRoof.solAR;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class calculateSplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_splash);
        loading();
    }
    private void loading(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }
}
