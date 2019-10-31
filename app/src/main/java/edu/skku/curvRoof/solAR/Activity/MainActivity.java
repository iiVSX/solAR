package edu.skku.curvRoof.solAR.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Utils.GpsUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ElecfeeDialog.ElecfeeDialogListner {
    private String func;
    private Context mContext;
    private FloatingActionButton elecFab, measureFab, askFab, myPageFab;
    private Button elecBtn, measureBtn, askBtn, myPageBtn;
    private TextView idTv;
    private LinearLayout menull;
    //기존 전기요금 등록 텍뷰
    private TextView elecFee;

    private double longitude, latitude;
    private String email, userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                GpsUtil gpsTracker = new GpsUtil(MainActivity.this);
                longitude = gpsTracker.getLongitude();
                latitude = gpsTracker.getLatitude();
                Looper.loop();
            }
        });

        t.start();

        Intent fromIntent = getIntent();
        email = fromIntent.getStringExtra("ID");

        mContext = getApplicationContext();

        menull = (LinearLayout) findViewById(R.id.menuLinearLayout);

        elecFab = (FloatingActionButton) findViewById(R.id.elecFab);
        measureFab = (FloatingActionButton) findViewById(R.id.measureFab);
        askFab = (FloatingActionButton) findViewById(R.id.askFab);
        myPageFab = (FloatingActionButton) findViewById(R.id.mypageFab);

        elecBtn = (Button) findViewById(R.id.elecBtn);
        measureBtn = (Button) findViewById(R.id.measureBtn);
        askBtn = (Button) findViewById(R.id.askBtn);
        myPageBtn = (Button) findViewById(R.id.myPageBtn);

        idTv = (TextView) findViewById(R.id.idTv);

        idTv.setText(email + "님 반갑습니다!");

        elecFee = findViewById(R.id.elecfee);

        putFirebase();

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
                        intent.putExtra("userID", userID);
                        startActivity(intent);
                        break;

                    case R.id.askFab:
                    case R.id.askBtn:
                        Intent intentlist = new Intent(MainActivity.this, companyListActivity.class);
                        intentlist.putExtra("userID", userID);
                        startActivity(intentlist);
                        break;

                    case R.id.mypageFab:
                    case R.id.myPageBtn:
                        Intent intentmypage = new Intent(MainActivity.this, mypageActivity.class);
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

    }

    //기존 전기요금 등록
    private void openDialog() {
        ElecfeeDialog exampleDialog = new ElecfeeDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void applyTexts(String elecfee) {
        //TextView aa = (TextView) findViewById(R.id.eaa);
        elecFee.setText("등록 전기요금은 " + elecfee + "원 입니다.");
    }

    @Override
    public void onClick(View v) {

    }


    public void putFirebase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        myRef.child("user_id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.getValue().equals(email)) {
                        userID = snapshot.getKey();
                    }
                }
                if(userID == null){
                    userID = myRef.child("user_id").push().getKey();
                    myRef.child("user_id").child(userID).setValue(email);
                }

                DatabaseReference userRef = myRef.child("user_list").child(userID);

                userRef.child("longitude").setValue(longitude);
                userRef.child("latitude").setValue(latitude);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}
