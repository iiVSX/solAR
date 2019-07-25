package edu.skku.curvRoof.solAR.Model;

public class Point {
    private float x,y,z,conf;

    public Point(){
    }

    public Point(float x, float y, float z, float conf){
        this.x = x;
        this.y = y;
        this.z = z;
        this.conf = conf;
    }

    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }
    public float getZ(){
        return z;
    }
    public float getConf(){
        return conf;
    }
}
