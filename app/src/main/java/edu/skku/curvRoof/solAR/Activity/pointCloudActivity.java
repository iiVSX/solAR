package edu.skku.curvRoof.solAR.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.ReqForm;
import com.curvsurf.fsweb.RespForm;
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
import edu.skku.curvRoof.solAR.Renderer.BackgroundRenderer;
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

    Handler mHandler = null; // handler for GPS tracker to toast on MainActivity

    private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET};

    private static final String REQUEST_URL = "http://192.168.123.50:8080/FindSurfaceWeb/ReqFS.do"; // Plane searching server address
    private planeFinder myPlaneFinder;
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

        mHandler = new Handler();

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
                        myPlaneFinder.execute();
                    }
                }
                else{
                    isPicked = false;
                }
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
                pointCloudRenderer.draw_seedPoint(vpMatrix);
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

    public class planeFinder extends AsyncTask<Object, RespForm.PlaneParam, RespForm.PlaneParam> {
        @Override
        protected RespForm.PlaneParam doInBackground(Object[] objects) {
            // Ready Point Cloud
            FloatBuffer points = pointCloudRenderer.getFiltered_pointCloud();

            // Ready Request Form
            ReqForm rf = new ReqForm();

            rf.pointCount  = points.capacity() / 3;
            rf.pointStride = 0;

            rf.seedIndex = pointCloudRenderer.getSeedPoint();
            rf.accuracy  = 0.02f;
            rf.meanDist  = 0.05f;
            rf.touchR    = 0.1f;

            rf.findType  = 1; // 1 - Plane, 2 - Sphere, 3 - Cylinder, 4 - Cone, 5 - Torus
            rf.radExp    = 5;
            rf.latExt    = 7;
            rf.option    = 0;

            FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);

            // Request Find Surface
            try{
                RespForm resp = fsr.request(rf, points);
                if(resp != null) {
                    if( resp.fsResult == 1 ) { // fsResult: 0 - Not Found, 1 - Plane, 2 - Sphere, 3 - Cylinder, 4 - Cone, 5 - Torus
                        RespForm.PlaneParam param = resp.getParamAsPlane();
                        return param;
                    }
                    else{

                    }
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
        protected void onPostExecute(RespForm.PlaneParam o) {
            super.onPostExecute(o);
        }
    }
}
