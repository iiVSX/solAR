package edu.skku.curvRoof.solAR.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import edu.skku.curvRoof.solAR.R;

public class historyListViewAdapter extends BaseAdapter {

    private ArrayList<historyListViewItem> historyViewItemList=new ArrayList<historyListViewItem>();
    public historyListViewAdapter(){

    }
    @Override
    public int getCount() {
        return historyViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return historyViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos=position;
        final Context context=parent.getContext();

        if(convertView==null){
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.historylistview_item,parent,false);

        }

        TextView timeView=(TextView)convertView.findViewById(R.id.timeText);
        TextView panelnumView=(TextView)convertView.findViewById(R.id.panelnumText);
        TextView moneyView=(TextView)convertView.findViewById(R.id.moneyText);

        historyListViewItem listViewItem = historyViewItemList.get(position);

        timeView.setText(listViewItem.getTime());
        panelnumView.setText(listViewItem.getPanelnum());
        moneyView.setText(listViewItem.getMoney());

        return convertView;
    }
    public void addItem(String in, String t, String p, String m){
        historyListViewItem item=new historyListViewItem();

        item.setInfo(in);
        item.setTime(t);
        item.setPanelnum(p);
        item.setMoney(m);

        historyViewItemList.add(item);
    }
}
