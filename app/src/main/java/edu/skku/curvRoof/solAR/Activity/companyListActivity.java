package edu.skku.curvRoof.solAR.Activity;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.skku.curvRoof.solAR.Model.Company;
import edu.skku.curvRoof.solAR.R;

public class companyListActivity extends AppCompatActivity {
    private static String IP_ADDRESS = "127.0.0.1";
    private String jsonString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_list);

        jsonParser parser = new jsonParser();
        parser.execute("http://"+IP_ADDRESS+"/PHP_connection.php","");
    }

    private class jsonParser extends AsyncTask<String, Void, String>{


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
                putInfo();
            }
            super.onPostExecute(s);
        }
    }
    public void putInfo(){
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
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}



