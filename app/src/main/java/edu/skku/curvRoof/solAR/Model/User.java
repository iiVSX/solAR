package edu.skku.curvRoof.solAR.Model;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User {
    private String userID;
    private String userNm;
    private double latitude;
    private double longitude;
    private String cityNm;
    private double area;
    private int area_type;
    private double azimuth;
    private double angle;
    private int panelNm;
    private String email;
    private String tel;

    FirebaseDatabase database;
    DatabaseReference myRef;

    User(final String email){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.child("user_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean flag = false;
                int user_num = 0;
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(child.getValue().equals(email)){
                        userID = child.getKey();
                        flag = true;
                    }
                    user_num++;
                }
                if(flag == false){
                    userID = String.valueOf(user_num + 1);
                    createUser();
                }
                else{
                    getUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void createUser(){

    }
    public void getUser(){

    }
    public String getUserID() {
        return userID;
    }

    public String getUserNm() {
        return userNm;
    }

    public void setUserNm(String userNm) {
        this.userNm = userNm;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCityNm() {
        return cityNm;
    }

    public void setCityNm(String cityNm) {
        this.cityNm = cityNm;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public int getArea_type() {
        return area_type;
    }

    public void setArea_type(int area_type) {
        this.area_type = area_type;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public int getPanelNm() {
        return panelNm;
    }

    public void setPanelNm(int panelNm) {
        this.panelNm = panelNm;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
