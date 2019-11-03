package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;

public class mypageActivity extends AppCompatActivity {
    private User user;
    private TextView userText;
    private Button infoBtn,historyBtn,settingsBtn;
    private FloatingActionButton infoFab,historyFab,settingsFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Intent fromIntent = getIntent();
        user = (User)fromIntent.getSerializableExtra("user");

        userText = (TextView)findViewById(R.id.userText);

        userText.setText(user.getUserID()+"님");

        infoBtn=(Button)findViewById(R.id.infoBtn);
        infoFab=(FloatingActionButton)findViewById(R.id.infoFab);
        historyBtn=(Button)findViewById(R.id.historyBtn);
        historyFab=(FloatingActionButton)findViewById(R.id.historyFab);
        settingsBtn=(Button)findViewById(R.id.settingsBtn);
        settingsFab=(FloatingActionButton)findViewById(R.id.settingsFab);

        Button.OnClickListener onClickListener= new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.historyBtn:
                    case R.id.historyFab:
                        Intent receiptIntent = new Intent(mypageActivity.this,receiptActivity.class);
                        startActivity(receiptIntent);
                        break;

                    default:
                        break;

                }
            }
        };

        historyBtn.setOnClickListener(onClickListener);
        historyFab.setOnClickListener(onClickListener);

    }
}
