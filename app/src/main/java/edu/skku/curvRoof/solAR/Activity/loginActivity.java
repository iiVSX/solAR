package edu.skku.curvRoof.solAR.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.skku.curvRoof.solAR.R;

public class loginActivity extends AppCompatActivity {
    private Button loginBtn;
    private EditText idEt, pwdEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = (Button)findViewById(R.id.loginBtn);
        idEt = (EditText)findViewById(R.id.idEt);
        pwdEt = (EditText)findViewById(R.id.pwdEt);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!idEt.getText().toString().trim().equals("") && !pwdEt.getText().toString().trim().equals("")){
                    Intent intent = new Intent(loginActivity.this, MainActivity.class);
                    intent.putExtra("ID", idEt.getText().toString());
                    intent.putExtra("password", pwdEt.getText().toString());
                    startActivity(intent);
                }
                else{
                    idEt.setText("");
                    pwdEt.setText("");
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
