package edu.skku.curvRoof.solAR.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.skku.curvRoof.solAR.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ElecfeeDialog.ElecfeeDialogListner{
    private String ID;
    private String func;
    private Context mContext;
    private ImageView menuBackground;
    private FloatingActionButton menuFab, elecFab, conditionFab, measureFab, askFab, myPageFab;
    private Button elecBtn, conditionBtn, measureBtn, askBtn, myPageBtn;
    private TextView idTv;
    private boolean isFabOpen=false;
    private LinearLayout menull;
    private Animation fab_open, fab_close;
    //기존 전기요금 등록 텍뷰
    private TextView elecFee;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=getApplicationContext();

        fab_open= AnimationUtils.loadAnimation(mContext,R.anim.fab_open);
        fab_close=AnimationUtils.loadAnimation(mContext,R.anim.fab_close);

        Intent fromIntent = getIntent();
        ID = fromIntent.getStringExtra("ID");

        menuFab=(FloatingActionButton)findViewById(R.id.menuFab);
        menuBackground=(ImageView)findViewById(R.id.menuBackground);
        menull=(LinearLayout)findViewById(R.id.menuLinearLayout);

        elecFab=(FloatingActionButton)findViewById(R.id.elecFab);
        conditionFab=(FloatingActionButton)findViewById(R.id.conditionFab);
        measureFab = (FloatingActionButton)findViewById(R.id.measureFab);
        askFab=(FloatingActionButton)findViewById(R.id.askFab);
        myPageFab=(FloatingActionButton)findViewById(R.id.mypageFab);

        elecBtn=(Button)findViewById(R.id.elecBtn);
        conditionBtn=(Button)findViewById(R.id.conditionBtn);
        measureBtn=(Button)findViewById(R.id.measureBtn);
        askBtn=(Button)findViewById(R.id.askBtn);
        myPageBtn=(Button)findViewById(R.id.myPageBtn);

        idTv = (TextView)findViewById(R.id.idTv);

        idTv.setText(ID+"님 반갑습니다!");

        elecFee = findViewById(R.id.elecfee);

        Button.OnClickListener onClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    //기존 전기요금 등록
                    case R.id.elecFab:
                    case R.id.elecBtn:
                       openDialog();
                       break;
                    //설치면적 측정
                    case R.id.measureFab:
                    case R.id.measureBtn:
                        Intent intent = new Intent(MainActivity.this, choiceActivity.class);
                        intent.putExtra("type", "measure");
                        startActivity(intent);
                        break;

                    case R.id.askFab:
                    case R.id.askBtn:
                        Intent intentlist= new Intent(MainActivity.this,companyListActivity.class);
                        startActivity(intentlist);
                        break;

                    case R.id.mypageFab:
                    case R.id.myPageBtn:
                        Intent intentmypage=new Intent(MainActivity.this,mypageActivity.class);
                        startActivity(intentmypage);
                        break;

                    default:
                        break;

                }
            }
        };
        elecFab.setOnClickListener(onClickListener);
        elecBtn.setOnClickListener(onClickListener);

        measureFab.setOnClickListener(onClickListener);
        measureBtn.setOnClickListener(onClickListener);

        askFab.setOnClickListener(onClickListener);
        askBtn.setOnClickListener(onClickListener);

        myPageFab.setOnClickListener(onClickListener);
        myPageBtn.setOnClickListener(onClickListener);

        menuFab.setOnClickListener(this);
    }
    //기존 전기요금 등록
    private void openDialog() {
        ElecfeeDialog exampleDialog = new ElecfeeDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }
    @Override
    public void applyTexts(String elecfee) {
        //TextView aa = (TextView) findViewById(R.id.eaa);
        elecFee.setText("등록 전기요금은 "+ elecfee +"원 입니다.");
    }
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.menuFab:
                toogleFab();
                break;
        }
    }

    private void toogleFab(){
        if(isFabOpen){
            menuFab.setForeground(getResources().getDrawable(R.drawable.ic_01_menu));
            menuBackground.startAnimation(fab_close);
            menull.startAnimation(fab_close);
            elecFab.setClickable(false);
            elecBtn.setClickable(false);
            conditionFab.setClickable(false);
            conditionBtn.setClickable(false);
            measureFab.setClickable(false);
            measureBtn.setClickable(false);
            askFab.setClickable(false);
            askBtn.setClickable(false);
            myPageFab.setClickable(false);
            myPageBtn.setClickable(false);
            isFabOpen=false;
        }
        else{
            menuFab.setForeground(getResources().getDrawable(R.drawable.ic_02_closebackground));
            menuBackground.startAnimation(fab_open);
            menull.startAnimation(fab_open);
            elecFab.setClickable(true);
            elecBtn.setClickable(true);
            conditionFab.setClickable(true);
            conditionBtn.setClickable(true);
            measureFab.setClickable(true);
            measureBtn.setClickable(true);
            askFab.setClickable(true);
            askBtn.setClickable(true);
            myPageFab.setClickable(true);
            myPageBtn.setClickable(true);
            isFabOpen=true;
        }
    }
}
