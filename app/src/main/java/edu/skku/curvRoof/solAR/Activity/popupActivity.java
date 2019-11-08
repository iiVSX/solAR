package edu.skku.curvRoof.solAR.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import edu.skku.curvRoof.solAR.R;

public class popupActivity extends Activity {

    private Button yesBtn;
    private Button noBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);

        yesBtn=(Button)findViewById(R.id.yesBtn);
        noBtn=(Button)findViewById(R.id.noBtn);

    }
    //Yes 버튼 클릭시 반응
    public void mOnYes(View v){
        yesBtn.setBackgroundColor(Color.GRAY);
        Intent intent = new Intent(popupActivity.this,loginActivity.class);
        setResult(RESULT_OK, intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    //No 버튼 클릭시 반응
    public void mOnNo(View view) {
        noBtn.setBackgroundColor(Color.GRAY);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        return;
    }


}
