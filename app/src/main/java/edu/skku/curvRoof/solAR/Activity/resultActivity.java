package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import edu.skku.curvRoof.solAR.R;

public class resultActivity extends AppCompatActivity {


    private TextView monthlyUse;
    private TextView expectReduce;
    private TextView expectFee;

    public double monthlyfee;// = i.getDoubleExtra("monthlyuse",0);
    //public double expectgen;// = i.getDoubleExtra("expectgen",0);
    public double expectfee;// = i.getDoubleExtra("expectfee",0);
    public double reducefee;
    public double down;
    private FloatingActionButton companyListFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //세로본능
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Intent i = getIntent();



        monthlyfee = i.getDoubleExtra("userfee",0);
        //expectgen = i.getDoubleExtra("expectgen",0);
        expectfee = i.getDoubleExtra("expectfee",0);
        //userfee = i.getDoubleExtra("usermoney",0);
        reducefee = monthlyfee - expectfee;

        String tmpmu = String.format("%.0f", monthlyfee);
        String tmpeg = String.format("%.0f", reducefee);
        String tmpef = String.format("%.0f", expectfee);

        monthlyUse = findViewById(R.id.monthlyUse); monthlyUse.setText(tmpmu+"원");
        expectReduce = findViewById(R.id.expectReduce); expectReduce.setText(tmpeg+"원");
        expectFee = findViewById(R.id.expectFee); expectFee.setText(tmpef+"원");
        companyListFab = findViewById(R.id.companyListFab);

        //down = (1-(expectfee/userfee))*100;


        companyListFab.setOnClickListener(new View.OnClickListener() {
            Intent intent = new Intent(resultActivity.this, companyListActivity.class);
            @Override
            public void onClick(View v) {

                startActivity(intent);

            }
        });
        setBarChart();
    }

    private void setBarChart(){

        BarChart barChart = (BarChart) findViewById(R.id.chart);


        ArrayList<BarEntry> val=new ArrayList<>(); //데이터의 값
        val.add(new BarEntry((float)(monthlyfee),0));
        val.add(new BarEntry((float)(expectfee),1));


        //String tmpdown = String.format("%.0f",down);
       //BarChart.setCenterText("전기세 절감율은 약"+ tmpdown + "%입니다.");


        BarDataSet dataSet = new BarDataSet(val,"aa");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        BarData data = new BarData((dataSet));
        data.setValueTextSize(0);

        barChart.setData(data);
        barChart.setFitBars(false);
        barChart.animateXY(1000,1000);
        barChart.invalidate();
    }
}
