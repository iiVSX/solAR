package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import edu.skku.curvRoof.solAR.Model.User;
import edu.skku.curvRoof.solAR.R;

public class historyPageActivity extends AppCompatActivity {
    private User user;
    private String trialID;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = database.getReference();
    private StorageReference sRef = FirebaseStorage.getInstance().getReference();
    private String img_url;

    FloatingActionButton companyListBtn;
    TextView panelNumTv, roofAreaTv, panelRangeTv, expectElecTv, expectFeeTv, trialIDTv;
    ImageView trialImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historypage);

        panelNumTv = (TextView)findViewById(R.id.panelText);
        roofAreaTv = (TextView)findViewById(R.id.roofText);
        panelRangeTv = (TextView)findViewById(R.id.useroofText);
        expectElecTv = (TextView)findViewById(R.id.elecText);
        expectFeeTv = (TextView)findViewById(R.id.elecfeeText);
        trialIDTv = (TextView)findViewById(R.id.trialIDTv);
        trialImg = (ImageView)findViewById(R.id.resultImage);
        companyListBtn = (FloatingActionButton)findViewById(R.id.companyListBtn);

        user = (User)getIntent().getSerializableExtra("user");
        trialID = getIntent().getStringExtra("trialID");

        trialIDTv.setText(trialID);

        mRef.child("user_list").child(user.getUserID()).child(trialID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String panelStr = dataSnapshot.child("panel_count").getValue().toString();
                panelNumTv.setText(panelStr);
                String area = dataSnapshot.child("area_width").getValue().toString()+"*"+dataSnapshot.child("area_height").getValue().toString();
                roofAreaTv.setText(area);
                expectElecTv.setText(dataSnapshot.child("expect_gen").getValue().toString());
                expectFeeTv.setText(dataSnapshot.child("expect_fee").getValue().toString());
                img_url = dataSnapshot.child("img_url").getValue().toString();
                try{
                    final File file = File.createTempFile("images", "jpg");
                    sRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            String filePath = file.getPath();
                            Bitmap image = BitmapFactory.decodeFile(filePath);
                            trialImg.setImageBitmap(image);
                        }
                    });
                }catch(IOException e){
                    Log.d("PLUSULTRA", e.getMessage());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        companyListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent companyListIntent = new Intent(getApplicationContext(), companyListActivity.class);
                startActivity(companyListIntent);
            }
        });
    }
}
