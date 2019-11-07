package edu.skku.curvRoof.solAR.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.skku.curvRoof.solAR.Model.Company;
import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Utils.companyListViewAdapter;
import edu.skku.curvRoof.solAR.Utils.companyListViewItem;

public class companyListActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "192.168.0.2";
    private String jsonString;
    private ArrayList<Company> companyList = new ArrayList<Company>();
    private ListView listview;
    private companyListViewAdapter adapter = new companyListViewAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_list);

        listview = (ListView) findViewById(R.id.companyListView);
        listview.setAdapter(adapter);

        getCompanyList();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                companyListViewItem item = (companyListViewItem) parent.getItemAtPosition(position);

                Drawable cpnyIcon = item.getCompanyIcon();
                String cpnyName = item.getCompanyName();
                String amt = item.getAmount();

                Drawable rgnIcon = item.getRegionIcon();
                String rgnName = item.getRegionName();
                String companyTel = item.getCompanyTel();

                Log.d("iiVSX", companyTel);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "전화걸기 권한을 설정해주세요", Toast.LENGTH_SHORT);
                    return;
                }
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+companyTel)));
            }
        });

    }
    public void getCompanyList() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mRef = database.getReference();

        mRef.child("company_list").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot com : dataSnapshot.getChildren()){
                    Company company = new Company();
                    company.setTel(com.child("tel").getValue().toString());
                    company.setEmail(com.child("email").getValue().toString());
                    company.setCompany_name(com.getKey());
                    company.setCityNm(com.child("address").getValue().toString());

                    companyList.add(company);
                }

                for(Company com : companyList){
                    adapter.addItem(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_19_panel), com.getCompany_name(), com.getEmail(), ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_12_gpsrecept), com.getCityNm(), com.getTel());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}



