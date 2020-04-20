package edu.skku.curvRoof.solAR.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import edu.skku.curvRoof.solAR.R;

public class startSplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Handler hd=new Handler();
        hd.postDelayed(new splashHandler(),2000);
    }

    private class splashHandler implements Runnable{
        public void run(){
            startActivity(new Intent(getApplicationContext(), loginActivity.class));
            startSplashActivity.this.finish();
        }
    }
    @Override
    public void onBackPressed() {

    }


}
