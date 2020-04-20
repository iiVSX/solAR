package edu.skku.curvRoof.solAR.Activity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Renderer.ARCore_PlaneRenderer;
import edu.skku.curvRoof.solAR.Renderer.BackgroundRenderer;

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
                float tx = event.getX();
                float ty = event.getY();
                if(hasTrackingPlane()){
                    hitResults = frame.hitTest(tx, tx);
                    for(HitResult hitResult : hitResults){
                        Log.d("HITHIT", hitResult.getHitPose().toString());
                    }
                }
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

            if(hasTrackingPlane()){
                recMsg.setText("패널을 설치하려는 곳을 터치 해주세요");

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
}
