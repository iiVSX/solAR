package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Utils.companyListViewAdapter;
import edu.skku.curvRoof.solAR.Utils.historyListViewAdapter;

public class historyActivity extends AppCompatActivity {

    private ListView historylist;
    private historyListViewAdapter adapter = new historyListViewAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historylist=(ListView)findViewById(R.id.historyListView);
        historylist.setAdapter(adapter);

        historylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent historyIntent=new Intent(historyActivity.this,historyPageActivity.class);
                startActivity(historyIntent);

            }
        });

        adapter.addItem("2019년 11월 3일 오후 9시 47분의 데이터","15개","130000원");
        adapter.addItem("2019년 11월 2일 오전 2시 47분의 데이터","12개","30000원");
    }
}
