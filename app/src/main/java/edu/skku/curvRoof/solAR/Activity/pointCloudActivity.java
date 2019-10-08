package edu.skku.curvRoof.solAR.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Model.Plane;
import edu.skku.curvRoof.solAR.Renderer.BackgroundRenderer;
import edu.skku.curvRoof.solAR.Renderer.PlaneRenderer;
import edu.skku.curvRoof.solAR.Renderer.PointCloudRenderer;
import edu.skku.curvRoof.solAR.Utils.GpsUtil;

public class pointCloudActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();

    private GLSurfaceView glSurfaceView = null;
    private Session session;

    //AR Core and Viewport change variables
    private boolean mUserRequestedInstall = false;
    private boolean mViewportChanged = false;
    private int mViewportWidth = -1;
    private int mViewportHeight = -1;

    private final int PERMISSION_REQUEST_CODE = 0;

    //mvpMatrix
    private float[] viewMatrix = new float[16];
    private float[] projMatrix = new float[16];
    private float[] vpMatrix = new float[16];

    //Recording gathered points, and pick start point for Region Growing
    private Button pickBtn;
    private Button recordBtn;
    private boolean isRecording = false;
    private boolean isRecorded = false;
    private boolean isPicked = false;
    private boolean pickTouched = false;
    private static int requestStatus = 0;                  // 0 : not requested
                                                    // 1 : requested
                                                    // 2 : plane found
                                                    // 3 : plane found(Toast alarmed)
                                                    // 4 : not found
                                                    // 5 : not found(Toast alarmed)

    Handler mHandler = null; // handler for GPS tracker to toast on MainActivity

    private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET};

    private static final String REQUEST_URL = "http://developers.curvsurf.com/FindSurface/plane"; // Plane searching server address
    private planeFinder myPlaneFinder;
    private PlaneRenderer planeRenderer = new PlaneRenderer();
    private Plane myPlane;
    private boolean normalValid = false;

    private float[] ray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_cloud);

        glSurfaceView = findViewById(R.id.pointCloud_view);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                GpsUtil gpsTracker = new GpsUtil(pointCloudActivity.this);
                Toast.makeText(getApplicationContext(), gpsTracker.getAddress(), Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        t.start();


        recordBtn = (Button)findViewById(R.id.recordBtn);
        pickBtn = (Button)findViewById(R.id.pickBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPicked = false;
                if(isRecording == false){
                    isRecording = true;
                    Toast.makeText(getApplicationContext(), "Start Recording", Toast.LENGTH_SHORT).show();
                }
                else{
                    isRecording = false;
                    pointCloudRenderer.filterHashMap();
                    Toast.makeText(getApplicationContext(), "Stop Recording", Toast.LENGTH_SHORT).show();
                    pointCloudRenderer.cal_gathering();
                    isRecorded = true;
                }

            }
        });

        pickBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(isPicked == false){
                    isPicked = true;
                    pickTouched = true;
                    if(myPlaneFinder != null){
                        if(myPlaneFinder.getStatus() == AsyncTask.Status.FINISHED || myPlaneFinder.getStatus() == AsyncTask.Status.RUNNING){
                            myPlaneFinder.cancel(true);
                            myPlaneFinder = new planeFinder();
                        }
                        myPlaneFinder.execute();
                    }
                }
                else{
                    isPicked = false;
                }
            }
        });


        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN :
                    case MotionEvent.ACTION_MOVE :
                    case MotionEvent.ACTION_UP   :
                        float tx = event.getX();
                        float ty = event.getY();

                        try{
                            ray = screenPointToWorldRay(tx, ty, session.update());
                        }catch (Exception e){
                            Log.d("hit test", e.getMessage());
                        }


                }
                return true;
            }
        });


        myPlaneFinder = new planeFinder();
        mUserRequestedInstall = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for(String permission : permissions){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                    startActivity(intent);
                }
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(session != null){
            glSurfaceView.onPause();
            session.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        for(String permission : REQUIRED_PERMISSSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSSIONS, PERMISSION_REQUEST_CODE);
            }
        }

        if(session == null){
            try{
                switch(ArCoreApk.getInstance().requestInstall(this,!mUserRequestedInstall)){
                    case INSTALL_REQUESTED:
                        mUserRequestedInstall = true;
                        return;
                    case INSTALLED:
                        break;
                }
                session = new Session(this);

                Config config = new Config(session);
                config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
                config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
                config.setFocusMode(Config.FocusMode.AUTO);
                session.configure(config);

            }catch (Exception e){
                return;
            }
        }

        try{
            session.resume();
        }catch (CameraNotAvailableException e){
            e.printStackTrace();
            session = null;
            finish();
        }

        glSurfaceView.onResume();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        try{
            backgroundRenderer.createOnGlThread(this);
            pointCloudRenderer.createGlThread(this);
            planeRenderer.createGlThread(this);

        }catch (IOException e){
            e.getMessage();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        if(session == null){
            return;
        }

        if(mViewportChanged){
            int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
        }

        try{
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            Frame frame = session.update();
            Camera camera = frame.getCamera();

            backgroundRenderer.draw(frame);

            if(pickTouched){
                pointCloudRenderer.pickPoint(camera);
                pickTouched = false;
            }

            if(isPicked){
                if(camera.getTrackingState() == TrackingState.TRACKING) {
                    // Fixed Work -> ARCore
                    camera.getViewMatrix(viewMatrix, 0);
                    camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
                }

                Matrix.multiplyMM(vpMatrix, 0, projMatrix,0,viewMatrix,0);
                if(normalValid == false){
                    pointCloudRenderer.draw_seedPoint(vpMatrix);
                    if(myPlane != null){
                        planeRenderer.bufferUpdate(myPlane);
                        myPlane.checkNormal(camera);
                        normalValid = true;
                    }
                }
                else{
                    planeRenderer.draw(vpMatrix);
                }
            }
            else if(isRecording){
                pointCloudRenderer.update(frame.acquirePointCloud());

                if(camera.getTrackingState() == TrackingState.TRACKING) {
                    // Fixed Work -> ARCore
                    camera.getViewMatrix(viewMatrix, 0);
                    camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
                }

                Matrix.multiplyMM(vpMatrix, 0, projMatrix,0,viewMatrix,0);
                pointCloudRenderer.draw(vpMatrix);
            }
            else if(isRecorded){
                if(camera.getTrackingState() == TrackingState.TRACKING) {
                    // Fixed Work -> ARCore
                    camera.getViewMatrix(viewMatrix, 0);
                    camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
                }

                Matrix.multiplyMM(vpMatrix, 0, projMatrix,0,viewMatrix,0);
                pointCloudRenderer.draw_final(vpMatrix);
            }
        }catch(CameraNotAvailableException e){
            finish();
        }
    }

    public class planeFinder extends AsyncTask<Object, ResponseForm.PlaneParam, ResponseForm.PlaneParam> {
        @Override
        protected ResponseForm.PlaneParam doInBackground(Object[] objects) {
            // Ready Point Cloud
            FloatBuffer points = pointCloudRenderer.getFiltered_pointCloud();

            // Ready Request Form
            RequestForm rf = new RequestForm();

            rf.setPointBufferDescription(points.capacity() / 3, 0, 0); //pointcount, pointstride, pointoffset
            rf.setPointDataDescription(0.02f, 0.05f); //accuracy, meanDistance
            rf.setTargetROI(pointCloudRenderer.getSeedPoint(), 0.1f);//seedIndex,touchRadius
            rf.setAlgorithmParameter(RequestForm.SearchLevel.RADICAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp

            FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);
            requestStatus = 1;
            // Request Find Surface
            try{
                ResponseForm resp = fsr.request(rf, points);
                if(resp != null && resp.isSuccess()) {
                        ResponseForm.PlaneParam param = resp.getParamAsPlane();
                        requestStatus = 2;
                        return param;
                }
                else{
                    requestStatus = 4;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ResponseForm.PlaneParam o) {
            super.onPostExecute(o);
            Log.d("requestStatus", String.valueOf(requestStatus));
            try{
                myPlane = new Plane(o.ll, o.lr, o.ur, o.ul);
            }catch (Exception e){
                Log.d("Plane", e.getMessage());
            }

        }
    }
    float[] screenPointToWorldRay(float xPx, float yPx, Frame frame) {
        float[] points = new float[12];  // {clip query, camera query, camera origin}
        // Set up the clip-space coordinates of our query point( 핸드폰 display 좌표를 local space로 변환)
        // +x is right:
        points[0] = 2.0f * xPx / glSurfaceView.getMeasuredWidth() - 1.0f;
        // +y is up (android UI Y is down):
        points[1] = 1.0f - 2.0f * yPx / glSurfaceView.getMeasuredHeight();
        points[2] = 1.0f; // +z is forwards (remember clip, not camera)
        points[3] = 1.0f; // w (homogenous coordinates)


        float[] matrices = new float[32];  // {proj, inverse proj}
        // If you'll be calling this several times per frame factor out
        // the next two lines to run when Frame.isDisplayRotationChanged().
        frame.getCamera().getProjectionMatrix(matrices, 0, 1.0f, 100.0f);
        Matrix.invertM(matrices, 16, matrices, 0);
        // Transform clip-space point to camera-space.
        // point[4],point[5],point[6],point[7]에 (camera space좌표) = (projection MTX inverse) * (local space좌표)
        Matrix.multiplyMV(points, 4, matrices, 16, points, 0);
        // points[4,5,6] is now a camera-space vector.  Transform to world space to get a point
        // along the ray.
        float[] out = new float[6];



        //터치한 ray의 카메라 좌표 -> world 좌표
        frame.getCamera().getPose().transformPoint(points, 4, out, 3);
        // use points[8,9,10] as a zero vector to get the ray head position in world space.(카메라에서 0,0,0을 world 좌표로 옮김)
        frame.getCamera().getPose().transformPoint(points, 8, out, 0);
        // normalize the direction vector:
        float dx = out[3] - out[0];
        float dy = out[4] - out[1];
        float dz = out[5] - out[2];
        float scale = 1.0f / (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

        // 실제 ray의 형태
        // A(out[0], out[1], out[2]),
        // B(out[0] + out[3], out[1] + out[4], out[2] + out[5])
        out[3] = dx * scale;
        out[4] = dy * scale;
        out[5] = dz * scale;
        return out;
    }


}
