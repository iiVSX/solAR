package edu.skku.curvRoof.solAR.Model;

import com.google.ar.core.Camera;
import com.google.ar.core.Pose;

public class Plane {
    private float[] ll,lr,ul,ur;
    private float[] normal = new float[3];

    private float[] planeVertex;

    public Plane(float[] ll, float[] lr, float[] ur, float[] ul){           //Plane 사용전 항상 checkNormal 을 해주세요

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
    }
    public void setLr(float[] lr) {
        this.lr = lr;
    }
    public void setUl(float[] ul) {
        this.ul = ul;
    }
    public void setUr(float[] ur) {
        this.ur = ur;
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

    public int hitPoint(float[] ray){
        // ray[0~2] : camera 좌표
        // ray[3~5] : ray 벡터 (0,0,0)기준
        // 0: no point, 1 : ll, 2 : lr, 3 : ur, 4:ul
        float threshold = 0.1f;
        float distance = 10;
        float k;
        float[] PonRay = new float[3];

        //check ll(1)
        k = (ray[0] * ray[3] + ray[1] * ray[4] + ray[2] + ray[5])
                - (ll[0] * ray[3] + ll[1] * ray[4]+ ll[2] * ray[5]);
        k /= (ray[3] *ray[3]  + ray[4]*ray[4] +ray[5]*ray[5]);
        PonRay[0] = ray[0] + k * ray[3];
        PonRay[1] = ray[1] + k * ray[4];
        PonRay[2] = ray[2] + k * ray[5];

        distance = (ll[0] - PonRay[0] *ll[0] - PonRay[0] ) +
                   (ll[1] - PonRay[1] *ll[1] - PonRay[1] ) +
                   (ll[2] - PonRay[2] *ll[2] - PonRay[2] );
        if(distance < threshold) return 1;

        //check lr(2)
        k = (ray[0] * ray[3] + ray[1] * ray[4] + ray[2] + ray[5])
                - (lr[0] * ray[3] + lr[1] * ray[4]+ lr[2] * ray[5]);
        k /= (ray[3] *ray[3]  + ray[4]*ray[4] +ray[5]*ray[5]);
        PonRay[0] = ray[0] + k * ray[3];
        PonRay[1] = ray[1] + k * ray[4];
        PonRay[2] = ray[2] + k * ray[5];

        distance = (lr[0] - PonRay[0] *lr[0] - PonRay[0] ) +
                (lr[1] - PonRay[1] *lr[1] - PonRay[1] ) +
                (lr[2] - PonRay[2] *lr[2] - PonRay[2] );
        if(distance < threshold) return 2;

        //check ur(3)
        k = (ray[0] * ray[3] + ray[1] * ray[4] + ray[2] + ray[5])
                - (ur[0] * ray[3] + ur[1] * ray[4]+ ur[2] * ray[5]);
        k /= (ray[3] *ray[3]  + ray[4]*ray[4] +ray[5]*ray[5]);
        PonRay[0] = ray[0] + k * ray[3];
        PonRay[1] = ray[1] + k * ray[4];
        PonRay[2] = ray[2] + k * ray[5];

        distance = (ur[0] - PonRay[0] *ur[0] - PonRay[0] ) +
                (ur[1] - PonRay[1] *ur[1] - PonRay[1] ) +
                (ur[2] - PonRay[2] *ur[2] - PonRay[2] );
        if(distance < threshold) return 3;

        //check ul(4)
        k = (ray[0] * ray[3] + ray[1] * ray[4] + ray[2] + ray[5])
                - (ul[0] * ray[3] + ul[1] * ray[4]+ ul[2] * ray[5]);
        k /= (ray[3] *ray[3]  + ray[4]*ray[4] +ray[5]*ray[5]);
        PonRay[0] = ray[0] + k * ray[3];
        PonRay[1] = ray[1] + k * ray[4];
        PonRay[2] = ray[2] + k * ray[5];

        distance = (ul[0] - PonRay[0] *ul[0] - PonRay[0] ) +
                (ul[1] - PonRay[1] *ul[1] - PonRay[1] ) +
                (ul[2] - PonRay[2] *ul[2] - PonRay[2] );
        if(distance < threshold) return 4;

        return 0;



    }
}
