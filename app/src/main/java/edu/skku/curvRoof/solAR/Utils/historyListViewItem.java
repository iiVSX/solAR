package edu.skku.curvRoof.solAR.Utils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.skku.curvRoof.solAR.R;

public class historyListViewItem{

    private String info;
    private String time;
    private String panelnum;
    private String money;

    public void setInfo(String in){info=in;}
    public void setTime(String t){time=t;}
    public void setPanelnum(String p){panelnum=p;}
    public void setMoney(String m){money=m;}

    public String getInfo(){return this.info;}
    public String getTime(){ return this.time;}
    public String getPanelnum(){return this.panelnum;}
    public String getMoney(){return this.money;}

}
