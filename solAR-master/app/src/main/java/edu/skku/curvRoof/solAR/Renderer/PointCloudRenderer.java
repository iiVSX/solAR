package edu.skku.curvRoof.solAR.Renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.util.Log;

import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import edu.skku.curvRoof.solAR.Model.Point;
import edu.skku.curvRoof.solAR.Utils.ShaderUtil;
public class PointCloudRenderer {
    private static final String TAG = PointCloudRenderer.class.getSimpleName();

    //declaring shader
    private static final String VERTEX_SHADER_NAME = "point_cloud.vert";
    private static final String FRAGMENT_SHADER_NAME = "point_cloud.frag";

    private int vertexShader;
    private int fragmentShader;

    private int mProgram;

    //position of variables in shader
    private int mPosition;
    private int mColor_u;
    private int mColor_a;
    private int uMVPMatrixHandle;
    private int mSize;
    private int bUseSolidColor; // when bUseSolidColor is 1, use Uniform, else use Attribute

    //color : r,g,b,a | vertex : x,y,z,confidence
    private static final int COORDS_PER_VERTEX = 4;
    private static final int FLOAT_SIZE = 4;

    //GL_ARRAY_BUFFER variables
    private int vbo;
    private int vboSize;

    //point cloud buffers
    private FloatBuffer pointCloud;
    private IntBuffer pointIdBuffer;
    private FloatBuffer filtered_pointCloud;

    //hash Map for storing vertexes by ID
    private HashMap<Integer, ArrayList<Point>> fullPointHashMap;
    private HashMap<Integer, Point> filteredPointHashMap;

    //Gathered PointCloud Buffer
    private FloatBuffer gathered_pointcloud_buffer;
    private FloatBuffer gathered_color_buffer;

    //Pick seed Point
    public float[] seedPoint;
    private FloatBuffer seedBuffer;
    private int seedPointID;

    public void createGlThread(Context context) throws IOException {

        //create buffer and bind with GL_ARRAY_BUFFER
        int[] buffers = new int[1];

        GLES20.glGenBuffers(1, buffers, 0);
        vbo = buffers[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        vboSize = 1000 * 16;
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vboSize, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //loading shader
        vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);

        //bind shader's variable position
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

        mPosition = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mColor_a = GLES20.glGetAttribLocation(mProgram, "a_Color");
        mColor_u = GLES20.glGetUniformLocation(mProgram, "u_Color");
        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_ModelViewProjection");
        mSize = GLES20.glGetUniformLocation(mProgram, "u_PointSize");
        bUseSolidColor = GLES20.glGetUniformLocation(mProgram, "bUseSolidColor");

        //create hash map
        fullPointHashMap = new HashMap<>();
        filteredPointHashMap = new HashMap<>();
    }

    public void update(PointCloud cloud, boolean recording) {   //  recording - false : point들을 표시는 하지만 해쉬에 저장하지는 않음
                                                                //              true : point들을 표시하면서 해쉬에 저장
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        //get point cloud's vertexes and their IDs and store in buffers
        pointCloud = cloud.getPoints();
        pointIdBuffer = cloud.getIds();

        // If the VBO is not large enough to fit the new point cloud, resize it.
        if (pointCloud.remaining() * 4 > vboSize) {
            while (pointCloud.remaining() * 4 > vboSize) {
                vboSize *= 2;
            }
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vboSize, null, GLES20.GL_DYNAMIC_DRAW);
        }

        //send point cloud vertexes to shader
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, 0, pointCloud.remaining() * 4, pointCloud);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //store point cloud vertexes in hash map
        if(recording){
            for (int i = 0; i < pointCloud.remaining() / 4; i++) {
                Point temp = new Point(pointCloud.get(i * 4), pointCloud.get(i * 4 + 1), pointCloud.get(i * 4 + 2), pointCloud.get(i * 4 + 3));

                //if hash map's IDth element doesn't exist, create array list
                if (!fullPointHashMap.containsKey(pointIdBuffer.get(i))) {
                    ArrayList<Point> list = new ArrayList<>();
                    list.add(temp);
                    fullPointHashMap.put(pointIdBuffer.get(i), list);
                } else {
                    fullPointHashMap.get(pointIdBuffer.get(i)).add(temp);
                }
            }
        }
    }

    public void draw_intial(float[] vpMatrix){
        GLES20.glUseProgram(mProgram);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(mPosition);
        GLES20.glVertexAttribPointer(mPosition, FLOAT_SIZE, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * FLOAT_SIZE, 0);

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, vpMatrix, 0);

        GLES20.glUniform1f(mSize, 15.0f);
        GLES20.glUniform1i(bUseSolidColor,1);
        GLES20.glUniform4f(mColor_u, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, pointCloud.remaining() / 4);

        GLES20.glDisableVertexAttribArray(mPosition);

    }

    public void draw(float[] vpMatrix) {
        GLES20.glUseProgram(mProgram);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);

        //change color by vertexes' confidence
        float[] color = new float[pointCloud.remaining()];
        for (int i = 3; i < pointCloud.remaining(); i = i + 4) {
            float conf = pointCloud.get(i);

            if (conf <= 0.33f) {
                color[i - 3] = 1.0f - conf;
                color[i - 2] = 0.0f;
                color[i - 1] = 0.0f;
                color[i] = 1.0f;
            } else if (conf <= 0.66f) {
                color[i - 3] = 0.0f;
                color[i - 2] = 1.0f - conf;
                color[i - 1] = 0.0f;
                color[i] = 1.0f;
            } else {
                color[i - 3] = 0.0f;
                color[i - 2] = 0.0f;
                color[i - 1] = conf;
                color[i] = 1.0f;
            }
        }

        //create color buffer and send to shader
        ByteBuffer bb = ByteBuffer.allocateDirect(FLOAT_SIZE * color.length);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer colorBuffer = bb.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        GLES20.glEnableVertexAttribArray(mPosition);
        GLES20.glEnableVertexAttribArray(mColor_a);

        GLES20.glVertexAttribPointer(mPosition, FLOAT_SIZE, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * FLOAT_SIZE, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glUniform1f(mSize, 15.0f);
        GLES20.glUniform1i(bUseSolidColor,0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glVertexAttribPointer(mColor_a, FLOAT_SIZE, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * FLOAT_SIZE, colorBuffer);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, pointCloud.remaining() / 4);

        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glDisableVertexAttribArray(mColor_a);
    }

    public void filterHashMap() {
        ArrayList<Float> listFinalPoints = new ArrayList<Float>();   //  ArrayList<Float> for final points (order is [x,y,z,conf])
        Iterator<Integer> keys = fullPointHashMap.keySet().iterator();
        while (keys.hasNext()) {
            int index = keys.next();
            ArrayList<Point> list = fullPointHashMap.get(index);
            float mean_x = 0.f, mean_y = 0.f, mean_z = 0.f;
            for (Point tmp : list) {
                mean_x += tmp.getX();
                mean_y += tmp.getY();
                mean_z += tmp.getZ();
            }
            mean_z /= list.size();
            mean_x /= list.size();
            mean_y /= list.size();

            if(list.size() < 5){
                listFinalPoints.add(mean_x);
                listFinalPoints.add(mean_y);
                listFinalPoints.add(mean_z);
                listFinalPoints.add(1.0f);
                filteredPointHashMap.put(index, new Point(mean_x, mean_y, mean_z, 1.0f));
                continue;   // no more calculation
            }

            // calculate variance
            float distance_mean = 0.f;
            float variance = 0.f;
            for (Point tmp : list) {
                float temp = (float)(Math.pow((tmp.getX() - mean_x), 2.0) + Math.pow((tmp.getY() - mean_y), 2.0) + Math.pow((tmp.getZ() - mean_z), 2.0));
                variance += temp;
                distance_mean += Math.sqrt(temp);
            }
            distance_mean /= list.size();
            variance = (variance / list.size()) - distance_mean*distance_mean;

            // when variance is 0
            if(variance == 0){
                int list_size = list.size();
                for(int i =1; i< list_size ;i++){
                    list.remove(1);
                }
                mean_x = list.get(0).getX();
                mean_y = list.get(0).getY();
                mean_z = list.get(0).getZ();

                listFinalPoints.add(mean_x);
                listFinalPoints.add(mean_y);
                listFinalPoints.add(mean_z);
                listFinalPoints.add(1.0f);

                filteredPointHashMap.put(index, new Point(mean_x, mean_y, mean_z, 1.0f));

                continue; // no more calculation
            }

            // else
            else {
                Iterator<Point> iter = list.iterator();
                while (iter.hasNext()) {
                    Point temp_point = iter.next();
                    float temp = (float)(Math.pow((temp_point.getX() - mean_x), 2) + Math.pow((temp_point.getY() - mean_y), 2) + Math.pow((temp_point.getZ() - mean_z), 2));
                    float z_score = (float)(Math.abs(temp - distance_mean) / Math.sqrt(variance));
                    if (z_score >= 1.2f) {
                        iter.remove();
                    }
                }

                if(list.size() == 0) continue;

                mean_x = 0.f;
                mean_y = 0.f;
                mean_z = 0.f;
                for (Point tmp : list) {
                    mean_x += tmp.getX();
                    mean_y += tmp.getY();
                    mean_z += tmp.getZ();
                }
                mean_z /= list.size();
                mean_x /= list.size();
                mean_y /= list.size();

                listFinalPoints.add(mean_x);
                listFinalPoints.add(mean_y);
                listFinalPoints.add(mean_z);
                listFinalPoints.add(1.0f);

                filteredPointHashMap.put(index, new Point(mean_x, mean_y, mean_z, 1.0f));
            }
        }

        // make Floatbuffer with filtered points
        ByteBuffer bb = ByteBuffer.allocateDirect(listFinalPoints.size() * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        filtered_pointCloud = bb.asFloatBuffer();

        // convert List to array(primitive)
        float[] tempArray = new float[listFinalPoints.size()];
        for(int i = 0 ; i < tempArray.length ; i++){
            tempArray[i] = listFinalPoints.get(i);
        }

        filtered_pointCloud.put(tempArray);
        filtered_pointCloud.position(0);
    }

    public void draw_final(float[] vpMatrix){
        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mPosition);

        GLES20.glVertexAttribPointer(mPosition, FLOAT_SIZE, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * FLOAT_SIZE, filtered_pointCloud);
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glUniform1f(mSize, 15.0f);
        GLES20.glUniform1i(bUseSolidColor,1);

        GLES20.glUniform4f(mColor_u, 0.0f, 0.0f, 1.0f, 1.0f);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, filtered_pointCloud.remaining()/4);
        GLES20.glDisableVertexAttribArray(mPosition);

    }

    public void cal_gathering() {
        int gathered_points_num = 0;
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        for (Integer key : fullPointHashMap.keySet()) {
            ArrayList<Point> pointsOfId = fullPointHashMap.get(key);
            gathered_points_num += pointsOfId.size();
        }

        float[] gathered_pointcloud = new float[gathered_points_num * 4];
        float[] gathered_color = new float[gathered_points_num * 4];

        int count = 0;

        for (Integer key : fullPointHashMap.keySet()) {
            ArrayList<Point> pointsOfId = fullPointHashMap.get(key);
            Random colorDecider = new Random();
            float R = colorDecider.nextFloat();
            float G = colorDecider.nextFloat();
            float B = colorDecider.nextFloat();
            for (Point eachPoint : pointsOfId) {
                gathered_pointcloud[count] = eachPoint.getX();
                gathered_pointcloud[count + 1] = eachPoint.getY();
                gathered_pointcloud[count + 2] = eachPoint.getZ();
                gathered_pointcloud[count + 3] = eachPoint.getConf();

                gathered_color[count] = R;
                gathered_color[count + 1] = G;
                gathered_color[count + 2] = B;
                gathered_color[count + 3] = 1.0f;
                count += 4;
            }
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(gathered_points_num * 16);
        bb.order(ByteOrder.nativeOrder());
        gathered_pointcloud_buffer = bb.asFloatBuffer();
        gathered_pointcloud_buffer.put(gathered_pointcloud);
        gathered_pointcloud_buffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(gathered_points_num * 16);
        cbb.order(ByteOrder.nativeOrder());
        gathered_color_buffer = cbb.asFloatBuffer();
        gathered_color_buffer.put(gathered_color);
        gathered_color_buffer.position(0);
    }

    public void pickPoint(Camera camera){
        float thresholdDistance = 0.01f; // 10cm = 0.1m * 0.1m = 0.01f
        seedPoint = new float[]{0, 0, 0, Float.MAX_VALUE};

        Pose CameraPose = camera.getPose();
        float[] camera_position =  CameraPose.getTranslation();
        float[] camera_zdir = CameraPose.getZAxis();

        for(Integer index : filteredPointHashMap.keySet()){
            Point tmp = filteredPointHashMap.get(index);
            Point product = new Point(tmp.getX() - camera_position[0], tmp.getY() - camera_position[1], tmp.getZ() - camera_position[2], 1.0f);

            float distanceSq = (float)(Math.pow(product.getX(),2) + Math.pow(product.getY(),2) + Math.pow(product.getZ(),2));// length between camera and point
            float innerProduct = camera_zdir[0] * product.getX() + camera_zdir[1] * product.getY() + camera_zdir[2] * product.getZ();
            distanceSq = distanceSq - (innerProduct * innerProduct);  //c^2 - a^2 = b^2

            // determine candidate points
            if(distanceSq < thresholdDistance && innerProduct < seedPoint[3]){
                seedPoint[0] = tmp.getX();
                seedPoint[1] = tmp.getY();
                seedPoint[2] = tmp.getZ();
                seedPointID = index;
            }
        }
        Log.d("pickSeed", seedPoint.toString());
    }

    public void draw_seedPoint(float[] vpMatrix){
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPosition);

        ByteBuffer bb = ByteBuffer.allocateDirect(COORDS_PER_VERTEX * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        seedBuffer = bb.asFloatBuffer();
        seedBuffer.put(seedPoint);
        seedBuffer.position(0);

        GLES20.glVertexAttribPointer(mPosition, FLOAT_SIZE, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * FLOAT_SIZE, seedBuffer);
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glUniform1f(mSize, 30.0f);
        GLES20.glUniform1i(bUseSolidColor,1);

        GLES20.glUniform4f(mColor_u, 1.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, seedBuffer.remaining()/4);
        GLES20.glDisableVertexAttribArray(mPosition);
    }

    public void draw_APoint(float[] vpMatrix, float[] point){
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(mPosition);

        ByteBuffer bb = ByteBuffer.allocateDirect(COORDS_PER_VERTEX * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(point);
        vertexBuffer.position(0);

        GLES20.glVertexAttribPointer(mPosition, FLOAT_SIZE, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * FLOAT_SIZE, vertexBuffer);
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, vpMatrix, 0);
        GLES20.glUniform1f(mSize, 30.0f);
        GLES20.glUniform1i(bUseSolidColor,1);

        GLES20.glUniform4f(mColor_u, 1.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexBuffer.remaining()/4);
        GLES20.glDisableVertexAttribArray(mPosition);
    }

    public FloatBuffer getFiltered_pointCloud(){
        return filtered_pointCloud;
    }

    public int getSeedPoint(){
        return seedPointID;
    }
}
