package edu.skku.curvRoof.solAR.Activity;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import edu.skku.curvRoof.solAR.Model.Company;
import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.companyListViewAdapter;
import edu.skku.curvRoof.solAR.companyListViewItem;

public class companyListActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "203.252.34.202";
    private String jsonString;
    private ArrayList<Company> companyList = new ArrayList<Company>();
    private ListView listview;
    private companyListViewAdapter adapter = new companyListViewAdapter();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_list);

        listview=(ListView)findViewById(R.id.companyListView);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                companyListViewItem item=(companyListViewItem)parent.getItemAtPosition(position);

                Drawable cpnyIcon=item.getCompanyIcon();
                String cpnyName=item.getCompanyName();
                String amt=item.getAmount();

                Drawable rgnIcon=item.getRegionIcon();
                String rgnName=item.getRegionName();
            }
        });

        phpConnection conn = new phpConnection();
        conn.execute("http://"+IP_ADDRESS+"/get_company.php","");
    }

    private class phpConnection extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... strings) {
            String serverURL = strings[0];
            String postParameters = strings[1];

            try{
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();

                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseCode = httpURLConnection.getResponseCode();
                Log.d("responseCode", String.valueOf(responseCode));

                InputStream inputStream;
                if(responseCode == HttpURLConnection.HTTP_OK){
                    inputStream = httpURLConnection.getInputStream();
                }else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine())!=null){
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString().trim();
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null){
                return;
            }
            else{
                jsonString = s;
                jsonParser();
            }
            super.onPostExecute(s);
        }
    }
    public void jsonParser(){
        try{
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("company_list");

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject company = jsonArray.getJSONObject(i);
                int id = company.getInt("comid");
                String name = company.getString("comname");
                double latitude = company.getDouble("latitude");
                double longitude = company.getDouble("longitude");
                String cityNm = company.getString("cityNm");
                int buildCnt = company.getInt("buildcnt");
                String email = company.getString("email");
                String tel = company.getString("tel");

                Company com = new Company();
                com.setId(id);
                com.setCompany_name(name);
                com.setLatitude(latitude);
                com.setLongitude(longitude);
                com.setCityNm(cityNm);
                com.setBuildcnt(buildCnt);
                com.setEmail(email);
                com.setTel(tel);

                companyList.add(com);
            }

            for(Company com : companyList){
                adapter.addItem(ContextCompat.getDrawable(this,R.drawable.ic_01_menu), com.getCompany_name(), "100000", ContextCompat.getDrawable(this,R.drawable.ic_12_gpsrecept), com.getCityNm());
            }
            adapter.notifyDataSetChanged();
        }catch (Exception e){
            Log.d("PLUSULTRA", e.getMessage());
        }
    }
}



