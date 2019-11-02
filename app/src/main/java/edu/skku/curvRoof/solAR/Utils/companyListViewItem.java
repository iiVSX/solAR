package edu.skku.curvRoof.solAR.Utils;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class companyListViewItem {
    private Drawable companyIcon;
    private String companyName;
    private String amount;
    private Drawable regionIcon;
    private String regionName;
    private String companyTel;

    public void setCompanyTel(String tel){companyTel = tel;}
    public void setCompanyIcon(Drawable icon){
        companyIcon=icon;
    }
    public void setCompanyName(String name){
        companyName=name;
    }
    public void setAmount(String amt){
        amount=amt;
    }
    public void setRegionIcon(Drawable icon){
        regionIcon=icon;
    }
    public void setRegionName(String name){
        regionName=name;
    }

    public Drawable getCompanyIcon(){
        return this.companyIcon;
    }
    public String getCompanyName(){
        return this.companyName;
    }
    public String getAmount(){
        return this.amount;
    }
    public String getCompanyTel(){return this.companyTel;}
    public Drawable getRegionIcon(){
        return this.regionIcon;
    }
    public String getRegionName(){
        return this.regionName;
    }
}
