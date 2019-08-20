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
    private Point ll,lr,ul,ur;
    private float[] normal = new float[3];

    private float[] planeVertex;

    private static final String VERTEX_SHADER_NAME = "plane.vert";
    private static final String FRAGMENT_SHADER_NAME = "plane.frag";
    private int vertexShader;
    private int fragmentShader;

    private int mProgram;

    private int mPosition;
    private int mColor_u;
    private int uMVPMatrixHandle;

    private static final int COORDS_PER_VERTEX = 4;
    private static final int FLOAT_SIZE = 4;

    private FloatBuffer vertexBuffer;

    public Plane(Point ll, Point lr, Point ur, Point ul){

        // Duplicate 4 points
        this.ll = ll;
        this.lr = lr;
        this.ul = ul;
        this.ur = ur;

        planeVertex = new float[] {
                ll.getX(), ll.getY(), ll.getZ(),
                lr.getX(), lr.getY(), lr.getZ(),
                ul.getX(), ul.getY(), ul.getZ(),

                ul.getX(), ul.getY(), ul.getZ(),
                lr.getX(), lr.getY(), lr.getZ(),
                ur.getX(), ur.getY(), ur.getZ(),

        };

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

    public void bufferUpdate(){
        planeVertex = new float[] {
                ll.getX(), ll.getY(), ll.getZ(),
                lr.getX(), lr.getY(), lr.getZ(),
                ul.getX(), ul.getY(), ul.getZ(),

                ul.getX(), ul.getY(), ul.getZ(),
                lr.getX(), lr.getY(), lr.getZ(),
                ur.getX(), ur.getY(), ur.getZ(),

        };

        ByteBuffer bb = ByteBuffer.allocateDirect(planeVertex.length * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(planeVertex);
        vertexBuffer.position(0);
    }

    public void createGlThread(Context context) throws IOException {


        vertexShader = ShaderUtil.loadGLShader("Plane", context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        fragmentShader = ShaderUtil.loadGLShader("Plane", context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);

        //bind shader's variable position
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

        mPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mColor_u = GLES20.glGetUniformLocation(mProgram, "u_Color");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

    }

    public void draw(float[] vpMatrix){
        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mPosition);

        GLES20.glVertexAttribPointer(mPosition, FLOAT_SIZE, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * FLOAT_SIZE, vertexBuffer);
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, vpMatrix, 0);

        GLES20.glUniform4f(mColor_u, 0.0f, 0.0f, 1.0f, 1.0f);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffer.remaining()/4);
        GLES20.glDisableVertexAttribArray(mPosition);

    }

}
