package edu.skku.curvRoof.solAR.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Utils.ShaderUtil;

public class Cube {

    private final FloatBuffer textureBuffer;
    private final int mTextureUniformHandle;
    String TAG = "Cube";

    Context context;
    static final int COORDDS_PER_VERTEX = 3;
    static final int vertexStride = COORDDS_PER_VERTEX *4;
    private static final String VERTEX_SHADER_NAME = "cube.vert";
    private static final String FRAGMENT_SHADER_NAME = "cube.frag";
    private final int vertexCount = vertices.length/COORDDS_PER_VERTEX;
    private int mProgram;

    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mColorHandle;
    private final int mTexCoordHandle;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer colorBuffer;

    static float[] vertices = {
            -(0.05f*1.67f),0.0f, (float) (0.05*1.0),
            0.05f*1.67f,0.0f,(float) (0.05*1.0),  // 오른쪽 아래
            0.05f*1.67f,  0.0032f,(float) (0.05*1.0),
            -0.05f*1.67f, 0.0032f,(float) (0.05*1.0),


            0.05f*1.67f,0.0f,(float) (0.05*1.0),
            0.05f*1.67f,0.0f,(float) -(0.05*1.0),
            0.05f*1.67f, 0.0032f,(float)(-(0.05*1.0)),
            0.05f*1.67f, 0.0032f,(float) (0.05*1.0),


            0.05f*1.67f,0.0f,(float) -(0.05*1.0),
            -0.05f*1.67f,0.0f,(float) -(0.05*1.0),
            -0.05f*1.67f,0.0032f,(float)-(0.05*1.0),
            0.05f*1.67f, 0.0032f,(float)-(0.05*1.0),


            -0.05f*1.67f,0.0f,(float) -(0.05*1.0),
            -(0.05f*1.67f),0.00f, (float) (0.05*1.0),
            -0.05f*1.67f, 0.0032f,(float) (0.05*1.0),
            -0.05f*1.67f,0.0032f,(float)-(0.05*1.0),


            -0.05f*1.67f,0.0f,(float) -(0.05*1.0), // 왼쪽 아래 정점
            (0.05f*1.67f),0.0f, (float) -(0.050*1.0),  // 오른쪽 아// 왼쪽 위
            0.05f*1.67f,0.0f,(float) (0.05*1.0),
            -0.05f*1.67f,0.0f,(float) (0.05*1.0),


            -0.05f*1.67f, 0.0032f,(float) (0.05*1.0),
            0.05f*1.67f, 0.0032f,(float) (0.05*1.0),
            0.05f*1.67f,0.0032f,(float)-(0.05*1.0),
            -0.05f*1.67f, 0.0032f,(float)-(0.05*1.0),
    };

    short[] indices = {
            0, 1, 2, 2, 3, 0,
            4, 5, 7, 5, 6, 7,
            8, 9, 11, 9, 10, 11,
            12, 13, 15, 13, 14, 15,
            16, 17, 19, 17, 18, 19,
            20, 21, 23, 21, 22, 23,
    };

    float [] textures = {

            //front face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,


            // Right face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            // Back face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            // Left face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            // Top face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            // Bottom face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };


    float[] colors = new float[]{
            0.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
    };
    private int m_textureId;
    private int mTextureDataHandle0;
    private int mTextureDataHandle1;
    private int mTextureDataHandle2;
    private int mTextureDataHandle3;
    private int mTextureDataHandle4;
    private int mTextureDataHandle5;

    //float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };


    public Cube(Context context,GLSurfaceView surfaceView){
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length*4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);

        ByteBuffer cbuf = ByteBuffer.allocateDirect(colors.length*4);
        cbuf.order(ByteOrder.nativeOrder());
        colorBuffer = cbuf.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);



        ByteBuffer tbb = ByteBuffer.allocateDirect(textures.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        textureBuffer = tbb.asFloatBuffer();
        textureBuffer.put(textures);
        textureBuffer.position(0);

        //loadTexture(context);

        try{
            int vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER,VERTEX_SHADER_NAME );
            int fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);
            mProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);
        }catch(IOException e) {
            Log.d("TAG", e.getMessage());
        }

        mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
        //mColorHandle = GLES20.glGetAttribLocation(mProgram,"aColor");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram,"a_texCoord");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram,"s_texture");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        mTextureDataHandle0=loadTexture(surfaceView,R.drawable.bsquare);
        mTextureDataHandle1 = loadTexture(surfaceView, R.drawable.bsquare);
        mTextureDataHandle2 = loadTexture(surfaceView,R.drawable.bsquare);
        mTextureDataHandle3 = loadTexture(surfaceView, R.drawable.bsquare);
        mTextureDataHandle4 = loadTexture(surfaceView,R.drawable.bsquare);
        mTextureDataHandle5 = loadTexture(surfaceView,R.drawable.msquare);

        //GLES20.glUniform1i(mTextureUniformHandle,0);
    }

    private static int loadTexture(GLSurfaceView mActivityContext2, final int resourceId){
        int[] texture = new int[1];


        GLES20.glGenTextures(1,texture,0);

        if (texture[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(mActivityContext2.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (texture[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return texture[0];
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
       /* GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP,textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);


        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,0, BitmapFactory.decodeResource(con.getResources(),R.drawable.cloud),0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,0,BitmapFactory.decodeResource(con.getResources(),R.drawable.cloud),0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,0,BitmapFactory.decodeResource(con.getResources(),R.drawable.m1),0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,0,BitmapFactory.decodeResource(con.getResources(),R.drawable.m1),0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,0,BitmapFactory.decodeResource(con.getResources(),R.drawable.m1),0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z,0,BitmapFactory.decodeResource(con.getResources(),R.drawable.m1),0);

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP,0);
        m_textureId =textures[0];*/
    }

    public void draw(float[] mvpMatrix,int i){

        Log.d("rrr", "RRR");
        GLES20.glUseProgram(mProgram);

        // vertex 설정
        vertexBuffer.position(12*i);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDDS_PER_VERTEX, GLES20.GL_FLOAT,false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // color 설정
        //GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 16, colorBuffer);
        //GLES20.glEnableVertexAttribArray(mColorHandle);

        textureBuffer.position(8*i);
        GLES20.glVertexAttribPointer(mTexCoordHandle,2,GLES20.GL_FLOAT,false,0,textureBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);

        if(i==0){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureDataHandle0);}
        if(i==1){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);

            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle1);
        }
        if(i==2){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);

            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle2);
        }
        if(i==3){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);

            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle3);
        }
        if(i==4){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE4);

            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle4);
        }
        if(i==5){
            GLES20.glActiveTexture(GLES20.GL_TEXTURE5);

            // Bind the texture to this unit.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle5);
        }
        GLES20.glUniform1i(mTextureUniformHandle,i);

        //loadTexture(c);

        //GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP,m_textureId);
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP,m_textureId);
        //GLES20.glEnableVertexAttribArray(mMipHandle);
        //GLES20.glUniform1i(mTextureUniformHandle,0);

        Log.d("HHH", "QQQ");
        // mvpMatrix 설정
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle,1,false, mvpMatrix,0);
        GLES20.glEnableVertexAttribArray(mMVPMatrixHandle);

        // 그리기
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        //disable
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
        GLES20.glDisableVertexAttribArray(mTextureUniformHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
    }

}
