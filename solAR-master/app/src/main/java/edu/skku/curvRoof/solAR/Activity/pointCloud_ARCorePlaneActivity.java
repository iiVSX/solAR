package edu.skku.curvRoof.solAR.Activity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.skku.curvRoof.solAR.Model.Cube;
import edu.skku.curvRoof.solAR.Model.Point;
import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Renderer.ARCore_PlaneRenderer;
import edu.skku.curvRoof.solAR.Renderer.BackgroundRenderer;
import edu.skku.curvRoof.solAR.Renderer.PointCloudRenderer;

public class pointCloud_ARCorePlaneActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final ARCore_PlaneRenderer planeRenderer = new ARCore_PlaneRenderer();

    private GLSurfaceView glSurfaceView = null;
    private Session session;
    private Frame frame;

    private boolean mUserRequestInstall = false;
    private boolean mViewportChanged = false;
    private int mViewportWidth = -1;
    private int mViewportHeight = -1;

    private Button recordBtn;
    private TextView recMsg;

    private List<HitResult> hitResults;

    // HitTest test
    private ArrayList<Point> TestPoints = new ArrayList<>();
    private PointCloudRenderer hitRenderer = new PointCloudRenderer();
    private BlockingQueue<MotionEvent> touchTap = new ArrayBlockingQueue<>(16);

    //Panel Rendering
    private Cube cube;
    double direction = 180;
    double angle = 33;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_cloud_land);
        recordBtn = findViewById(R.id.recordBtn);
        recordBtn.setVisibility(View.GONE);
        recMsg = findViewById(R.id.recMsg_textView);
        recMsg.setText("평면을 찾는 중입니다");

        glSurfaceView = findViewById(R.id.pointCloud_view);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                touchTap.offer(event);
                return true;
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(session != null){
            glSurfaceView.onPause();
            session.pause();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(session == null){
            try{
                switch (ArCoreApk.getInstance().requestInstall(this, !mUserRequestInstall)){
                    case INSTALL_REQUESTED:
                        mUserRequestInstall = true;
                        return;
                    case INSTALLED:
                        break;
                }
                session = new Session(this);

                Config config = new Config(session);
                config.setFocusMode(Config.FocusMode.AUTO);
                session.configure(config);
            }catch(Exception e){
                Log.d("onResume", e.getMessage());
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
            planeRenderer.createOnGlThread(this, "model/msquare.png");
            hitRenderer.createGlThread(this);
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
            session.setCameraTextureName(backgroundRenderer.getTextureId());
            frame = session.update();
            Camera camera = frame.getCamera();


            backgroundRenderer.draw(frame);

            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            tapHandle();

            if(hasTrackingPlane()){
                recMsg.setText("패널을 설치하려는 곳을 터치 해주세요");
                float[] vpMatrix = new float[16];
                Matrix.multiplyMM(vpMatrix, 0, projmtx, 0, viewmtx, 0);
                if(!TestPoints.isEmpty()){
//                    for(Point point : TestPoints){
//                        float[] temp = new float[]{point.getX(),point.getY(), point.getZ(),0};
//                        hitRenderer.draw_APoint(vpMatrix, temp);
//                    }

                    // 패널 그리기
                    Point tPose = TestPoints.get(TestPoints.size()-1);
                    float[] mvpMatrix = new float[16];
                    float[] rotateMatrix = new float[16];
                    Matrix.setIdentityM(rotateMatrix, 0);
                    Matrix.rotateM(rotateMatrix,0, (float)angle, 1,0,0);

                    float[] transMatrix = new float[16];
                    Matrix.setIdentityM(transMatrix, 0);
                    Matrix.translateM(transMatrix,0,tPose.getX(), tPose.getY(), tPose.getZ());

                    float[] dirMatrix = new float[16];
                    Matrix.setIdentityM(dirMatrix, 0);
                    Matrix.rotateM(dirMatrix,0, ((float)direction+180)%360, 0,1,0);


                    Matrix.multiplyMM(mvpMatrix, 0, rotateMatrix, 0, cube.MDS[0][0],0);
                    Matrix.multiplyMM(mvpMatrix, 0, dirMatrix, 0, mvpMatrix, 0);
                    Matrix.multiplyMM(mvpMatrix, 0, transMatrix, 0, mvpMatrix, 0);
                    Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, mvpMatrix, 0);

                    cube.draw(mvpMatrix, 0);
                    cube.draw(mvpMatrix, 1);
                    cube.draw(mvpMatrix, 2);
                    cube.draw(mvpMatrix, 3);
                    cube.draw(mvpMatrix, 4);
                    cube.draw(mvpMatrix, 5);
                }
            }
            else{
                recMsg.setText("평면을 찾는 중입니다");
            }
            planeRenderer.drawPlanes(session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);
        }catch (CameraNotAvailableException e){
            finish();
        }
    }

    private boolean hasTrackingPlane() {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    private boolean isInPlane(Pose p){
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.isPoseInPolygon(p)) {
                return true;
            }
        }
        return false;
    }

    private void tapHandle(){
        MotionEvent tap = touchTap.poll();

        if(hasTrackingPlane() && tap != null){
            hitResults = frame.hitTest(tap.getX(), tap.getY());
            for(HitResult hitResult : hitResults){
                Trackable trackable = hitResult.getTrackable();
                if ((trackable instanceof Plane
                        && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())
                        && (ARCore_PlaneRenderer.calculateDistanceToPlane(hitResult.getHitPose(), frame.getCamera().getPose()) > 0))
                        || (trackable instanceof com.google.ar.core.Point
                        && ((com.google.ar.core.Point) trackable).getOrientationMode()
                        == com.google.ar.core.Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)){
                    Log.d("HITHIT", hitResult.getHitPose().toString());
                    if (TestPoints.size() >= 20) {
                        TestPoints.remove(0);
                    }
                    Point hitPoint = new Point(
                            hitResult.getHitPose().tx(),
                            hitResult.getHitPose().ty(),
                            hitResult.getHitPose().tz(),
                            1.0f);
                    TestPoints.add(hitPoint);
                }
            }
        }
    }
}
