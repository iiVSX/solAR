package edu.skku.curvRoof.solAR.Model;

public class Plane {
    private Point ll,lr,ul,ur;
    private float[] normal = new float[3];

    public Plane(Point ll, Point lr, Point ul, Point ur){

        // Duplicate 4 points
        this.ll = ll;
        this.lr = lr;
        this.ul = ul;
        this.ur = ur;

        this.calNormal();
    }

    public Point getLl() {
        return ll;
    }
    public Point getLr() {
        return lr;
    }
    public Point getUl() {
        return ul;
    }
    public Point getUr() {
        return ur;
    }
    public float[] getNormal() {return normal;}

    public void setLl(Point ll) {
        this.ll = ll;
        this.calNormal();
    }
    public void setLr(Point lr) {
        this.lr = lr;
        this.calNormal();
    }
    public void setUl(Point ul) {
        this.ul = ul;
        this.calNormal();
    }
    public void setUr(Point ur) {
        this.ur = ur;
        this.calNormal();
    }

    protected void calNormal(){
        // Calculate normal vector
        float[] vec1 = new float[3];
        float[] vec2 = new float[3];

        vec1[0] = lr.getX() - ll.getX();
        vec1[1] = lr.getY() - ll.getY();
        vec1[2] = lr.getZ() - ll.getZ();

        vec2[0] = ul.getX() - ll.getX();
        vec2[1] = ul.getY() - ll.getY();
        vec2[2] = ul.getZ() - ll.getZ();

        this.normal[0] = vec1[1]*vec2[2] - vec1[2]*vec2[1];
        this.normal[1] = vec1[2]*vec2[0] - vec1[0]*vec2[2];
        this.normal[2] = vec1[0]*vec2[1] - vec1[1]*vec2[0];
    }

}
