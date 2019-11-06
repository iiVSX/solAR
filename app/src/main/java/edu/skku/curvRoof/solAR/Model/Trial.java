package edu.skku.curvRoof.solAR.Model;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Trial implements Serializable {
    double latitude, longitude;
    double angle, area_width, area_height, azimuth;
    int area_type, panel_count;

    public double getElec_gen() {
        return elec_gen;
    }

    public void setElec_gen(double elec_gen) {
        this.elec_gen = elec_gen;
    }

    double elec_gen;
    String captureUrl;
    String trialID;

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

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(double azimuth) {
        this.azimuth = azimuth;
    }

    public int getArea_type() {
        return area_type;
    }

    public void setArea_type(int area_type) {
        this.area_type = area_type;
    }

    public int getPanel_count() {
        return panel_count;
    }

    public void setPanel_count(int panel_count) {
        this.panel_count = panel_count;
    }

    public String getCaptureUrl() {
        return captureUrl;
    }

    public void setCaptureUrl(String captureUrl) {
        this.captureUrl = captureUrl;
    }

    public String getTrialID() {
        return trialID;
    }

    public void setTrialID(String trialID) {
        this.trialID = trialID;
    }

    public double getArea_width() {
        return area_width;
    }

    public void setArea_width(double area_width) {
        this.area_width = area_width;
    }

    public double getArea_height() {
        return area_height;
    }

    public void setArea_height(double area_height) {
        this.area_height = area_height;
    }
}
