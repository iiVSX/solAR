package edu.skku.curvRoof.solAR.Model;

import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.Camera;
import com.google.ar.core.Pose;

import edu.skku.curvRoof.solAR.Utils.VectorCal;

public class Plane {
    private float[] ll,lr,ul,ur;
    public float[] pivot; // used for calculating rayOnPlane
    private float[] normal = new float[3];

    private float[] planeVertex;

    public Plane(float[] ll, float[] lr, float[] ur, float[] ul, Camera camera){           //Plane 사용전 항상 checkNormal 을 해주세요

        // Duplicate 4 points
        this.ll = ll;
        this.lr = lr;
        this.ul = ul;
        this.ur = ur;


        planeVertex = new float[] {
                ul[0], ul[1], ul[2],
                ll[0], ll[1], ll[2],
                lr[0], lr[1], lr[2],
                ur[0], ur[1], ur[2],
        };

        this.calNormal();
        this.checkNormal(camera);
        pivot = new float[3];
        pivot[0] = ll[0];
        pivot[1] = ll[1];
        pivot[2] = ll[2];
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
        planeVertex = new float[] {
                ul[0], ul[1], ul[2],
                ll[0], ll[1], ll[2],
                lr[0], lr[1], lr[2],
                ur[0], ur[1], ur[2],
        };
    }
    public void setLr(float[] lr) {
        this.lr = lr;
        planeVertex = new float[] {
                ul[0], ul[1], ul[2],
                ll[0], ll[1], ll[2],
                lr[0], lr[1], lr[2],
                ur[0], ur[1], ur[2],
        };
    }
    public void setUl(float[] ul) {
        this.ul = ul;
        planeVertex = new float[] {
                ul[0], ul[1], ul[2],
                ll[0], ll[1], ll[2],
                lr[0], lr[1], lr[2],
                ur[0], ur[1], ur[2],
        };
    }
    public void setUr(float[] ur) {
        this.ur = ur;
        planeVertex = new float[] {
                ul[0], ul[1], ul[2],
                ll[0], ll[1], ll[2],
                lr[0], lr[1], lr[2],
                ur[0], ur[1], ur[2],
        };
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

        float scala = (float)Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] *normal[2]);

        this.normal[0] /= scala;
        this.normal[1] /= scala;
        this.normal[2] /= scala;

    }

    public boolean checkNormal(Camera camera){
        Pose pose = camera.getPose();
        float[] z_dir = pose.getZAxis();

        if(z_dir[0] * normal[0] + z_dir[1] * normal[1] + z_dir[2] * normal[2] >= 0){
            return true;
        }
        else{
            normal[0] = -normal[0];
            normal[1] = -normal[1];
            normal[2] = -normal[2];
            return false;
        }
    }

    public int hitPoint(float[] ray){			// Plane
        // ray[0~2] : camera 좌표
        // ray[3~5] : ray 벡터 (0,0,0)기준
        // 0: no point, 1 : ll, 2 : lr, 3 : ur, 4:ul
        float threshold = 0.1f;
        double distance = 10;
        float[] PA = new float[3];
        float[] outer = new float[3];

        //check ll(1)
        PA[0] = ll[0] - ray[0];
        PA[1] = ll[1] - ray[1];
        PA[2] = ll[2] - ray[2];

        outer[0] = PA[1]*ray[5] - PA[2]*ray[4];
        outer[1] = PA[2]*ray[3] - PA[0]*ray[5];
        outer[2] = PA[0]*ray[4] - PA[1]*ray[3];
        distance = Math.sqrt(outer[0]*outer[0]+outer[1]*outer[1]+outer[2]*outer[2]);

        Log.d("hitPoint", "ll : " + distance);

        if(distance < threshold) return 1;

        //check lr(2)
        PA[0] = lr[0] - ray[0];
        PA[1] = lr[1] - ray[1];
        PA[2] = lr[2] - ray[2];

        outer[0] = PA[1]*ray[5] - PA[2]*ray[4];
        outer[1] = PA[2]*ray[3] - PA[0]*ray[5];
        outer[2] = PA[0]*ray[4] - PA[1]*ray[3];
        distance = Math.sqrt(outer[0]*outer[0]+outer[1]*outer[1]+outer[2]*outer[2]);

        Log.d("hitPoint", "lr : " + distance);

        if(distance < threshold) return 2;

        //check ur(1)
        PA[0] = ur[0] - ray[0];
        PA[1] = ur[1] - ray[1];
        PA[2] = ur[2] - ray[2];

        outer[0] = PA[1]*ray[5] - PA[2]*ray[4];
        outer[1] = PA[2]*ray[3] - PA[0]*ray[5];
        outer[2] = PA[0]*ray[4] - PA[1]*ray[3];
        distance = Math.sqrt(outer[0]*outer[0]+outer[1]*outer[1]+outer[2]*outer[2]);

        Log.d("hitPoint", "ur : " + distance);

        if(distance < threshold) return 3;

        //check ul(1)
        PA[0] = ul[0] - ray[0];
        PA[1] = ul[1] - ray[1];
        PA[2] = ul[2] - ray[2];

        outer[0] = PA[1]*ray[5] - PA[2]*ray[4];
        outer[1] = PA[2]*ray[3] - PA[0]*ray[5];
        outer[2] = PA[0]*ray[4] - PA[1]*ray[3];
        distance = Math.sqrt(outer[0]*outer[0]+outer[1]*outer[1]+outer[2]*outer[2]);

        Log.d("hitPoint", "ul : " + distance);

        if(distance < threshold) return 4;


        return 0;
    }

    public float[] rayOnPlane(float[] ray){ // ray[0~2] : 카메라 좌표, ray[3~5] : ray의 방향 벡터
        float[] pointOnRay = new float[3];

        float u = normal[0]*ray[3] + normal[1]*ray[4] + normal[2]*ray[5];
        u =    (normal[0]*(pivot[0] - ray[0]) +
                normal[1]*(pivot[1] - ray[1]) +
                normal[2]*(pivot[2] - ray[2])) / u;

        pointOnRay[0] = ray[0] + (ray[3]*u);
        pointOnRay[1] = ray[1] + (ray[4]*u);
        pointOnRay[2] = ray[2] + (ray[5]*u);

        return pointOnRay;
    }

    public float[] getPanelPivot(){
        float[] pivot = {0,0,0};
        float[] y = {0,1,0};
        if(VectorCal.inner(normal,y) == 1){
            pivot[0] = (ll[0]+lr[0])/2;
            pivot[1] = (ll[1]+lr[1])/2;
            pivot[2] = (ll[2]+lr[2])/2;

            return pivot;
        }
        else{
            float[] mid = new float[3];
            mid[0] = (ll[0] + lr[0] + ur[0] + ul[0])/4;
            mid[1] = (ll[1] + lr[1] + ur[1] + ul[1])/4;
            mid[2] = (ll[2] + lr[2] + ur[2] + ul[2])/4;

            if(ll[1] < mid[1]){
                pivot[0] += ll[0];
                pivot[1] += ll[1];
                pivot[2] += ll[2];
            }
            if(lr[1] < mid[1]){
                pivot[0] += lr[0];
                pivot[1] += lr[1];
                pivot[2] += lr[2];
            }
            if(ul[1] < mid[1]){
                pivot[0] += ul[0];
                pivot[1] += ul[1];
                pivot[2] += ul[2];
            }
            if(ur[1] < mid[1]){
                pivot[0] += ur[0];
                pivot[1] += ur[1];
                pivot[2] += ur[2];
            }

            pivot[0] /= 2.0f;
            pivot[1] /= 2.0f;
            pivot[2] /= 2.0f;


            return pivot;
        }
    }

    public float getAngle(){
        float angle = 0;
        float[] ydir = new float[]{0,1,0};

        angle = normal[0]*ydir[0] + normal[1]*ydir[1] + normal[2]*ydir[2];  // 내적

        angle = (float)Math.acos((double)angle);

        angle = (float)Math.toDegrees(angle);

        return angle;
    }

    public float getDir(){
        boolean flag = false;
        float angle = 0;
        float[] zdir = {0,0,1};
        float[] xdir = {1,0,0};
        float[] ydir = {0,1,0};
        float[] dir = {lr[0] - ll[0], 0, lr[2]-ll[2]};
        dir[0] /= VectorCal.vectorSize(dir);
        dir[2] /= VectorCal.vectorSize(dir);
        if(VectorCal.inner(normal, ydir) == 1.0f){
            angle = dir[0] * xdir[0] + dir[1] * xdir[1] + dir[2] * xdir[2];  // 내적
            angle = (float)Math.acos((double)angle);
            angle = (float)Math.toDegrees(angle);

            if(dir[0] * zdir[0] + dir[1] * zdir[1] + dir[2] * zdir[2] > 0){
                angle = -angle;
            }

            Log.d("dir", dir[0] * zdir[0] + dir[1] * zdir[1] + dir[2] * zdir[2] + " ");

            return angle;
        }
        else{
            float[] proNormal = {normal[0], 0, normal[2]};
            VectorCal.normalize(proNormal);

            angle = proNormal[0] * xdir[0] + proNormal[2] * xdir[2];  // 내적
            angle = (float)Math.asin((double)angle);
            angle = (float)Math.toDegrees(angle);

            //Log.d("dir", dir[0] * xdir[0] + dir[1] * xdir[1] + dir[2] * xdir[2] + " ");
            Log.d("dir", angle + " ");
            return angle;
        }

    }

}
