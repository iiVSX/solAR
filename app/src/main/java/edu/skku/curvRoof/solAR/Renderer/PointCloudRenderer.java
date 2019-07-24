package edu.skku.curvRoof.solAR.Renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.google.ar.core.PointCloud;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import edu.skku.curvRoof.solAR.Utils.ShaderUtil;
public class PointCloudRenderer {
    private static final String TAG = PointCloudRenderer.class.getSimpleName();

    private int mProgram;

    private static final String VERTEX_SHADER_NAME="point_cloud.vert";
    private static final String FRAGMENT_SHADER_NAME="point_cloud.frag";

    private static final int COORDS_PER_VERTEX = 4;
    private static final int FLOAT_SIZE = 4;

    private int vertexShader;
    private int fragmentShader;

    private int mPosition;
    private int mColor;
    private int uMVPMatrixHandle;
    private int mSize;

    private int vbo;
    private int vboSize;
    private FloatBuffer pointCloud;

    public void createGlThread(Context context) throws IOException {
        int[] buffers = new int[1];

        GLES20.glGenBuffers(1, buffers, 0);
        vbo = buffers[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        vboSize = 1000 * 16;
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vboSize, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        fragmentShader = ShaderUtil.loadGLShader(TAG,context,GLES20.GL_FRAGMENT_SHADER,FRAGMENT_SHADER_NAME);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

        mPosition = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mColor = GLES20.glGetAttribLocation(mProgram, "u_Color");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_ModelViewProjection");
        mSize = GLES20.glGetUniformLocation(mProgram, "u_PointSize");
    }

    public void update(PointCloud cloud){
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        // If the VBO is not large enough to fit the new point cloud, resize it.
        pointCloud = cloud.getPoints();
        if (pointCloud.remaining() * 4 > vboSize) {
            while (pointCloud.remaining() * 4 > vboSize) {
                vboSize *= 2;
            }
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vboSize, null, GLES20.GL_DYNAMIC_DRAW);
        }
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, 0, pointCloud.remaining() * 4, pointCloud);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void draw(float[] vpMatrix){
        GLES20.glUseProgram(mProgram);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        float[] color = new float[pointCloud.remaining()];
        for(int i=3; i<pointCloud.remaining(); i=i+4){
            float conf = pointCloud.get(i);

            if(conf <= 0.33f){
                color[i-3] = 1.0f-conf;
                color[i-2] = conf;
                color[i-1] = conf;
                color[i] = 1.0f;
            }
            else if(conf <= 0.66f){
                color[i-3] = conf;
                color[i-2] = 1.0f-conf;
                color[i-1] = conf;
                color[i] = 1.0f;
            }
            else{
                color[i-3] = conf;
                color[i-2] = conf;
                color[i-1] = 1.0f-conf;
                color[i] = 1.0f;
            }
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(FLOAT_SIZE*color.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer colorBuffer = bb.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        GLES20.glEnableVertexAttribArray(mPosition);
        GLES20.glEnableVertexAttribArray(mColor);

        GLES20.glVertexAttribPointer(mPosition, FLOAT_SIZE,GLES20.GL_FLOAT,false,COORDS_PER_VERTEX*FLOAT_SIZE, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle,1,false,vpMatrix,0);
        GLES20.glUniform1f(mSize, 15.0f);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glVertexAttribPointer(mColor, FLOAT_SIZE, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX*FLOAT_SIZE, colorBuffer);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, pointCloud.remaining()/4);
        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glDisableVertexAttribArray(mColor);

    }

}
