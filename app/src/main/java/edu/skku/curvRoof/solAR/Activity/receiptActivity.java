package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;

public class receiptActivity extends AppCompatActivity {
    private FloatingActionButton goFab;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Intent fromIntent = getIntent();
        user = (User)fromIntent.getSerializableExtra("user");

        goFab = (FloatingActionButton)findViewById(R.id.goFab);
        goFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(receiptActivity.this, companyListActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
    }
    /*
    public static class calculateSplashActivity extends AppCompatActivity {

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


    public static class moveSplashActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_move_splash);
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
    */
}
