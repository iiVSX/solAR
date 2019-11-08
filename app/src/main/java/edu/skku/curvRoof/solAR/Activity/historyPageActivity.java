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
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
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
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference sRef = storage.getReference();
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

        mRef.child("user_list").child(user.getUserID()).child(trialID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String panelStr = dataSnapshot.child("panel_count").getValue().toString();
                panelNumTv.setText(panelStr);
                String area = dataSnapshot.child("area_width").getValue().toString()+"*"+dataSnapshot.child("area_height").getValue().toString();
                roofAreaTv.setText(area);
                expectElecTv.setText(dataSnapshot.child("expect_gen").getValue().toString());
                expectFeeTv.setText(dataSnapshot.child("expect_fee").getValue().toString());
                img_url = dataSnapshot.child("img_url").getValue().toString();
                trialIDTv.setText(dataSnapshot.child("now_time").getValue().toString());
                try{
                    if(img_url != null){
                        StorageReference imgRef = storage.getReferenceFromUrl(img_url);
                        final long MAX_BYTES = 1024*1024*8;
                        imgRef.getBytes(MAX_BYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                trialImg.setImageBitmap(bitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "이미지 다운로드에 실패하였습니다.", Toast.LENGTH_SHORT);
                            }
                        });
                    }

                }catch(Exception e){
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
