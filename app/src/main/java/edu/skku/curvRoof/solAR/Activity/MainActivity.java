package edu.skku.curvRoof.solAR.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ElecfeeDialog.ElecfeeDialogListner {
    private String func;
    private Context mContext;
    private FloatingActionButton elecFab, measureFab, askFab, historyFab;
    private Button elecBtn, measureBtn, askBtn, historyBtn;
    private TextView idTv, elecFeeTv;
    private LinearLayout menull;
    //기존 전기요금 등록 텍뷰
    private TextView elecFee;
    private Long temp2;

    private double elec_fee;
    private String email, userID;
    private User user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = new User();

        Intent fromIntent = getIntent();
        email = fromIntent.getStringExtra("ID");

        mContext = getApplicationContext();

        menull = (LinearLayout) findViewById(R.id.menuLinearLayout);

        elecFab = (FloatingActionButton) findViewById(R.id.elecFab);
        measureFab = (FloatingActionButton) findViewById(R.id.measureFab);
        askFab = (FloatingActionButton) findViewById(R.id.askFab);
        historyFab = (FloatingActionButton) findViewById(R.id.historyFab);

        elecBtn = (Button) findViewById(R.id.elecBtn);
        measureBtn = (Button) findViewById(R.id.measureBtn);
        askBtn = (Button) findViewById(R.id.askBtn);
        historyBtn = (Button) findViewById(R.id.historyBtn);

        idTv = (TextView) findViewById(R.id.idTv);
        elecFeeTv = (TextView)findViewById(R.id.elecfee);

        elecFee = findViewById(R.id.elecfee);

        checkUser();

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
                        user.setElec_fee(temp2);
                        intent.putExtra("user", user);
                        startActivity(intent);
                        break;

                    case R.id.askFab:
                    case R.id.askBtn:
                        Intent intentlist = new Intent(MainActivity.this, companyListActivity.class);
                        intentlist.putExtra("user", user);
                        startActivity(intentlist);
                        break;

                    case R.id.historyFab:
                    case R.id.historyBtn:
                        Intent intentmypage = new Intent(MainActivity.this, historyActivity.class);
                        intentmypage.putExtra("user", user);
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

        historyFab.setOnClickListener(onClickListener);
        historyBtn.setOnClickListener(onClickListener);

    }

    //기존 전기요금 등록
    private void openDialog() {
        ElecfeeDialog exampleDialog = new ElecfeeDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void applyTexts(String elecfee) {
        elecFee.setText("등록 전기요금은 " + elecfee + "원 입니다.");
        elec_fee = Double.valueOf(elecfee);
        if(userID != null){
            user.setElec_fee(elec_fee);
            myRef.child("user_list").child(userID).child("elec_fee").setValue(elec_fee);
        }
        else{
            Toast.makeText(getApplicationContext(), "USER NOT REGISTERED", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onClick(View v) {

    }


    public void checkUser(){
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.child("user_id").getChildren()){
                    if(snapshot.getValue().equals(email)) {
                        userID = snapshot.getKey();
                    }
                }
                if(userID == null){
                    userID = myRef.child("user_id").push().getKey();
                    myRef.child("user_id").child(userID).setValue(email);
                }
                else{
                    Object temp = null;
                    if((temp = dataSnapshot.child("user_list").child(userID).child("elec_fee").getValue()) != null){
                        temp2 = (Long)temp;
                        elecFeeTv.setText("등록 전기요금은 " + temp.toString() + "원 입니다.");
                    }
                }
                user.setUserID(userID);
                idTv.setText(user.getUserID() + "님 반갑습니다!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
