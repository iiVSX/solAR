package edu.skku.curvRoof.solAR.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.skku.curvRoof.solAR.R;

public class Cube {
    private static int [] textureIds;

    //float[] vertices = new float[72];

    Bitmap[]bitmap = new Bitmap[6];

    public Bitmap[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(Context context) {
        Bitmap [] bmap = new Bitmap[6];

        bmap[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);

        bmap[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);

        bmap[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);

        bmap[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);

        bmap[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m2);

        bmap[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.m1);



        this.bitmap = bmap;
    }

    public static int[] getTextureIds() {
        return textureIds;
    }

    public static void setTextureIds(int[] textureIds) {
        Cube.textureIds = textureIds;
    }

    float [] vertices = new float[]{


            // 앞면
            -(0.5f*1.67f),0.0f, (float) (0.5*1.0),
            0.5f*1.67f,0.0f,(float) (0.5*1.0),  // 오른쪽 아래

            -0.5f*1.67f, 0.032f,(float) (0.5*1.0),
            0.5f*1.67f,  0.032f,(float) (0.5*1.0),   // 오른쪽 위

            // 오른쪽 면

            0.5f*1.67f,0.0f,(float) (0.5*1.0),
            0.5f*1.67f,0.0f,(float) -(0.5*1.0),

            0.5f*1.67f, 0.032f,(float) (0.5*1.0),
            0.5f*1.67f, 0.032f,(float)(-(0.5*1.0)),// 오른쪽 위

            // 뒷면

            0.5f*1.67f,0.0f,(float) -(0.5*1.0),
            -0.5f*1.67f,0.0f,(float) -(0.5*1.0),

            0.5f*1.67f, 0.032f,(float)-(0.5*1.0),
            -0.5f*1.67f,0.032f,(float)-(0.5*1.0),

            // 왼쪽면

            -0.5f*1.67f,0.0f,(float) -(0.5*1.0),

            -(0.5f*1.67f),0.0f, (float) (0.5*1.0),

            -0.5f*1.67f,0.032f,(float)-(0.5*1.0),

            -0.5f*1.67f, 0.032f,(float) (0.5*1.0),

            // 아래쪽 면

            -0.5f*1.67f,0.0f,(float) -(0.5*1.0), // 왼쪽 아래 정점

            (0.5f*1.67f),0.0f, (float) -(0.50*1.0),  // 오른쪽 아래

            -0.5f*1.67f,0.0f,(float) (0.5*1.0),  // 왼쪽 위

            0.5f*1.67f,0.0f,(float) (0.5*1.0),

            // 위쪽면
            -0.5f*1.67f, 0.032f,(float) (0.5*1.0),
            0.5f*1.67f, 0.032f,(float) (0.5*1.0),
            -0.5f*1.67f, .032f,(float)-(0.5*1.0),
            0.5f*1.67f,0.032f,(float)-(0.5*1.0),

    };

    short [] indices = new short[]{

            //정점배열의 정점 인덱스를 이용하여 각 면마다 2개의 3각형(CCW)을 구성한다

            0,1,3, 0,3,2,           //앞면을 구성하는 2개의 3각형

            4,5,7, 4,7,6,           //오른쪽면

            8,9,11, 8,11,10,        //...

            12,13,15, 12,15,14,

            16,17,19, 16,19,18,

            20,21,23, 20,23,22,

    };

    private float [] textures = new float[]{

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

    public float[] getTextures() {
        return textures;
    }

    public void setTextures(float[] textures) {
        this.textures = textures;
    }

    public float[] getVertices() {
        return vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    public short[] getIndices() {
        return indices;
    }

    public void setIndices(short[] indices) {
        this.indices = indices;
    }


}
