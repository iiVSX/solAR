package edu.skku.curvRoof.solAR.Utils;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class GpsUtil extends Service implements LocationListener {
    private Context context;
    private double latitude, longitude;
    private LocationManager lm;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private long MIN_TIME = 1000 * 60, MIN_DISTANCE = 10;
    private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    public GpsUtil(Context context) {
        this.context = context;
        getLocation();
    }


    public void getLocation(){
        try{
            lm = (LocationManager)context.getSystemService(LOCATION_SERVICE);

            isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled){
                for(String permission : REQUIRED_PERMISSSIONS){
                    if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(context, "PERMISSION NEEDED", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            else if(isNetworkEnabled){
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                if(lm != null){
                    Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location != null){
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                }
            }
            else{
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                if(lm != null){
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location != null){
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
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
