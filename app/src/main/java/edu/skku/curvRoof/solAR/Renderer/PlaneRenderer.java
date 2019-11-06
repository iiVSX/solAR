package edu.skku.curvRoof.solAR.Renderer;

import android.content.Context;
import android.opengl.GLES20;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import edu.skku.curvRoof.solAR.Model.Plane;
import edu.skku.curvRoof.solAR.Utils.ShaderUtil;

public class PlaneRenderer {
    private static final String VERTEX_SHADER_NAME = "plane.vert";
    private static final String FRAGMENT_SHADER_NAME = "plane.frag";
    private int vertexShader;
    private int fragmentShader;

    private int mProgram;

    private int mPosition;
    private int mColor_u;
    private int uMVPMatrixHandle;

    private static final int COORDS_PER_VERTEX = 3;
    private static final int FLOAT_SIZE = 4;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer colorBuffer;

    private float[] planeVertex = new float[12];
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };
    private float[] colors = new float[]{
            0.0f, 0.0f, 1.0f, 0.5f,
            0.0f, 0.0f, 1.0f, 0.5f,
            0.0f, 0.0f, 1.0f, 0.5f,
            0,0f, 0.0f, 1.0f, 0.5f
    };

    public void bufferUpdate(Plane plane){
        planeVertex = plane.getPlaneVertex();

        ByteBuffer bb = ByteBuffer.allocateDirect(planeVertex.length * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(planeVertex);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2); // (# of coordinate values * 2 bytes per short)
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * FLOAT_SIZE);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
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
        mColor_u = GLES20.glGetAttribLocation(mProgram, "u_Color");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

    }

    public void draw(float[] vpMatrix){
        GLES20.glUseProgram(mProgram);

        GLES20.glVertexAttribPointer(mPosition, 3, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * FLOAT_SIZE, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPosition);

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glEnableVertexAttribArray(uMVPMatrixHandle);

        GLES20.glVertexAttribPointer(mColor_u, 4, GLES20.GL_FLOAT, false, 16, colorBuffer);
        GLES20.glEnableVertexAttribArray(mColor_u);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glEnableVertexAttribArray(mColor_u);
        GLES20.glEnableVertexAttribArray(uMVPMatrixHandle);

    }
}
