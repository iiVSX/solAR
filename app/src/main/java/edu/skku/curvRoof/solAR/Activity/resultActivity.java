package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.skku.curvRoof.solAR.Model.Trial;
import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;

public class resultActivity extends AppCompatActivity {


    private TextView monthlyUse;
    private TextView expectReduce;
    private TextView expectFee;
    private TextView reduceAmt;

    public double monthlyfee;// = i.getDoubleExtra("monthlyuse",0);
    //public double expectgen;// = i.getDoubleExtra("expectgen",0);
    public double expectfee;// = i.getDoubleExtra("expectfee",0);
    public double reducefee;
    public double down;
    private FloatingActionButton companyListFab;
    private Button saveBtn;

    private User user;
    private Trial trial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //세로본능
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Intent i = getIntent();

        user = (User)i.getSerializableExtra("user");
        trial = (Trial)i.getSerializableExtra("trial");

        //monthlyfee = i.getDoubleExtra("userfee",0);
        monthlyfee = user.getElec_fee();
        //expectgen = i.getDoubleExtra("expectgen",0);
        //expectfee = i.getDoubleExtra("expectfee",0);
        expectfee = user.getExpect_fee();
        //userfee = i.getDoubleExtra("usermoney",0);
        reducefee = monthlyfee - expectfee;

        String tmpmu = String.format("%.0f", monthlyfee);
        String tmpeg = String.format("%.0f", reducefee);
        String tmpef = String.format("%.0f", expectfee);

        monthlyUse = findViewById(R.id.monthlyUse); monthlyUse.setText(tmpmu+"원");
        expectReduce = findViewById(R.id.expectReduce); expectReduce.setText(tmpeg+"원");
        expectFee = findViewById(R.id.expectFee); expectFee.setText(tmpef+"원");
        companyListFab = findViewById(R.id.companyListFab);
        saveBtn = findViewById(R.id.save_btn);

        down = (1-(expectfee/monthlyfee))*100;
        String tmpdo = String.format("%.0f", down);
        reduceAmt = findViewById(R.id.reduceAmt); reduceAmt.setText(tmpdo+"%");

        companyListFab.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(resultActivity.this, companyListActivity.class);
            @Override
            public void onClick(View v) {

                startActivity(intent);

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
            }
        });
        setBarChart();
    }

    private void setBarChart(){

        BarChart barChart = (BarChart) findViewById(R.id.chart);


        ArrayList<BarEntry> val= new ArrayList<>(); //데이터의 값
        val.add(new BarEntry(0, (float)monthlyfee));
        val.add(new BarEntry(1, (float)expectfee));


        //String tmpdown = String.format("%.0f",down);
        //BarChart.setCenterText("전기세 절감율은 약"+ tmpdown + "%입니다.");


        BarDataSet dataSet = new BarDataSet(val,"Fee");
        //barChart.animateY(5000);
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        barChart.setData(data);

        barChart.setFitBars(true);
        barChart.setScaleEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);

        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        XAxis xAxis = barChart.getXAxis();

        //xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);


        //leftAxis.setTextSize(10f);
        //leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(false);

        /*
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        */
        final ArrayList<String> xlabel = new ArrayList<>();
        xlabel.add("월 평균 전기료");
        xlabel.add("예상 전기료");

        //XAxis xxAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        //xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if(value >= 0){
                    if(value <= xlabel.size()-1){
                        return xlabel.get((int)value);
                    }
                    return "";
                }
                return "";
            }
        });
    }
    public void saveInfo(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        String userID = user.getUserID();
        String trialID = trial.getTrialID();

        myRef.child("user_list").child(userID).child(trialID).child("longitude").setValue(trial.getLongitude());
        myRef.child("user_list").child(userID).child(trialID).child("latitude").setValue(trial.getLatitude());
        myRef.child("user_list").child(userID).child(trialID).child("area_type").setValue(trial.getArea_type());
       /* myRef.child("user_list").child(userID).child(trialID).child("area_height").setValue(trial.getArea_height());
        myRef.child("user_list").child(userID).child(trialID).child("area_width").setValue(trial.getArea_width());
        myRef.child("user_list").child(userID).child(trialID).child("angle").setValue(trial.getAngle());
        myRef.child("user_list").child(userID).child(trialID).child("azimuth").setValue(trial.getAzimuth());
        myRef.child("user_list").child(userID).child(trialID).child("img_url").setValue(trial.getCaptureUrl());
        myRef.child("user_list").child(userID).child(trialID).child("panel_count").setValue(trial.getPanel_count());*/

    }

}
