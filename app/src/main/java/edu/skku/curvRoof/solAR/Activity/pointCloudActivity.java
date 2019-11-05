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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

import edu.skku.curvRoof.solAR.Model.Cube;
import edu.skku.curvRoof.solAR.Model.Trial;
import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Model.Plane;
import edu.skku.curvRoof.solAR.Renderer.BackgroundRenderer;
import edu.skku.curvRoof.solAR.Renderer.LineRender;
import edu.skku.curvRoof.solAR.Renderer.PlaneRenderer;
import edu.skku.curvRoof.solAR.Renderer.PointCloudRenderer;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

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
    private Button nextBtn;
    private Button recordBtn;
    private Button backBtn;
    private int renderingStage = 0;
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

    private float[] ray = null;
    private int holedPoint = 0;
    Frame frame;
    LineRender lineRenderer = new LineRender();
    LineRender normalLineRenderer = new LineRender();
    private float[] pop;

    //Cube
    Cube cube;
    double direction;
    double angle;
    int m,n;

    //tmp


    private User user;
    private Trial trial;
    private LinearLayout dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_cloud_land);

        user = (User)getIntent().getSerializableExtra("user");
        trial = (Trial)getIntent().getSerializableExtra("trial");

        glSurfaceView = findViewById(R.id.pointCloud_view);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // dashboard
        dashboard = findViewById(R.id.dashboard);
        //tmp
        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(renderingStage == 4){
                    renderingStage = 5;
                    backBtn.setVisibility(View.VISIBLE);
                    dashboard.setVisibility(View.VISIBLE);
                }
                else if(renderingStage == 5){
                    // next activity(result_activity)
                    Intent intentmypage = new Intent(pointCloudActivity.this, resultActivity.class);
                    trial.setAngle(angle);
                    trial.setAzimuth(direction);
                    trial.setArea_height((double)m);
                    trial.setArea_height((double)n);
                    intentmypage.putExtra("trial", trial);
                    startActivity(intentmypage);

                }
            }
        });
        backBtn = (Button)findViewById(R.id.BackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(renderingStage == 5){
                    renderingStage = 4;
                    backBtn.setVisibility(View.INVISIBLE);
                    dashboard.setVisibility(View.INVISIBLE);
                }
            }
        });

        Toast.makeText(getApplicationContext(), "오른쪽의 흰색 버튼을 눌러 촬영을 시작하세요", Toast.LENGTH_SHORT).show();

        //
        recordBtn = (Button)findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(renderingStage == 1){
                    renderingStage = 0;
                    pointCloudRenderer.filterHashMap();
                    Toast.makeText(getApplicationContext(), "인식하려는 면적을 터치하세요", Toast.LENGTH_SHORT).show();
                    renderingStage = 2;
                }
                else if(renderingStage == 0){
                    renderingStage = 1;
                    Toast.makeText(getApplicationContext(), "움직이면서 면적을 촬영하세요", Toast.LENGTH_SHORT).show();
                }

            }
        });

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {		// pointCloudActivity (onCreate)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float tx = event.getX();
                float ty = event.getY();
                // ray 생성
                ray = screenPointToWorldRay(tx, ty, frame);

                if(renderingStage == 2){
                    renderingStage = 3;
                    if(myPlaneFinder != null){
                        if(myPlaneFinder.getStatus() == AsyncTask.Status.FINISHED || myPlaneFinder.getStatus() == AsyncTask.Status.RUNNING){
                            myPlaneFinder.cancel(true);
                            myPlaneFinder = new planeFinder();
                        }
                        myPlaneFinder.execute();
                    }
                    recordBtn.setVisibility(View.GONE);
                    nextBtn.setVisibility(View.VISIBLE);

                }

                else if(renderingStage == 4){
                    // obj 움직이기
                    if(myPlane != null){            // 1:ll, 2:lr, 3:ur, 4:ul
                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN :
                                Log.d("moveP",
                                        "\nll : "+ myPlane.getLl()[0]+", "+ myPlane.getLl()[1]+", "+ myPlane.getLl()[2]+"\n"+
                                                "lr : "+ myPlane.getLr()[0]+", "+ myPlane.getLr()[1]+", "+ myPlane.getLr()[2]+"\n"+
                                                "ul : "+ myPlane.getUl()[0]+", "+ myPlane.getUl()[1]+", "+ myPlane.getUl()[2]+"\n"+
                                                "ur : "+ myPlane.getUr()[0]+", "+ myPlane.getUr()[1]+", "+ myPlane.getUr()[2]+"\n");
                                if(myPlane != null) {
                                    holedPoint = myPlane.hitPoint(ray);
                                }
                                break;
                            case MotionEvent.ACTION_MOVE :
                                if(myPlane != null) {
                                    pop = myPlane.rayOnPlane(ray);  // point on Plane
                                    Log.d("moveP", "\n" + holedPoint +" : "+ pop[0] + "/ " + pop[1] + "/ " + pop[2] );
                                    if(holedPoint == 1) myPlane.setLl(pop);
                                    else if(holedPoint == 2) myPlane.setLr(pop);
                                    else if(holedPoint == 3) myPlane.setUr(pop);
                                    else if(holedPoint == 4) myPlane.setUl(pop);
                                    planeRenderer.bufferUpdate(myPlane);
                                }

                                break;
                            case MotionEvent.ACTION_UP   :
                                holedPoint = 0;
                        }
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
            lineRenderer.createGlThread(this);
            normalLineRenderer.createGlThread(this);
            cube = new Cube(this, glSurfaceView);

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
            // 항상 카메라 화면 렌더링
            session.setCameraTextureName(backgroundRenderer.getTextureId());
            frame = session.update();
            Camera camera = frame.getCamera();
            backgroundRenderer.draw(frame);

            // arcore mvp matrix 계산
            if(camera.getTrackingState() == TrackingState.TRACKING) {
                // Fixed Work -> ARCore
                camera.getViewMatrix(viewMatrix, 0);
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
            }
            Matrix.multiplyMM(vpMatrix, 0, projMatrix,0,viewMatrix,0);

            switch (renderingStage){
                case 1: // recording point Cloud
                    pointCloudRenderer.update(frame.acquirePointCloud());
                    pointCloudRenderer.draw(vpMatrix);
                    break;

                case 2: // rendering recorded point cloud
                    if(pointCloudRenderer.getFiltered_pointCloud() != null){
                        pointCloudRenderer.draw_final(vpMatrix);
                    }

                    break;

                case 3: // pickPoint
                    pointCloudRenderer.pickPoint(camera);
                    renderingStage = 4;
                    break;

                case 4: // plane이 정상적으로 뽑혔으면 PlaneRenderer, 그게 아니면 PointCloud에서 seed point draw
                    if(normalValid == false){
                        pointCloudRenderer.draw_seedPoint(vpMatrix);
                        if(myPlane != null){
                            planeRenderer.bufferUpdate(myPlane);
                            normalValid = true;
                        }
                    }
                    else{
                        planeRenderer.draw(vpMatrix);
                    }
                    break;

                case 5:         // cube 렌더링
                    m =3;
                    n=2;
                    angle=30.0f;
                    direction = 45.0f;
                    float[] change = myPlane.getLl();
                    float[] x = new float[6];
                    float[] y = new float[6];
                    float[] z = new float[6];
                    float[] modelMatrix = new float[16];
                    float[] mvpMatrix = new float[16];

                    for(int i = 0;i<n;i++){
                        for(int j = 0;j<m;j++){
                            Matrix.setIdentityM(modelMatrix,0);

                            Matrix.rotateM(modelMatrix,0,(float)direction,0,1,0);
                            Matrix.translateM(modelMatrix,0,change[0],change[1],change[2]);

                            Matrix.translateM(modelMatrix,0,(float)(j+0.5)*0.167f,(float)((i+0.5)*0.1f*sin(toRadians(angle))),(float)(-(i+0.5)*0.1f*cos(toRadians(angle))));

                            Matrix.rotateM(modelMatrix,0,(float)angle,1,0,0);
                            Matrix.multiplyMM(mvpMatrix,0,vpMatrix,0,modelMatrix,0);
                            cube.draw(mvpMatrix, 0);
                            cube.draw(mvpMatrix, 1);
                            cube.draw(mvpMatrix, 2);
                            cube.draw(mvpMatrix, 3);
                            cube.draw(mvpMatrix, 4);
                            cube.draw(mvpMatrix, 5);
                        }
                    }

                    break;
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
                myPlane = new Plane(o.ll, o.lr, o.ur, o.ul,frame.getCamera());
            }catch (Exception e){
                Log.d("Plane", e.getMessage());
            }
            if(myPlane == null){
                float[] seed = pointCloudRenderer.seedPoint;
                float[] ll = {seed[0]-0.3f,seed[1],seed[2]};
                float[] lr = {seed[0],seed[1],seed[2]-0.3f};
                float[] ur = {seed[0]+0.3f,seed[1],seed[2]};
                float[] ul = {seed[0],seed[1],seed[2]+0.3f};
                myPlane = new Plane(ll,lr,ur,ul,frame.getCamera());
            }
        }
    }
    float[] screenPointToWorldRay(float xPx, float yPx, Frame frame) {		// pointCloudActivity
        // ray[0~2] : camera pose
        // ray[3~5] : Unit vector of ray
        float[] ray_clip = new float[4];
        ray_clip[0] = 2.0f * xPx / glSurfaceView.getMeasuredWidth() - 1.0f;
        // +y is up (android UI Y is down):
        ray_clip[1] = 1.0f - 2.0f * yPx / glSurfaceView.getMeasuredHeight();
        ray_clip[2] = -1.0f; // +z is forwards (remember clip, not camera)
        ray_clip[3] = 1.0f; // w (homogenous coordinates)

        float[] ProMatrices = new float[32];  // {proj, inverse proj}
        frame.getCamera().getProjectionMatrix(ProMatrices, 0, 1.0f, 100.0f);
        Matrix.invertM(ProMatrices, 16, ProMatrices, 0);
        float[] ray_eye = new float[4];
        Matrix.multiplyMV(ray_eye, 0, ProMatrices, 16, ray_clip, 0);

        ray_eye[2] = -1.0f;
        ray_eye[3] = 0.0f;

        float[] out = new float[6];
        float[] ray_wor = new float[4];
        float[] ViewMatrices = new float[32];

        frame.getCamera().getViewMatrix(ViewMatrices, 0);
        Matrix.invertM(ViewMatrices, 16, ViewMatrices, 0);
        Matrix.multiplyMV(ray_wor, 0, ViewMatrices, 16, ray_eye, 0);

        float size = (float)Math.sqrt(ray_wor[0] * ray_wor[0] + ray_wor[1] * ray_wor[1] + ray_wor[2] * ray_wor[2]);

        out[3] = ray_wor[0] / size;
        out[4] = ray_wor[1] / size;
        out[5] = ray_wor[2] / size;

        out[0] = frame.getCamera().getPose().tx();
        out[1] = frame.getCamera().getPose().ty();
        out[2] = frame.getCamera().getPose().tz();
        //Log.d("Ray", out[0] + " " + out[1] + " " + out[2] + " " + out[3] + " " + out[4] + " " + out[5] + " ");

        return out;
    }


}
