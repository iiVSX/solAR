package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import edu.skku.curvRoof.solAR.R;

public class resultActivity extends AppCompatActivity {


    private TextView monthlyUse;
    private TextView expectGen;
    private TextView realUse;
    private TextView expectFee;
    public double monthlyuse;// = i.getDoubleExtra("monthlyuse",0);
    public double expectgen;// = i.getDoubleExtra("expectgen",0);
    public double realuse;// = i.getDoubleExtra("realuse",0);
    public double expectfee;// = i.getDoubleExtra("expectfee",0);
    public double userfee;
    public double down;
    private Button companyListBtn;
    Intent intent = new Intent(this, companyListActivity.class);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //세로본능
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        Intent i = getIntent();



        monthlyuse = i.getDoubleExtra("monthlyuse",0);
        expectgen = i.getDoubleExtra("expectgen",0);
        realuse = i.getDoubleExtra("realuse",0);
        expectfee = i.getDoubleExtra("expectfee",0);
        userfee = i.getDoubleExtra("usermoney",0);

        String tmpmu = String.format("%.0f", monthlyuse);
        String tmpeg = String.format("%.0f", expectgen);
        String tmprl = String.format("%.0f", realuse);
        String tmpef = String.format("%.0f", expectfee);

        monthlyUse = findViewById(R.id.montlyuse); monthlyUse.setText(tmpmu+"kWh");
        expectGen = findViewById(R.id.expectgen); expectGen.setText(tmpeg+"kWh");
        realUse = findViewById(R.id.realuse); realUse.setText(tmprl+"kWh");
        expectFee = findViewById(R.id.expectfee); expectFee.setText(tmpef+"원");
        companyListBtn = findViewById(R.id.companyListBtn);

        down = (1-(expectfee/userfee))*100;
        setPieChart();

        companyListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
    }

    private void setPieChart(){

        PieChart pieChart = (PieChart) findViewById(R.id.chart);

        pieChart.setUsePercentValues(true);

        ArrayList<PieEntry> val=new ArrayList<>(); //데이터의 값
        val.add(new PieEntry((float)(userfee-expectfee),""));
        val.add(new PieEntry((float)(expectfee),""));


        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        String tmpdown = String.format("%.0f",down);
        pieChart.setCenterText("전기세 절감율은 약"+ tmpdown + "%입니다.");
        pieChart.setHoleRadius(70);

        PieDataSet dataSet = new PieDataSet(val,"");
        dataSet.setSliceSpace(2f);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        PieData data = new PieData((dataSet));
        data.setValueTextSize(0);

        pieChart.setData(data);
    }
}
