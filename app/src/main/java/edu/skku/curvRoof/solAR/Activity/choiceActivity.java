package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.skku.curvRoof.solAR.Model.Trial;
import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;
import edu.skku.curvRoof.solAR.Utils.GpsUtil;

public class choiceActivity extends AppCompatActivity {

    private ImageButton roof_button;
    private ImageButton top_button;
    private User user;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private double longitude, latitude;
    private Trial trial;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        final Intent intent =new Intent(this, pointCloudActivity.class);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        user = (User)getIntent().getSerializableExtra("user");
        userID = user.getUserID();

        roof_button=(ImageButton)findViewById(R.id.roof_button);
        top_button=(ImageButton)findViewById(R.id.top_button);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                GpsUtil gpsTracker = new GpsUtil(choiceActivity.this);
                longitude = gpsTracker.getLongitude();
                latitude = gpsTracker.getLatitude();
                Looper.loop();
            }
        });

        t.start();

        roof_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                intent.putExtra("user",user);
                if(user.getUserID() != null){
                    trial = createTrial(longitude,latitude,0);
                    intent.putExtra("trial", trial);
                }
                startActivity(intent);

            }
        });

        top_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                intent.putExtra("user",user);
                if(user.getUserID() != null){
                    createTrial(longitude,latitude,1);
                    intent.putExtra("trial", trial);
                }
                startActivity(intent);
            }
        });
    }

    public Trial createTrial(double latitude, double longitude, int area_type){
        Trial trial = new Trial();
        trial.setTrialID(myRef.child(userID).push().getKey());

        trial.setLongitude(longitude);
        trial.setLatitude(latitude);
        trial.setArea_type(area_type);

        return trial;
    }
}
