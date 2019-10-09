package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.skku.curvRoof.solAR.R;

public class renderingActivity extends AppCompatActivity {
    // 임시 값 ///
    private double panelinfo =  7.5;//1.64 x 0.99 x 15.4 x 30(1month)
    private double panelnum = 10;
    private double radiation = 3.57;
    private double userfee = 55080;
    private double solargen;
    private double monthlyuse;
    private double money;
    private double result;
    private double generate;
    //////////////
    private Button gotoResult;
    private TextView expectGen;
    private TextView expectFee;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //가로본능
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_rendering);

        //결과화면으로
        gotoResult = (Button)findViewById(R.id.gotoresult);
        gotoResult.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(renderingActivity.this, resultActivity.class);
                i.putExtra("expectgen",generate);
                i.putExtra("monthlyuse", monthlyuse);
                i.putExtra("realuse", result);
                i.putExtra("expectfee", money);
                i.putExtra("usermoney", userfee);
                startActivity(i);
            }
        });


        //계산
        /**
         * 1.DB에서 사용자의 전기세 받아오기.
         * 2.위치정보 받아서 DB에서 일사량 가져오기.
         * 3.면적통해서 개수 받아오기.
         * **/

        double temp;
        //유저의 전기세를 바탕으로 사용전력량 계산
        if (userfee <= 17960) {
            //printf("태양광 발전을 필요로 하지 않습니다.");
        }
        else if (userfee <= 65760) {
            temp = userfee / 1.137;
            monthlyuse = ((temp - 20260) / 187.9) + 200;
        }
        else {
            temp = userfee / 1.137;
            monthlyuse = ((temp - 57840) / 280.6) + 400;
        }
        generate = panelinfo*panelnum*radiation; //발전량 계산
        //expectGen.setText(generate);
        result = monthlyuse - generate;


        //예상 전기료 계산
        if (result <= 200) {
            temp = 910 + 93.3 * result;
            if (temp < 5000) money = 1130;
            else {
                money = (temp - 4000) * 1.137;
            }
        }
        else if (result <= 400) {
            temp = 20260 + ((result - 200) * 187.9);
            money = temp * 1.137;
        }
        else {
            temp = 57840 + ((result - 400) * 280.6);
            money = temp * 1.137;
        }
        String tmpgen = String.format("%.0f", generate);
        String tmpmon = String.format("%.0f", money);
        expectGen = findViewById(R.id.expectgen);
        expectFee = findViewById(R.id.expectfee);
        expectGen.setText(tmpgen+"kWh");
        expectFee.setText(tmpmon+"원");

    }
}
