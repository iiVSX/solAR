package edu.skku.curvRoof.solAR.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.skku.curvRoof.solAR.R;

public class companyListViewAdapter extends BaseAdapter {

    private ArrayList<companyListViewItem> listViewItemList=new ArrayList<companyListViewItem>();
    public companyListViewAdapter(){

    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
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
            convertView=inflater.inflate(R.layout.companylistview_item,parent,false);

        }

        ImageView companyIconView = (ImageView)convertView.findViewById(R.id.companyIcon);
        TextView companyNameView=(TextView)convertView.findViewById(R.id.companyName);
        TextView amountView = (TextView)convertView.findViewById(R.id.amount);

        ImageView regionIconView = (ImageView)convertView.findViewById(R.id.regionIcon);
        TextView regionNameView = (TextView)convertView.findViewById(R.id.regionName);

        companyListViewItem listViewItem = listViewItemList.get(position);

        companyIconView.setImageDrawable(listViewItem.getCompanyIcon());
        companyNameView.setText(listViewItem.getCompanyName());
        amountView.setText(listViewItem.getAmount());

        regionIconView.setImageDrawable(listViewItem.getRegionIcon());
        regionNameView.setText(listViewItem.getRegionName());

        return convertView;
    }
    public void addItem(Drawable companyIcon, String companyName, String amount, Drawable regionIcon, String regionName, String tel){
        companyListViewItem item=new companyListViewItem();

        item.setCompanyIcon(companyIcon);
        item.setCompanyName(companyName);
        item.setAmount(amount);
        item.setCompanyTel(tel);
        item.setRegionIcon(regionIcon);
        item.setRegionName(regionName);

        listViewItemList.add(item);
    }
}
