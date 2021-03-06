package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.skku.curvRoof.solAR.Model.Cube;
import edu.skku.curvRoof.solAR.Model.Trial;
import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Model.Plane;
import edu.skku.curvRoof.solAR.Renderer.BackgroundRenderer;
import edu.skku.curvRoof.solAR.Renderer.LineRenderer;
import edu.skku.curvRoof.solAR.Renderer.PlaneRenderer;
import edu.skku.curvRoof.solAR.Renderer.PointCloudRenderer;
import edu.skku.curvRoof.solAR.Utils.VectorCal;
import edu.skku.curvRoof.solAR.Utils.elefeeCal;

public class pointCloudActivity extends AppCompatActivity implements GLSurfaceView.Renderer,SensorEventListener {
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final LineRenderer gridRenderer = new LineRenderer();

    private GLSurfaceView glSurfaceView = null;
    private Session session;

    //AR Core and Viewport change variables
    private boolean mUserRequestedInstall = false;
    private boolean mViewportChanged = false;
    private int mViewportWidth = -1;
    private int mViewportHeight = -1;

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

    private static final String REQUEST_URL = "http://developers.curvsurf.com/FindSurface/plane"; // Plane searching server address
    private planeFinder myPlaneFinder;
    private PlaneRenderer planeRenderer = new PlaneRenderer();
    private Plane myPlane;
    private boolean normalValid = false;

    private float[] ray = null;
    private int holedPoint = 0;
    Frame frame;
    private float[] pop;

    //Cube
    Cube cube;
    float change[] = null;
    float[]pNormal = {0,1,0,0};
    int pHold = 0;  // 0 : 아무것도 없음, 1 : control Point 잡음, 2 : 패널 이동
    double direction = 180;
    double angle = 33 ;
    int m,n;
    boolean roofTopmode = true;
    boolean CaptureFlag = false;

    //compass
    SensorManager mSensorManger;
    Sensor mAccelerometer;
    Sensor mMagnetometer;
    float[] mLastAccelerometer = new float[3];
    float[] mLastMagnetometer = new float[3];
    boolean mLastAccelerometerSet = false;
    boolean mLastMagnetometerSet = false;
    float[] mR = new float[9];
    float[] mOrientation = new float[3];
    float azimuthinDegrees;


    //blue btn
    private TextView textView_dir;
    private TextView textView_angle;
    private TextView textView_row;
    private TextView textView_col;
    private Button btn_dir_left;
    private Button btn_dir_right;
    private Button btn_angle_n;
    private Button btn_angle_p;
    private Button btn_row_n;
    private Button btn_row_p;
    private Button btn_col_n;
    private Button btn_col_p;

    //rec information
    private TextView recMsg;
    private LinearLayout recLayout;



    // ------------------ from rendering activity ------------------------- //
    // 임시 값 ///
    private elefeeCal eleCal = new elefeeCal();
    //////////////
    //private Button gotoResult;
    private TextView textView_fee;
    //private TextView expectFee;


    //////////////////////////////////////////////////////////////////////////
    private double area_width;
    private double area_height;

    //tmp

    private StorageReference mRef;
    private boolean captureEnabled = false;
    private User user;
    private Trial trial;
    private LinearLayout dashboard;
    private Bitmap footprint;

    // ----------------- ROI circle -------------------
    private LineRenderer circleRenderer = new LineRenderer();
    private float circleRadius = 0.25f;
    private float z_dis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_cloud_land);

        roofTopmode = getIntent().getBooleanExtra("roopTopMode", true);
        user = (User)getIntent().getSerializableExtra("user");
        trial = (Trial)getIntent().getSerializableExtra("trial");

        myPlaneFinder = new planeFinder();

        glSurfaceView = findViewById(R.id.pointCloud_view);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // dashboard
        dashboard = findViewById(R.id.dashboard);

        recLayout = findViewById(R.id.recMsgLayout);
        recMsg = findViewById(R.id.recMsg_textView);


        backBtn = (Button)findViewById(R.id.BackBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(renderingStage == 5){
                    renderingStage = 4;
                    backBtn.setVisibility(View.INVISIBLE);
                    dashboard.setVisibility(View.INVISIBLE);
                    recLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        Toast.makeText(getApplicationContext(), "오른쪽의 빨간 버튼을 눌러 촬영을 시작하세요", Toast.LENGTH_SHORT).show();

        //
        recordBtn = (Button)findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(renderingStage == 1){
                    renderingStage = 0;
                    pointCloudRenderer.filterHashMap();
                    recMsg.setText("인식하려는 평면을 터치 해주세요");
                    recordBtn.setVisibility(View.GONE);
                    renderingStage = 2;
                }
                else if(renderingStage == 0){
                    renderingStage = 1;
                    recordBtn.setForeground(getApplicationContext().getDrawable(R.drawable.ic_recstop));
                    recMsg.setText("움직이면서 평면을 촬영해주세요");
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
                    //renderingStage = 3;

                    pointCloudRenderer.pickPoint(frame.getCamera());

                    float[] view_seed = pointCloudRenderer.getSeedArr();
                    frame.getCamera().getViewMatrix(viewMatrix,0);
                    Matrix.multiplyMV(view_seed, 0, viewMatrix, 0, view_seed,0);
                    z_dis = -view_seed[2];

                    Log.d("ROIZ", String.format("%.2f", z_dis * circleRadius));

                    renderingStage = 4;

                    if(myPlaneFinder != null){
                        if(myPlaneFinder.getStatus() == AsyncTask.Status.FINISHED || myPlaneFinder.getStatus() == AsyncTask.Status.RUNNING){
                            myPlaneFinder.cancel(true);
                            myPlaneFinder = new planeFinder();
                        }
                        myPlaneFinder.execute();
                    }
                    recordBtn.setVisibility(View.GONE);
                    nextBtn.setVisibility(View.VISIBLE);
                    String value = String.format("%.0f", direction);
                    //textView_dir.setText(value);
                    textView_angle.setText(String.valueOf(angle));
                    recMsg.setText("네 꼭지점을 끌어서 지붕또는 옥상의 크기에 맞추세요");
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
                else if(renderingStage == 5){
                    if(change != null){
                        float[] tVec = new float[3];
                        tVec[0] = change[0] - ray[0]; tVec[1] = change[1] - ray[1]; tVec[2] = change[2] - ray[2];
                        float distance = VectorCal.inner(tVec, ray,3);
                        distance = VectorCal.vectorSize(tVec) * VectorCal.vectorSize(tVec) - distance *distance;


                        switch(event.getAction()) {
                            case MotionEvent.ACTION_DOWN :
                                if(distance < 0.1){
                                    pHold = 2;
                                }
                            case MotionEvent.ACTION_MOVE :
                                if(pHold == 2)  {
                                    change = myPlane.rayOnPlane(ray);
                                }
                                else if(pHold == 1){

                                }
                                break;
                            case MotionEvent.ACTION_UP   :
                                pHold = 0;
                        }
                    }
                }

                return true;
            }
        });

        //blue btn
        textView_dir= findViewById(R.id.textView_dir);
        textView_dir.setText(String.valueOf(direction));
        btn_dir_left = (Button)findViewById(R.id.btn_dir_left);
        btn_dir_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direction --;
                String value = String.format("%.0f", direction);
                textView_dir.setText(value);
                textView_fee.setText(
                        String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                );
            }
        });

        btn_dir_right = (Button)findViewById(R.id.btn_dir_right);
        btn_dir_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direction ++;
                String value = String.format("%.0f", direction);
                textView_dir.setText(value);
                textView_fee.setText(
                        String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                );
            }
        });

        textView_angle= findViewById(R.id.textView_angle);
        textView_angle.setText(String.valueOf(angle));
        btn_angle_n = (Button)findViewById(R.id.btn_angle_n);
        btn_angle_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle --;
                String value = String.format("%.0f", angle);
                textView_angle.setText(value);
                textView_fee.setText(
                        String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                );
            }
        });

        btn_angle_p = (Button)findViewById(R.id.btn_angle_p);
        btn_angle_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle ++;
                String value = String.format("%.0f", angle);
                textView_angle.setText(value);
                textView_fee.setText(
                        String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                );
            }
        });

        //----------- from rendering activity --------------//
        //계산




        //////////////////////////////////////////////////////
        //tmp
        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(renderingStage == 4){
                    renderingStage = 5;
                    recLayout.setVisibility(View.GONE);
                    backBtn.setVisibility(View.VISIBLE);
                    dashboard.setVisibility(View.VISIBLE);

                    change = myPlane.getPanelPivot();

                    float[] widthV = {
                            myPlane.getLl()[0] - myPlane.getLr()[0],
                            myPlane.getLl()[1] - myPlane.getLr()[1],
                            myPlane.getLl()[2] - myPlane.getLr()[2]
                    };
                    float[] heightV = {
                            myPlane.getLl()[0] - myPlane.getUl()[0],
                            myPlane.getLl()[1] - myPlane.getUl()[1],
                            myPlane.getLl()[2] - myPlane.getUl()[2]
                    };

                    n = (int)(VectorCal.vectorSize(heightV)/(1.0f * 1.0f));
                    m= (int)(VectorCal.vectorSize(widthV)/(1.0f * 1.67f));

                    textView_row.setText(String.valueOf(n));
                    textView_col.setText(String.valueOf(m));
                    textView_fee.setText(
                            String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                    );


                    if(roofTopmode){
                        angle = trial.getOptimalAngle();
                        direction = trial.getOptimalAzimuth();
                    }
                    else{
                        angle = myPlane.getAngle();
                        direction = myPlane.getDir();
                    }
                    String value = String.format("%.0f", direction);
                    textView_dir.setText(value);
                    value = String.format("%.0f", angle);
                    textView_angle.setText(value);

                    renderingStage = 5;
                }
                else if(renderingStage == 5){
                    // next activity(result_activity)
                    trial.setAngle(angle);
                    trial.setAzimuth(direction);
                    area_width = m * (1.0 * 1.67);
                    area_height = n * (1.0 * 1.0f);
                    trial.setPanel_count(n*m);
                    trial.setArea_height(area_height);
                    trial.setArea_height(area_width);

                    eleCal.calUserfee(user, n, m,direction, angle); // calUserfee으로 userfee, money, generate의 값을 update
                   user.setElec_fee(eleCal.userfee);
                    user.setExpect_fee(eleCal.money);
                    trial.setElec_gen(eleCal.generate);

                    //captureView(footprint);
                    CaptureFlag = true;
                    Intent intent = new Intent(getApplicationContext(), resultSplashActivity.class);
                    startActivity(intent);
                }


            }

        });
        ////

        textView_fee = findViewById(R.id.textView_fee);
        textView_row = findViewById(R.id.textView_row);
        textView_col = findViewById(R.id.textView_col);
        btn_row_n = findViewById(R.id.button_row_n);
        btn_row_p = findViewById(R.id.button_row_p);
        btn_col_n = findViewById(R.id.button_col_n);
        btn_col_p = findViewById(R.id.button_col_p);

        btn_row_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(n>1){
                    n--;
                    textView_row.setText(String.valueOf(n));
                    textView_fee.setText(
                            String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                    );
                }
            }
        });
        btn_row_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(n<9){
                    n++;
                    textView_row.setText(String.valueOf(n));
                    textView_fee.setText(
                            String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                    );
                }
            }
        });
        btn_col_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m>1){
                    m--;
                    textView_col.setText(String.valueOf(m));
                    textView_fee.setText(
                            String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                    );
                }
            }
        });
        btn_col_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m<9){
                    m++;
                    textView_col.setText(String.valueOf(m));
                    textView_fee.setText(
                            String.format("%.0f",eleCal.calUserfee(user, n, m, direction, angle)) + "원"
                    );
                }
            }
        });

        mUserRequestedInstall = false;

        mSensorManger = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

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
        mSensorManger.unregisterListener(this, mAccelerometer);
        mSensorManger.unregisterListener(this, mMagnetometer);

    }

    @Override
    protected void onResume() {
        super.onResume();

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
                Log.d("ULTRA", e.getMessage());
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
        mSensorManger.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManger.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        try{
            backgroundRenderer.createOnGlThread(this);
            pointCloudRenderer.createGlThread(this);
            planeRenderer.createGlThread(this);
            cube = new Cube(this, glSurfaceView);
            gridRenderer.createGlThread(this);
            circleRenderer.createGlThread(this);
            circleRenderer.setCircleVertex(circleRadius);

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

                circleRenderer.draw_circle(projMatrix);
                Matrix.multiplyMM(vpMatrix, 0, projMatrix,0,viewMatrix,0);

                Log.d("Matrix", "start");
                for(int i = 0; i<16; i+=4){
                    Log.d("Matrix", String.format("%.5f %.5f %.5f %.5f", viewMatrix[i],viewMatrix[i+1],viewMatrix[i+2],viewMatrix[i+3]));
                }

                switch (renderingStage){
                    case 0:
                        pointCloudRenderer.update(frame.acquirePointCloud(),false);
                        pointCloudRenderer.draw_initial(vpMatrix);
                        break;
                    case 1: // recording point Cloud
                        pointCloudRenderer.update(frame.acquirePointCloud(),true);
                        pointCloudRenderer.draw(vpMatrix);
                        break;

                    case 2: // rendering recorded point cloud
                        if(pointCloudRenderer.getFiltered_pointCloud() != null){
                            pointCloudRenderer.draw_final(vpMatrix);
                        }

                        break;

                    case 3: // pickPoint
//                    pointCloudRenderer.pickPoint(camera);
//                    renderingStage = 4;
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

                            // grid
                            gridRenderer.bufferUpdate(myPlane.gridPoints[0],myPlane.gridPoints[4]);
                            gridRenderer.draw(vpMatrix);
                            gridRenderer.bufferUpdate(myPlane.gridPoints[4],myPlane.gridPoints[8]);
                            gridRenderer.draw(vpMatrix);
                            gridRenderer.bufferUpdate(myPlane.gridPoints[8],myPlane.gridPoints[12]);
                            gridRenderer.draw(vpMatrix);
                            gridRenderer.bufferUpdate(myPlane.gridPoints[12],myPlane.gridPoints[0]);
                            gridRenderer.draw(vpMatrix);

                            gridRenderer.bufferUpdate(myPlane.gridPoints[1],myPlane.gridPoints[11]);
                            gridRenderer.draw(vpMatrix);
                            gridRenderer.bufferUpdate(myPlane.gridPoints[2],myPlane.gridPoints[10]);
                            gridRenderer.draw(vpMatrix);
                            gridRenderer.bufferUpdate(myPlane.gridPoints[3],myPlane.gridPoints[9]);
                            gridRenderer.draw(vpMatrix);

                            gridRenderer.bufferUpdate(myPlane.gridPoints[5],myPlane.gridPoints[15]);
                            gridRenderer.draw(vpMatrix);
                            gridRenderer.bufferUpdate(myPlane.gridPoints[6],myPlane.gridPoints[14]);
                            gridRenderer.draw(vpMatrix);
                            gridRenderer.bufferUpdate(myPlane.gridPoints[7],myPlane.gridPoints[13]);
                            gridRenderer.draw(vpMatrix);
                        }
                        break;

                    case 5:         // cube 렌더링

                        float[] originMatrix = new float[16];
                        Matrix.setIdentityM(originMatrix,0);
//                    float[] h = {
//                            myPlane.getLl()[0] - myPlane.getUl()[0],
//                            myPlane.getLl()[1] - myPlane.getUl()[1],
//                            myPlane.getLl()[2] - myPlane.getUl()[2],
//                    };
//                    float pHeight = VectorCal.vectorSize(h);
                        Matrix.translateM(originMatrix, 0, -(1.0f * 1.67f)*m*0.5f, 0, 0);

                        float[] rotateMatrix = new float[16];
                        Matrix.setIdentityM(rotateMatrix, 0);
                        Matrix.rotateM(rotateMatrix,0, (float)angle, 1,0,0);

                        float[] transMatrix = new float[16];
                        Matrix.setIdentityM(transMatrix, 0);
                        Matrix.translateM(transMatrix,0,change[0], change[1], change[2]);

                        float[] dirMatrix = new float[16];
                        Matrix.setIdentityM(dirMatrix, 0);
                        Matrix.rotateM(dirMatrix,0, ((float)direction+180)%360, 0,1,0);

                        float[] mvpMatrix = new float[16];
                        planeRenderer.draw(vpMatrix);

                        gridRenderer.bufferUpdate(myPlane.gridPoints[0],myPlane.gridPoints[4]);
                        gridRenderer.draw(vpMatrix);
                        gridRenderer.bufferUpdate(myPlane.gridPoints[4],myPlane.gridPoints[8]);
                        gridRenderer.draw(vpMatrix);
                        gridRenderer.bufferUpdate(myPlane.gridPoints[8],myPlane.gridPoints[12]);
                        gridRenderer.draw(vpMatrix);
                        gridRenderer.bufferUpdate(myPlane.gridPoints[12],myPlane.gridPoints[0]);
                        gridRenderer.draw(vpMatrix);

                        gridRenderer.bufferUpdate(myPlane.gridPoints[1],myPlane.gridPoints[11]);
                        gridRenderer.draw(vpMatrix);
                        gridRenderer.bufferUpdate(myPlane.gridPoints[2],myPlane.gridPoints[10]);
                        gridRenderer.draw(vpMatrix);
                        gridRenderer.bufferUpdate(myPlane.gridPoints[3],myPlane.gridPoints[9]);
                        gridRenderer.draw(vpMatrix);

                        gridRenderer.bufferUpdate(myPlane.gridPoints[5],myPlane.gridPoints[15]);
                        gridRenderer.draw(vpMatrix);
                        gridRenderer.bufferUpdate(myPlane.gridPoints[6],myPlane.gridPoints[14]);
                        gridRenderer.draw(vpMatrix);
                        gridRenderer.bufferUpdate(myPlane.gridPoints[7],myPlane.gridPoints[13]);
                        gridRenderer.draw(vpMatrix);
                        for(int i = 0;i<n;i++){
                            for(int j = 0;j<m;j++){
                                float[] model = new float[16];

                                Matrix.multiplyMM(model, 0, cube.MDS[i][j],0, originMatrix, 0);
                                Matrix.multiplyMM(mvpMatrix, 0, dirMatrix, 0, rotateMatrix, 0);
                                Matrix.multiplyMM(mvpMatrix, 0, transMatrix, 0, mvpMatrix, 0);
                                Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, model, 0);
                                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, mvpMatrix, 0);


                                cube.draw(mvpMatrix, 0);
                                cube.draw(mvpMatrix, 1);
                                cube.draw(mvpMatrix, 2);
                                cube.draw(mvpMatrix, 3);
                                cube.draw(mvpMatrix, 4);
                                cube.draw(mvpMatrix, 5);

                                if(CaptureFlag == true){
                                    CaptureFlag = false;
                                    footprint = getScreenshot();
                                    captureView(footprint);
                                }
                            }
                        }
                        break;
                }
            }

        }catch(CameraNotAvailableException e){
            finish();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {                    // 센서 값 바뀌면 불리는 callback 함수
        if(event.sensor == mAccelerometer){
            System.arraycopy(event.values,0 ,mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        }
        else if(event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }

        if(mLastAccelerometerSet && mLastMagnetometerSet){
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            azimuthinDegrees = (int)( Math.toDegrees( SensorManager.getOrientation( mR, mOrientation)[0] ) + 360 ) % 360;
            Log.d("Magnet Degree", azimuthinDegrees + "'");
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class planeFinder extends AsyncTask<Object, ResponseForm.PlaneParam, ResponseForm.PlaneParam> {
        @Override
        protected ResponseForm.PlaneParam doInBackground(Object[] objects) {
            // Ready Point Cloud
            FloatBuffer points = pointCloudRenderer.getFiltered_pointCloud();

            // Ready Request Form
            RequestForm rf = new RequestForm();

            rf.setPointBufferDescription(points.capacity()/4, 16, 0); //pointcount, pointstride, pointoffset
            rf.setPointDataDescription(0.05f, 0.05f); //accuracy, meanDistance
            rf.setTargetROI(pointCloudRenderer.getSeedPoint(), Math.max(z_dis * circleRadius, 0.1f));//seedIndex,touchRadius
            rf.setAlgorithmParameter(RequestForm.SearchLevel.NORMAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp
            Log.d("PointsBuffer", points.toString());
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
                float[] ll = {seed[0]-0.3f,seed[1],seed[2]+0.3f};
                float[] lr = {seed[0]+0.3f,seed[1],seed[2]+0.3f};
                float[] ur = {seed[0]+0.3f,seed[1],seed[2]-0.3f};
                float[] ul = {seed[0]-0.3f,seed[1],seed[2]-0.3f};
                myPlane = new Plane(ll,lr,ur,ul,frame.getCamera());
                Toast.makeText(getApplicationContext(), "추출 실패" + String.format(" ROI: %.2f", circleRadius * z_dis),Toast.LENGTH_SHORT).show();
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

        return out;
    }


    public Bitmap getScreenshot(){
        Bitmap bitmap;
        int screenshotSize = mViewportWidth *mViewportHeight;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(screenshotSize*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        GLES20.glReadPixels(0,0,mViewportWidth,mViewportHeight,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,byteBuffer);
        int pixelsBuffer[] = new int[screenshotSize];
        byteBuffer.asIntBuffer().get(pixelsBuffer);
        byteBuffer = null;

        bitmap = Bitmap.createBitmap(mViewportWidth,mViewportHeight,Bitmap.Config.RGB_565);
        bitmap.setPixels(pixelsBuffer,screenshotSize-mViewportWidth,-mViewportWidth,0,0,mViewportWidth,mViewportHeight);
        pixelsBuffer = null;

        short sBuffer[] = new short[screenshotSize];
        ShortBuffer sb = ShortBuffer.wrap(sBuffer);
        bitmap.copyPixelsToBuffer(sb);

        for(int i = 0;i<screenshotSize;++i){
            short v = sBuffer[i];
            sBuffer[i] = (short)(((v&0x1f)<<11)|(v&0x7e0)|((v&0xf800)>>11));
        }
        sb.rewind();
        bitmap.copyPixelsFromBuffer(sb);

        return bitmap;
    }
    public void captureView(Bitmap bitmap) {


        mRef = FirebaseStorage.getInstance().getReference();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] data = out.toByteArray();

        final StorageReference storeRef = mRef.child(trial.getTrialID()+".jpg");

        try {
            UploadTask uploadTask = storeRef.putBytes(data);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storeRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Intent intentmypage = new Intent(pointCloudActivity.this, resultActivity.class);
                        Toast.makeText(getApplicationContext(), "이미지 저장 중입니다.", Toast.LENGTH_LONG);
                        Uri downloadUri = task.getResult();
                        trial.setCaptureUrl(downloadUri.toString());
                        intentmypage.putExtra("user", user);
                        intentmypage.putExtra("trial", trial);
                        startActivity(intentmypage);
                        finish();
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        } catch (Exception e) {
            Log.d("PLUSULTRA", e.getMessage());
        }
    }
}
