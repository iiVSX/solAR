package edu.skku.curvRoof.solAR.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import edu.skku.curvRoof.solAR.Utils.VectorCal;

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
    private float[] pop;

    //Cube
    Cube cube;
    float change[] = null;
    float[]pNormal = {0,1,0,0};
    int pHold = 0;  // 0 : 아무것도 없음, 1 : control Point 잡음, 2 : 패널 이동
    double direction = 0;
    double angle = 33;
    int m,n;

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



    // ------------------ from rendering activity ------------------------- //
    // 임시 값 ///
    private double panelinfo =  7.5;//1.64 x 0.99 x 15.4 x 30(1month)
    private double panelnum = 10;
    private double radiation = 3.57;
    private double userfee; //월평균 전기세
    private double monthlyuse; //userfee를 통해 알아낸 월 평균 전기 사용량
    private double money; //예상 전기세
    private double result; // 월평균 사용량 - 예상 발전량
    private double generate; //예상 발전량
    private double longitude, latitude;
    //////////////
    //private Button gotoResult;
    private TextView textView_fee;
    //private TextView expectFee;


    //////////////////////////////////////////////////////////////////////////
    private double area_width;
    private double area_height;

    //tmp

    private StorageReference mRef;
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
                    String value = String.format("%.0f", direction);
                    textView_dir.setText(value);
                    textView_angle.setText(String.valueOf(angle));

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
                textView_dir.setText(String.valueOf(direction));
            }
        });

        btn_dir_right = (Button)findViewById(R.id.btn_dir_right);
        btn_dir_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                direction ++;
                String value = String.format("%.0f", direction);
                textView_dir.setText(value);
            }
        });

        textView_angle= findViewById(R.id.textView_angle);
        textView_angle.setText(String.valueOf(angle));
        btn_angle_n = (Button)findViewById(R.id.btn_angle_n);
        btn_angle_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle --;
                textView_angle.setText(String.valueOf(angle));
            }
        });

        btn_angle_p = (Button)findViewById(R.id.btn_angle_p);
        btn_angle_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angle ++;
                textView_angle.setText(String.valueOf(angle));
            }
        });

        //----------- from rendering activity --------------//
        //Intent intent = new Intent(this, receiptActivity.calculateSplashActivity.class);
        //startActivity(intent);

        //결과화면으로
        /*
        gotoResult = (Button)findViewById(R.id.gotoresult);
        gotoResult.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(renderingActivity.this, resultActivity.class);
                //i.putExtra("expectgen",generate);
                i.putExtra("userfee", userfee); //사용자 전기세 전송
                //i.putExtra("monthlyuse", monthlyuse);
                //i.putExtra("realuse", result);
                i.putExtra("expectfee", money); //예상 전기세 전송
                //i.putExtra("usermoney", userfee);
                i.putExtra("user", user);
                i.putExtra("trial", trial);
                startActivity(i);
            }
        });
        */

        //계산
        userfee = user.getElec_fee();
        Log.d("adfasdfasdf", String.valueOf(userfee));
//        longitude = trial.getLongitude();
//        latitude = trial.getLatitude();
        /**
         * 1.DB에서 사용자의 전기세 받아오기.
         * 2.위치정보 받아서 DB에서 일사량 가져오기.
         * 3.면적통해서 개수 받아오기.
         * **/

        double temp;
        //유저의 전기세를 바탕으로 사용전력량 계산
        if (userfee <= 17960) {
            //printf("태양광 발전을 필요로 하지 않습니다.");
        }
        else if (userfee <= 65760) {
            temp = userfee / 1.137;
            monthlyuse = ((temp - 20260) / 187.9) + 200;
        }
        else {
            temp = userfee / 1.137;
            monthlyuse = ((temp - 57840) / 280.6) + 400;
        }
        generate = panelinfo*panelnum*radiation; //발전량 계산
        //expectGen.setText(generate);
        result = monthlyuse - generate;


        //예상 전기료 계산
        if (result <= 200) {
            temp = 910 + 93.3 * result;
            if (temp < 5000) money = 1130;
            else {
                money = (temp - 4000) * 1.137;
            }
        }
        else if (result <= 400) {
            temp = 20260 + ((result - 200) * 187.9);
            money = temp * 1.137;
        }
        else {
            temp = 57840 + ((result - 400) * 280.6);
            money = temp * 1.137;
        }
        String tmpgen = String.format("%.0f", generate);
        String tmpmon = String.format("%.0f", money);
        textView_fee = findViewById(R.id.textView_fee);
        //expectFee = findViewById(R.id.expectfee);
        //expectGen.setText(tmpgen+"kWh");
        textView_fee.setText(tmpmon+"원");

        //////////////////////////////////////////////////////
        //tmp
        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(renderingStage == 4){
                    renderingStage = 5;
                    backBtn.setVisibility(View.VISIBLE);
                    dashboard.setVisibility(View.VISIBLE);

                    change = myPlane.getPanelPivot();
                    direction = myPlane.getDir();

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
                    String value = String.format("%.0f", direction);
                    textView_dir.setText(value);
                    value = String.format("%.0f", angle);
                    textView_angle.setText(value);
                    textView_row.setText(String.valueOf(n));
                    textView_col.setText(String.valueOf(m));
                    renderingStage = 5;
                }
                else if(renderingStage == 5){
                    // next activity(result_activity)
                    Intent intentmypage = new Intent(pointCloudActivity.this, resultActivity.class);
                    trial = new Trial();
                    trial.setAngle(angle);
                    trial.setAzimuth(direction);
                    area_width = m * (0.1 * 1.67);
                    area_height = n * (0.1 * 1.0f);
                    trial.setPanel_count((int)(n*m));
                    trial.setArea_height(area_height);
                    trial.setArea_height(area_width);
                    user.setElec_fee(userfee);
                    user.setExpect_fee(money);
                    trial.setElec_gen(generate);
                    captureView(glSurfaceView);
                    intentmypage.putExtra("user", user);
                    intentmypage.putExtra("trial", trial);
                    startActivity(intentmypage);

                }
            }
        });
        ////


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
                }
            }
        });
        btn_row_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(n<9){
                    n++;
                    textView_row.setText(String.valueOf(n));
                }
            }
        });
        btn_col_n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(n>1){
                    m--;
                    textView_col.setText(String.valueOf(m));
                }
            }
        });
        btn_col_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m<9){
                    m++;
                    textView_col.setText(String.valueOf(m));
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
                Log.d("PLUSULTRA", e.getMessage());
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
                    Matrix.rotateM(dirMatrix,0, (float)direction, 0,1,0);

                    float[] mvpMatrix = new float[16];
                    planeRenderer.draw(vpMatrix);
                    //pointCloudRenderer.draw_origin(vpMatrix);
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
                float[] ll = {seed[0]-0.3f,seed[1],seed[2]+0.3f};
                float[] lr = {seed[0]+0.3f,seed[1],seed[2]+0.3f};
                float[] ur = {seed[0]+0.3f,seed[1],seed[2]-0.3f};
                float[] ul = {seed[0]-0.3f,seed[1],seed[2]-0.3f};
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

    public void captureView(View View) {
        String CAPTURE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/solAR";
        View.buildDrawingCache();
        Bitmap captureView = View.getDrawingCache();
        mRef = FirebaseStorage.getInstance().getReference();
        FileOutputStream fos;

        File path = new File(CAPTURE_PATH);
        if(!path.isDirectory()){
            path.mkdirs();
        }
        String filePath = CAPTURE_PATH+"/"+trial.getTrialID()+ ".png";

        try {
            fos = new FileOutputStream(filePath);
            captureView.compress(Bitmap.CompressFormat.PNG, 100, fos);

            Uri file = Uri.fromFile(new File(filePath));
            mRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                    trial.setCaptureUrl(downloadUrl.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT);
                }
            });
        } catch (FileNotFoundException e) {
            Log.d("PLUSULTRA", e.getMessage());
        }
    }

    public double getOptimalAngle(){
        double longitude = trial.getLongitude();
        return 31.39 + 0.0471*longitude;
    }

    public double getOptimalAzimuth(){
        double latitude = trial.getLatitude();
        return 178.65 + 0.0177*latitude;
    }
}
