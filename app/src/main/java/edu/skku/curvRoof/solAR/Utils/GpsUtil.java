package edu.skku.curvRoof.solAR.Utils;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class GpsUtil extends Service implements LocationListener {
    private final Context context;

    private double latitude, longitude;
    protected LocationManager lm;
    private Location location;

    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;

    private long MIN_TIME = 100, MIN_DISTANCE = 10;
    private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private Geocoder mGeocoder;
    private String address;

    public GpsUtil(Context context) {
        this.context = context;
        mGeocoder = new Geocoder(context);
        getLocation();
    }


    public String getLocation(){
        try{
            lm = (LocationManager)context.getSystemService(LOCATION_SERVICE);

            isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled){
                for(String permission : REQUIRED_PERMISSSIONS){
                    if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(context, "PERMISSION NEEDED", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }
            else if(isNetworkEnabled){
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                if(lm != null){
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location != null){
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                }
            }
            else{
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                if(lm != null){
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location != null){
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                }
            }

        }catch(Exception e){
            Log.d("Plus",e.getMessage());
        }
        return null;
    }

    public void geocodedAddress(){
        try{
            List<Address> address_list = mGeocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            address = address_list.get(0).getAddressLine(0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getAddress(){
        return address;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            Log.d("Plus", String.valueOf(longitude)+String.valueOf(latitude));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
