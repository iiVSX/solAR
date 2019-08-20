package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.skku.curvRoof.solAR.R;

public class MainActivity extends AppCompatActivity {
    private String ID;
    private String func;
    private FloatingActionButton measureBtn;
    private Button measureBtn2;
    private TextView idTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent fromIntent = getIntent();
        ID = fromIntent.getStringExtra("ID");

        measureBtn = (FloatingActionButton)findViewById(R.id.measureFab);
        measureBtn2=(Button)findViewById(R.id.measureBtn);
        idTv = (TextView)findViewById(R.id.idTv);

        idTv.setText(ID+"님 반갑습니다!");

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.measureFab:
                    case R.id.measureBtn:
                        Intent intent = new Intent(MainActivity.this, choiceActivity.class);
                        intent.putExtra("type", "measure");
                        startActivity(intent);
                        break;

                    default:
                        break;

                }
            }
        };

        measureBtn.setOnClickListener(onClickListener);
        measureBtn2.setOnClickListener(onClickListener);

    }

}
