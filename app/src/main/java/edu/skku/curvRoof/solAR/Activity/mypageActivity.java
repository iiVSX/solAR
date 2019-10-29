package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import edu.skku.curvRoof.solAR.R;

public class mypageActivity extends AppCompatActivity {
    private String ID;
    private TextView userText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Intent fromIntent = getIntent();
        ID = fromIntent.getStringExtra("ID");

        userText = (TextView)findViewById(R.id.userText);

        userText.setText(ID+"님");


    }
}
