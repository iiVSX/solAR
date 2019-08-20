package edu.skku.curvRoof.solAR.Model;

import android.content.Context;
import android.opengl.GLES20;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import edu.skku.curvRoof.solAR.Utils.ShaderUtil;

public class Plane {
    private float[] ll,lr,ul,ur;
    private float[] normal = new float[3];

    private float[] planeVertex;

    public Plane(float[] ll, float[] lr, float[] ur, float[] ul){

        // Duplicate 4 points
        this.ll = ll;
        this.lr = lr;
        this.ul = ul;
        this.ur = ur;

        planeVertex = new float[] {
                ll[0], ll[1], ll[2],
                lr[0], lr[1], lr[2],
                ul[0], ul[1], ul[2],

                ul[0], ul[1], ul[2],
                lr[0], lr[1], lr[2],
                ur[0], ur[1], ur[2],
        };

        this.calNormal();
    }

    public float[] getLl() {
        return ll;
    }
    public float[] getLr() {
        return lr;
    }
    public float[] getUl() {
        return ul;
    }
    public float[] getUr() {
        return ur;
    }
    public float[] getNormal() {return normal;}

    public float[] getPlaneVertex(){return planeVertex;}

    public void setLl(float[] ll) {
        this.ll = ll;
        this.calNormal();
    }
    public void setLr(float[] lr) {
        this.lr = lr;
        this.calNormal();
    }
    public void setUl(float[] ul) {
        this.ul = ul;
        this.calNormal();
    }
    public void setUr(float[] ur) {
        this.ur = ur;
        this.calNormal();
    }

    protected void calNormal(){
        // Calculate normal vector
        float[] vec1 = new float[3];
        float[] vec2 = new float[3];

        vec1[0] = lr[0] - ll[0];
        vec1[1] = lr[1] - ll[1];
        vec1[2] = lr[2] - ll[2];

        vec2[0] = ul[0] - ll[0];
        vec2[1] = ul[1] - ll[1];
        vec2[2] = ul[2] - ll[2];

        this.normal[0] = vec1[1]*vec2[2] - vec1[2]*vec2[1];
        this.normal[1] = vec1[2]*vec2[0] - vec1[0]*vec2[2];
        this.normal[2] = vec1[0]*vec2[1] - vec1[1]*vec2[0];
    }
}
