package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Utils.companyListViewAdapter;
import edu.skku.curvRoof.solAR.Utils.historyListViewAdapter;
import edu.skku.curvRoof.solAR.Utils.historyListViewItem;

public class historyActivity extends AppCompatActivity {

    private ListView historylist;
    private historyListViewAdapter adapter = new historyListViewAdapter();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = database.getReference();
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        user = (User)getIntent().getSerializableExtra("user");

        historylist=(ListView)findViewById(R.id.historyListView);
        historylist.setAdapter(adapter);

        historylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                historyListViewItem item = (historyListViewItem)parent.getItemAtPosition(position);
                Intent historyIntent=new Intent(historyActivity.this,historyPageActivity.class);
                historyIntent.putExtra("trialID", item.getTime());
                historyIntent.putExtra("user", user);
                startActivity(historyIntent);

            }
        });

        mRef.child("user_list").child(user.getUserID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot trial : dataSnapshot.getChildren()) {
                    if (!trial.getKey().equals("elec_fee")) {
                        adapter.addItem(trial.getKey(), trial.child("panel_count").getValue().toString(), trial.child("expect_fee").getValue().toString());
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
