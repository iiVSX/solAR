package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.skku.curvRoof.solAR.R;

public class choiceActivity extends AppCompatActivity {

    private ImageButton roof_button;
    private ImageButton top_button;
    private String userID;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        final Intent intent =new Intent(this, pointCloudActivity.class);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        Intent fromintent = getIntent();
        userID = fromintent.getStringExtra("userID");

        roof_button=(ImageButton)findViewById(R.id.roof_button);
        top_button=(ImageButton)findViewById(R.id.top_button);

        roof_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                intent.putExtra("userID",userID);
                if(userID != null){
                    myRef.child("user_list").child(userID).child("area_type").setValue("0");
                }
                startActivity(intent);

            }
        });

        top_button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                intent.putExtra("userID",userID);
                if(userID != null){
                    myRef.child("user_list").child(userID).child("area_type").setValue("1");
                }
                startActivity(intent);
            }
        });
    }
}
