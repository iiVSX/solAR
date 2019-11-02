package edu.skku.curvRoof.solAR.Renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import edu.skku.curvRoof.solAR.Model.Cube;
import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Utils.ShaderUtil;

public class CubeRenderer {

    private static final String VERTEX_SHADER_NAME = "cube.vert";
    private static final String FRAGMENT_SHADER_NAME = "cube.frag";
    private int mProgram;

    private int mPosition;
    private int uvHandle;
    private int uMVPMatrixHandle;

    private int vertexShader;
    private int fragmentShader;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer uvBuffer;

    private int[] textures = new int[1];

    private float[] cubeVertex;
    short[] drawOrder = new short[]{

            //정점배열의 정점 인덱스를 이용하여 각 면마다 2개의 3각형(CCW)을 구성한다

            0,1,3, 0,3,2,           //앞면을 구성하는 2개의 3각형

            4,5,7, 4,7,6,           //오른쪽면

            8,9,11, 8,11,10,        //...

            12,13,15, 12,15,14,

            16,17,19, 16,19,18,

            20,21,23, 20,23,22,

    };;
    private float [] UV = new float[]{

            //6개의 면에 매핑될 텍스쳐 좌표 24개를  선언한다

            0.0f, 1.0f,

            1.0f, 1.0f,

            0.0f, 0.0f,

            1.0f, 0.0f,



            0.0f, 1.0f,

            1.0f, 1.0f,

            0.0f, 0.0f,

            1.0f, 0.0f,



            0.0f, 1.0f,

            1.0f, 1.0f,

            0.0f, 0.0f,

            1.0f, 0.0f,



            0.0f, 1.0f,

            1.0f, 1.0f,

            0.0f, 0.0f,

            1.0f, 0.0f,



            0.0f, 1.0f,

            1.0f, 1.0f,

            0.0f, 0.0f,

            1.0f, 0.0f,



            0.0f, 1.0f,

            1.0f, 1.0f,

            0.0f, 0.0f,

            1.0f, 0.0f,



    };
    private Bitmap[] bitmap = new Bitmap[6];

    public void createGlThread(Context context) throws IOException {

        vertexShader = ShaderUtil.loadGLShader("Cube", context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        fragmentShader = ShaderUtil.loadGLShader("Cube", context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);

        //bind shader's variable position
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

        mPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        uvHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");

        bitmap[0] = BitmapFactory.decodeResource(context.getResources(),R.drawable.m2);
        bitmap[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);
        bitmap[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);
        bitmap[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);
        bitmap[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);
        bitmap[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m1);

    }


    public void bufferUpdate(Cube cube){

        cubeVertex = cube.getVertices();

        ByteBuffer vbb = ByteBuffer.allocateDirect(cubeVertex.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(cubeVertex);
        vertexBuffer.position(0);


        ByteBuffer ibb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(drawOrder);
        indexBuffer.position(0);


        ByteBuffer tbb = ByteBuffer.allocateDirect(cubeVertex.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        uvBuffer = tbb.asFloatBuffer();
        uvBuffer.put(UV);
        uvBuffer.position(0);

    }

    public void draw(float[] vpMatrix) {

        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPosition);
        GLES20.glVertexAttribPointer(mPosition, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);

        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, vpMatrix, 0);

        GLES20.glGenTextures(1, textures, 0);
        bindTexture(textures[0]);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_BYTE, indexBuffer);

        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glDisableVertexAttribArray(uvHandle);

    }


    protected void bindTexture(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, bitmap[0], 0 );
        GLUtils.texImage2D( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, bitmap[1], 0 );
        GLUtils.texImage2D( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, bitmap[2], 0 );
        GLUtils.texImage2D( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, bitmap[3], 0 );
        GLUtils.texImage2D( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, bitmap[4], 0 );
        GLUtils.texImage2D( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, bitmap[5], 0 );

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    }

}