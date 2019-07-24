package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import edu.skku.curvRoof.solAR.R;

public class choiceActivity extends AppCompatActivity {

    private ImageButton roof_button;
    private ImageButton top_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        final Intent intent =new Intent(this, pointCloudActivity.class);

        roof_button=(ImageButton)findViewById(R.id.roof_button);
        top_button=(ImageButton)findViewById(R.id.top_button);

        roof_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                intent.putExtra("roof_type","roof");
                startActivity(intent);
            }
        });

        top_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                intent.putExtra("roof_type","top");
                startActivity(intent);
            }
        });
    }
}
